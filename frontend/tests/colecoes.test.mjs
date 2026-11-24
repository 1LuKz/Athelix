import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'
import { URL } from 'node:url'
import ts from 'typescript'

const exports = {}
const source = fs.readFileSync(new URL('../src/lib/carregarColecao.ts', import.meta.url), 'utf8')
vm.runInNewContext(
  ts.transpileModule(source, {
    compilerOptions: {
      module: ts.ModuleKind.CommonJS,
      target: ts.ScriptTarget.ES2022,
    },
  }).outputText,
  { exports },
)
const { carregarColecao } = exports

function pagina(items, page, totalItems, totalPages) {
  return { items, page, size: 100, totalItems, totalPages, totalAtivos: 42 }
}

test('coleções completas preservam todos os registros e metadados além do primeiro lote', async () => {
  const fonte = Array.from({ length: 237 }, (_, id) => ({ id, nome: `Membro ${id}` }))
  const chamadas = []
  const resultado = await carregarColecao(async (page) => {
    chamadas.push(page)
    return pagina(fonte.slice(page * 100, (page + 1) * 100), page, fonte.length, 3)
  })
  assert.deepEqual(chamadas, [0, 1, 2])
  assert.deepEqual([...resultado.items], fonte)
  assert.equal(resultado.totalItems, fonte.length)
  assert.equal(resultado.totalAtivos, 42)
  assert.equal(resultado.totalPages, 1)
})

test('uma coleção vazia não dispara consultas extras', async () => {
  let chamadas = 0
  const resultado = await carregarColecao(async () => {
    chamadas++
    return pagina([], 0, 0, 0)
  })
  assert.equal(chamadas, 1)
  assert.equal(resultado.items.length, 0)
})

test('falha em um lote rejeita a consulta inteira em vez de apresentar uma lista incompleta', async () => {
  const erro = new Error('Não foi possível carregar o segundo lote')
  await assert.rejects(
    carregarColecao(async (page) => {
      if (page === 1) throw erro
      return pagina([{ id: 1 }], 0, 2, 2)
    }),
    (cause) => cause === erro,
  )
})

test('cancelar a consulta impede novas requisições e descarta o resultado parcial', async () => {
  const controller = new globalThis.AbortController()
  const chamadas = []
  await assert.rejects(
    carregarColecao(async (page) => {
      chamadas.push(page)
      controller.abort()
      return pagina([{ id: 1 }], page, 200, 2)
    }, controller.signal),
    { name: 'AbortError' },
  )
  assert.deepEqual(chamadas, [0])
})
