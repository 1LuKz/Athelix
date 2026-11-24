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
const root = fileURLToPath(new URL('../src/', import.meta.url))
const departamentos = [40, 25, 15, 10, 6, 4].map((percentual, index) => ({
  id: index + 1,
  nome: `Departamento ${index + 1}`,
  membrosAtivos: percentual,
  participacaoPercentual: percentual,
}))

function carregarDashboard(perfil, capacidades) {
  const consultas = []
  const cache = new Map()
  const dados = {
    'dashboard-resumo': {
      ativos: 100,
      pendentes: 2,
      inativos: 3,
      novosMembrosNoMes: 5,
      eventosAtivos: 4,
      eventosNaSemana: 1,
      saldoAtual: '1500.00',
      variacaoSaldoPercentual: '5.0',
      cadastrosPatrimonio: 8,
      unidadesPatrimonio: 50,
      percentualConferido: 75,
    },
    'dashboard-financeiro': {
      seisMeses: [{ mes: '2026-11', receitas: '2000.00', despesas: '500.00', saldo: '1500.00' }],
    },
    'dashboard-departamentos': departamentos,
  }
  function load(filename) {
    if (filename.endsWith('/features/auth/AuthSessionProvider.tsx'))
      return {
        useAuthSession: () => ({ state: { capacidades }, activeVinculo: { atleticaId: 1, perfilAcesso: perfil } }),
      }
    if (filename.endsWith('/components/TituloPagina.tsx'))
      return {
        TituloPagina: ({ children }) => React.createElement('header', null, children),
      }
    if (filename.endsWith('/features/dashboard/api.ts')) return { dashboardApi: {} }
    if (filename.endsWith('/lib/errorFeedback.ts')) return { errorFeedback: (error) => error?.message ?? 'Erro' }
    if (cache.has(filename)) return cache.get(filename)
    const exports = {}
    cache.set(filename, exports)
    const code = ts.transpileModule(fs.readFileSync(filename, 'utf8'), {
      compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022, jsx: ts.JsxEmit.ReactJSX },
    }).outputText
    vm.runInNewContext(
      code,
      {
        exports,
        require: (dependency) => {
          if (dependency === '@tanstack/react-query')
            return {
              useQuery: (options) => {
                consultas.push({ recurso: options.queryKey[2], enabled: options.enabled })
                return {
                  data: options.enabled ? dados[options.queryKey[2]] : undefined,
                  isLoading: false,
                  isError: false,
                  refetch: async () => {},
                }
              },
            }
          if (dependency === 'react-router-dom')
            return {
              Link: ({ to, children, ...props }) => React.createElement('a', { ...props, href: to }, children),
            }
          if (dependency === 'recharts')
            return new Proxy(
              {},
              {
                get: (_, name) =>
                  ['ResponsiveContainer', 'BarChart'].includes(name)
                    ? ({ children }) => React.createElement('div', null, children)
                    : () => null,
              },
            )
          if (!dependency.startsWith('.')) return require(dependency)
          const resolved = path.resolve(path.dirname(filename), dependency)
          const candidate = [resolved + '.ts', resolved + '.tsx', path.join(resolved, 'index.ts')].find((item) =>
            fs.existsSync(item),
          )
          if (!candidate) throw new Error(`Módulo não encontrado: ${dependency}`)
          return load(candidate)
        },
      },
      { filename },
    )
    return exports
  }
  return { load, consultas }
}

