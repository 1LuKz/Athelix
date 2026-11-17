import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'
import vm from 'node:vm'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import ts from 'typescript'
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../src')
const requireModule = createRequire(import.meta.url), cache = new Map()
function load(file) {
  file = path.resolve(root, file.endsWith('.ts') ? file : `${file}.ts`)
  if (cache.has(file)) return cache.get(file)
  const exports = {}; cache.set(file, exports)
  const code = ts.transpileModule(fs.readFileSync(file, 'utf8'), { compilerOptions: { module: ts.ModuleKind.CommonJS } }).outputText
  vm.runInNewContext(code, { exports, require: name => name.startsWith('.') ? load(path.relative(root, path.resolve(path.dirname(file), name))) : requireModule(name), Date, Number, RegExp })
  return exports
}
const { normalizeMoney } = load('lib/formSchemas')
const { tarefaSchema } = load('features/tarefas/schemas/tarefa')
const { eventoSchema } = load('features/eventos/schemas/evento')
const { lancamentoSchema } = load('features/financeiro/schemas/lancamento')
const { departamentoSchema } = load('features/departamentos/schemas/departamento')
const { membroSchema } = load('features/membros/schemas/membro')
const { itemSchema } = load('features/patrimonio/schemas/item')
const { documentoSchema, uploadDocumentoSchema } = load('features/documentos/schemas/documento')
test('tarefa exige evento positivo e respeita limites', () => {
  const v = { eventoId: '1', titulo: 'Reunião', descricao: '', responsavelMembroId: '', departamentoId: '', prioridade: 'MEDIA', prazo: '' }
  assert.equal(tarefaSchema.safeParse(v).success, true)
  for (const eventoId of ['', '0', '-1']) assert.equal(tarefaSchema.safeParse({ ...v, eventoId }).success, false)
  assert.equal(tarefaSchema.safeParse({ ...v, titulo: 'x'.repeat(161) }).success, false)
})
test('evento valida ordem, campos obrigatórios e valor', () => {
  const v = { nome: 'Evento', descricao: '', inicio: '2026-09-30T10:00', fim: '2026-09-30T11:00', local: 'Sede', cidade: '', uf: '', orcamento: '1.250,90', participantes: '' }
  assert.equal(eventoSchema.safeParse(v).success, true)
  assert.equal(eventoSchema.safeParse({ ...v, fim: '2026-09-30T09:00' }).success, false)
  assert.equal(eventoSchema.safeParse({ ...v, nome: '' }).success, false)
  assert.equal(eventoSchema.safeParse({ ...v, orcamento: '0' }).success, false)
})
test('moeda mantém centavos em entradas brasileiras e decimais', () => {
  for (const value of ['R$ 1.250,90', '1250,90', '1250.90']) assert.equal(normalizeMoney(value), '1250.90')
  assert.equal(normalizeMoney('1.250'), '1250.00')
  for (const value of ['', '-1', 'abc', '1,999', '999999999999999999.00']) assert.equal(normalizeMoney(value), null)
  const v = { descricao: 'Conta', categoriaId: '2', valor: 'R$ 1.250,90', dataCompetencia: '2026-09-30', dataVencimento: '2026-10-01', eventoId: '' }
  assert.equal(lancamentoSchema.safeParse(v).success, true)
  assert.equal(lancamentoSchema.safeParse({ ...v, categoriaId: '' }).success, false)
  assert.equal(lancamentoSchema.safeParse({ ...v, valor: '0' }).success, false)
})
test('demais domínios recusam dados obrigatórios inválidos', () => {
  assert.equal(departamentoSchema.safeParse({ nome: '', descricao: '', responsavel: '' }).success, false)
  assert.equal(membroSchema.safeParse({ nome: 'Ana', email: 'invalido', telefone: '', curso: 'Direito', periodoCurso: '1', cargoId: '1', departamentoId: '1', criando: true }).success, false)
  assert.equal(itemSchema.safeParse({ nome: 'Item', descricao: '', categoriaId: '1', quantidadeTotal: '0', conservacao: 'BOM', localizacao: 'Sede', departamentoResponsavelId: '1', valorEstimadoUnitario: '10', ativo: true, justificativaReducao: '' }).success, false)
  const doc = { titulo: 'Ata', categoria: 'ATA', descricao: '', visibilidade: 'MEMBROS', gestaoOrigemId: '' }
  assert.equal(documentoSchema.safeParse(doc).success, true)
  assert.equal(uploadDocumentoSchema.safeParse({ ...doc, observacao: '', arquivo: [] }).success, false)
})
