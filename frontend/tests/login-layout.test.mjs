import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import vm from 'node:vm'
import { createRequire } from 'node:module'
import { fileURLToPath, URL, URLSearchParams } from 'node:url'
import React from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import ts from 'typescript'

const require = createRequire(import.meta.url)
const root = fileURLToPath(new URL('../src/', import.meta.url))

function renderAccess(loading = false) {
  const cache = new Map()
  let LoginPage
  const link = ({ to, children, ...props }) => React.createElement('a', { ...props, href: to }, children)
  function load(filename) {
    if (filename.endsWith('.png')) return { default: '/assets/brasao-athletix.png' }
    if (filename.endsWith('/features/auth/AuthSessionProvider.tsx'))
      return {
        useAuthSession: () => ({ state: null, loading }),
      }
    if (filename.endsWith('/lib/http.ts')) return { HttpError: class extends Error {} }
    if (cache.has(filename)) return cache.get(filename)
    const exports = {}
    cache.set(filename, exports)
    const code = ts.transpileModule(fs.readFileSync(filename, 'utf8'), {
      compilerOptions: {
        module: ts.ModuleKind.CommonJS,
        target: ts.ScriptTarget.ES2022,
        jsx: ts.JsxEmit.ReactJSX,
      },
    }).outputText
    vm.runInNewContext(
      code,
      {
        exports,
        require: (dependency) => {
          if (dependency === 'react-router-dom')
            return {
              Link: link,
              useNavigate: () => () => {},
              useSearchParams: () => [new URLSearchParams()],
              Outlet: () => React.createElement(LoginPage),
            }
          if (!dependency.startsWith('.')) return require(dependency)
          const resolved = path.resolve(path.dirname(filename), dependency)
          if (resolved === path.join(root, 'components/ui'))
            return {
              ...load(path.join(root, 'components/ui/Input.tsx')),
              ...load(path.join(root, 'components/ui/Button.tsx')),
              ...load(path.join(root, 'components/ui/State.tsx')),
              useToast: () => ({ error: () => {} }),
            }
          const file = [resolved, resolved + '.tsx', resolved + '.ts'].find((item) => fs.existsSync(item))
          return load(file)
        },
      },
      { filename },
    )
    return exports
  }
  LoginPage = load(path.join(root, 'features/auth/LoginPage.tsx')).LoginPage
  const { AuthLayout } = load(path.join(root, 'layouts/AuthLayout.tsx'))
  return renderToStaticMarkup(React.createElement(AuthLayout))
}

test('login preserva formulário convencional, rótulos e ação principal sem tabela nem paginação', () => {
  const html = renderAccess()
  assert.match(html, /class="auth-panel"/)
  assert.match(html, /class="auth-aside"/)
  assert.match(html, /Bem-vindo de volta/)
  assert.match(html, /Organize\. Colabore\./)
  assert.match(html, /alt="Brasão do Athletix"/)
  assert.match(html, /E-mail institucional<\/span><input[^>]*type="email"/)
  assert.match(html, /Senha<\/span><input[^>]*type="password"/)
  assert.match(html, /autoComplete="current-password"/)
  assert.match(html, /href="\/esqueci-senha"/)
  assert.match(html, /type="submit"[^>]*>Entrar<\/button>/)
  assert.ok(html.indexOf('name="email"') < html.indexOf('name="senha"'))
  assert.ok(html.indexOf('name="senha"') < html.indexOf('Esqueci minha senha'))
  assert.ok(html.indexOf('Esqueci minha senha') < html.indexOf('>Entrar</button>'))
  assert.doesNotMatch(html, /<table|field-table|table-scroll|Campos do formulário|Preenchimento|paginas-controles/)
})

test('login mantém o estado de verificação da sessão sem exibir campos prematuramente', () => {
  const html = renderAccess(true)
  assert.match(html, /Verificando sua sessão/)
  assert.doesNotMatch(html, /<form|<table/)
})
