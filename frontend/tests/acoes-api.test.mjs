import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { URL, URLSearchParams } from 'node:url'
import vm from 'node:vm'
import ts from 'typescript'

function adapter(feature, exportName) {
  const calls = []
  const code = ts.transpileModule(
    fs.readFileSync(new URL(`../src/features/${feature}/api.ts`, import.meta.url), 'utf8'),
    { compilerOptions: { module: ts.ModuleKind.CommonJS } },
  ).outputText
  const exports = {}
  vm.runInNewContext(code, {
    exports,
    require: (path) => {
      if (path === '../../lib/http')
        return {
          http: (url, options) => {
            calls.push({ url, ...options })
            return Promise.resolve({})
          },
        }
      throw new Error(`Dependência inesperada: ${path}`)
    },
    URLSearchParams,
  })
  return { api: exports[exportName], calls }
}

test('cargos usam payload de criação e versão na edição', async () => {
  const { api, calls } = adapter('membros', 'membrosApi')
  const fields = { nome: 'Tesoureiro', descricao: 'Finanças', perfilAcesso: 'FINANCEIRO', ativo: true }
  await api.criarCargo(fields)
  await api.atualizarCargo(2, { ...fields, version: 7 })
  assert.equal(calls[0].url, '/cargos')
  assert.equal(calls[0].method, 'POST')
  assert.equal(calls[1].url, '/cargos/2')
  assert.equal(calls[1].method, 'PUT')
  assert.equal(calls[1].body.version, 7)
})

test('categorias financeira e patrimonial enviam contratos exigidos', async () => {
  const financeiro = adapter('financeiro', 'financeiroApi'),
    patrimonio = adapter('patrimonio', 'patrimonioApi')
  await financeiro.api.criarCategoria({ nome: 'Patrocínio', tipoAplicavel: 'RECEITA', ativa: true })
  await patrimonio.api.criarCategoria({ nome: 'Material', ativa: true })
  assert.equal(financeiro.calls[0].url, '/financeiro/categorias')
  assert.equal(financeiro.calls[0].body.tipoAplicavel, 'RECEITA')
  assert.equal(patrimonio.calls[0].url, '/patrimonio/categorias')
  assert.equal(patrimonio.calls[0].body.ativa, true)
})

test('edições e arquivamento enviam versões e rotas corretas', async () => {
  const tarefas = adapter('tarefas', 'tarefasApi'),
    financeiro = adapter('financeiro', 'financeiroApi'),
    documentos = adapter('documentos', 'documentosApi')
  await tarefas.api.update(4, {
    titulo: 'Entrega',
    descricao: '',
    responsavelMembroId: null,
    departamentoId: 3,
    prioridade: 'ALTA',
    prazo: null,
    version: 6,
  })
  await financeiro.api.atualizar(5, {
    descricao: 'Conta',
    categoriaId: 2,
    valor: '20.00',
    dataCompetencia: '2026-11-01',
    version: 8,
  })
  await documentos.api.atualizar(9, {
    titulo: 'Ata',
    categoria: 'ATA',
    descricao: null,
    visibilidade: 'MEMBROS',
    gestaoOrigemId: null,
    arquivado: true,
    version: 2,
  })
  for (const [call, url, version] of [
    [tarefas.calls[0], '/tarefas/4', 6],
    [financeiro.calls[0], '/financeiro/lancamentos/5', 8],
    [documentos.calls[0], '/documentos/9', 2],
  ]) {
    assert.equal(call.url, url)
    assert.equal(call.method, 'PUT')
    assert.equal(call.body.version, version)
  }
  assert.equal(documentos.calls[0].body.arquivado, true)
  await documentos.api.listar({ incluirArquivados: true })
  assert.equal(documentos.calls[1].url, '/documentos?incluirArquivados=true')
})
