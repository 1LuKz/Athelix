import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import vm from 'node:vm'
import { createRequire } from 'node:module'
import { fileURLToPath, URL } from 'node:url'
import React from 'react'
import { renderToStaticMarkup } from 'react-dom/server'
import ts from 'typescript'

const require = createRequire(import.meta.url)
const root = fileURLToPath(new URL('../src/components/', import.meta.url))
const cache = new Map()
function load(filename) {
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
        if (!dependency.startsWith('.')) return require(dependency)
        return load(path.resolve(path.dirname(filename), dependency + '.tsx'))
      },
    },
    { filename },
  )
  return exports
}
const { FormFields, FormContent, FilterTable } = load(path.join(root, 'ui/FormFields.tsx'))
const { Input } = load(path.join(root, 'ui/Input.tsx'))
const { Button } = load(path.join(root, 'ui/Button.tsx'))

test('formulários preservam todos os campos, erros e rótulos dentro de uma tabela sem paginação', () => {
  const fields = Array.from({ length: 25 }, (_, index) =>
    React.createElement(Input, {
      key: index,
      label: `Campo ${index}`,
      name: `campo${index}`,
      error: index === 24 ? 'Corrija este campo' : undefined,
    }),
  )
  const html = renderToStaticMarkup(
    React.createElement(FormFields, null, React.createElement('div', { className: 'form-grid' }, fields)),
  )
  assert.equal((html.match(/<input /g) ?? []).length, 25)
  assert.equal((html.match(/scope="row"/g) ?? []).length, 25)
  assert.match(html, /aria-invalid="true"/)
  assert.match(html, /Corrija este campo/)
  assert.match(html, /role="region"/)
  assert.doesNotMatch(html, /Página |Próxima|Anterior/)
})

test('ações de salvar e navegação ficam fora da área rolável dos campos', () => {
  const html = renderToStaticMarkup(
    React.createElement(
      FormContent,
      null,
      React.createElement(Input, { label: 'Nome', name: 'nome' }),
      React.createElement(Button, { type: 'submit' }, 'Salvar'),
      React.createElement('a', { href: '/login' }, 'Voltar'),
    ),
  )
  const finalTabela = html.indexOf('</table>')
  assert.ok(html.indexOf('name="nome"') < finalTabela)
  assert.ok(html.indexOf('type="submit"') > finalTabela)
  assert.ok(html.indexOf('href="/login"') > finalTabela)
})

test('filtros continuam rotulados e disponíveis em uma linha de tabela', () => {
  const html = renderToStaticMarkup(
    React.createElement(
      FilterTable,
      null,
      React.createElement(Input, { label: 'Buscar membro', type: 'search', name: 'busca' }),
      React.createElement(Input, { label: 'Data', type: 'date', name: 'data' }),
    ),
  )
  assert.match(html, /scope="col">Buscar membro/)
  assert.match(html, /scope="col">Data/)
  assert.match(html, /type="search"/)
  assert.match(html, /type="date"/)
  assert.equal((html.match(/<label /g) ?? []).length, 2)
})