// Capacidades dos quatro perfis, conforme CapacidadeService no backend.
const perfis = [
  [
    'ADMINISTRADOR',
    [
      'CONSULTAR_MEMBROS',
      'CONSULTAR_DEPARTAMENTOS',
      'CONSULTAR_RESUMOS_FINANCEIROS',
      'GERENCIAR_EVENTOS_E_TAREFAS',
      'GERENCIAR_PATRIMONIO',
    ],
    true,
    true,
    true,
  ],
  ['FINANCEIRO', ['CONSULTAR_MEMBROS', 'CONSULTAR_DEPARTAMENTOS', 'CONSULTAR_RESUMOS_FINANCEIROS'], true, false, false],
  [
    'GESTOR',
    [
      'CONSULTAR_MEMBROS',
      'CONSULTAR_DEPARTAMENTOS',
      'CONSULTAR_RESUMOS_FINANCEIROS',
      'GERENCIAR_EVENTOS_DO_PROPRIO_ESCOPO',
      'GERENCIAR_PATRIMONIO_DO_PROPRIO_DEPARTAMENTO',
    ],
    true,
    true,
    true,
  ],
  ['MEMBRO', ['CONSULTAR_MEMBROS_DADOS_MINIMOS', 'CONSULTAR_RESUMOS_FINANCEIROS'], false, false, false],
]

for (const [perfil, capacidades, gerenciaPessoas, gerenciaEventos, gerenciaPatrimonio] of perfis) {
  test(`dashboard ${perfil} mostra somente o resumo e os destinos permitidos`, () => {
    const { load, consultas } = carregarDashboard(perfil, capacidades)
    const { DashboardPage } = load(path.join(root, 'features/dashboard/pages/DashboardPage.tsx'))
    const html = renderToStaticMarkup(React.createElement(DashboardPage))
    assert.match(html, /dashboard-view/)
    assert.match(html, /Receitas e despesas/)
    assert.equal(html.includes('Participação por departamento'), gerenciaPessoas)
    assert.equal(/href="\/membros\?/.test(html), gerenciaPessoas)
    assert.equal(/href="\/eventos\?/.test(html), gerenciaEventos)
    assert.equal(html.includes('href="/patrimonio"'), gerenciaPatrimonio)
    assert.doesNotMatch(html, /Próximos eventos|Tarefas pendentes|Últimas movimentações|paginas-controles|<details/)
    assert.deepEqual(
      consultas.filter((item) => item.enabled).map((item) => item.recurso),
      gerenciaPessoas
        ? ['dashboard-resumo', 'dashboard-financeiro', 'dashboard-departamentos']
        : ['dashboard-resumo', 'dashboard-financeiro'],
    )
  })
}

test('dashboard não consulta nem renderiza o painel financeiro sem a capacidade exigida', () => {
  const { load, consultas } = carregarDashboard('MEMBRO', ['CONSULTAR_MEMBROS_DADOS_MINIMOS'])
  const { DashboardPage } = load(path.join(root, 'features/dashboard/pages/DashboardPage.tsx'))
  const html = renderToStaticMarkup(React.createElement(DashboardPage))
  assert.doesNotMatch(html, /Receitas e despesas|Saldo atual|href="\/financeiro"/)
  assert.deepEqual(
    consultas.filter((item) => item.enabled).map((item) => item.recurso),
    ['dashboard-resumo'],
  )
})

test('resumo de departamentos cabe no limite sem perder membros nem alterar a fonte', () => {
  const { load } = carregarDashboard('ADMINISTRADOR', [])
  const { resumirDepartamentos } = load(path.join(root, 'features/dashboard/utils/dashboardView.ts'))
  const fonte = [...departamentos].reverse()
  const copia = JSON.stringify(fonte)
  for (const limite of [1, 2, 3, 5, 6, 20]) {
    const resumo = resumirDepartamentos(fonte, limite)
    assert.ok(resumo.length <= limite)
    assert.equal(
      resumo.reduce((total, item) => total + item.membrosAtivos, 0),
      100,
    )
    assert.equal(
      resumo.reduce((total, item) => total + item.participacaoPercentual, 0),
      100,
    )
  }
  assert.equal(JSON.stringify(fonte), copia)
  assert.equal(resumirDepartamentos(fonte, 1)[0].nome, 'Todos os departamentos')
  assert.equal(resumirDepartamentos([], 3).length, 0)
})
