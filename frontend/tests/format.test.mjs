import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { URL } from 'node:url'
import vm from 'node:vm'
import ts from 'typescript'

function load(file) {
  const code = ts.transpileModule(fs.readFileSync(new URL(`../src/lib/${file}.ts`, import.meta.url), 'utf8'), { compilerOptions: { module: ts.ModuleKind.CommonJS } }).outputText
  const exports = {}
  vm.runInNewContext(code, { exports, Intl, Date, Number })
  return exports
}
const format = load('format'), labels = load('labels')
test('datas civis não mudam conforme o fuso do navegador', () => {
  assert.equal(format.formatDate('2026-09-30'), '30/09/2026')
  assert.equal(format.formatDate(null), '—')
  assert.equal(format.formatMonth('2026-09'), '09/2026')
})
test('instantes usam fuso informado ou São Paulo', () => {
  const value = '2026-09-30T01:00:00Z'
  assert.match(format.formatDateTime(value), /29\/09\/2026.*22:00/)
  assert.match(format.formatDateTime(value, 'America/Manaus'), /29\/09\/2026.*21:00/)
  assert.match(format.formatDateTime(value, 'Asia/Tokyo'), /30\/09\/2026.*10:00/)
  assert.match(format.formatDateTime(value, 'fuso-invalido'), /29\/09\/2026.*22:00/)
})
test('valores monetários e enums têm rótulos em português', () => {
  assert.match(format.formatMoney('1234.50'), /1\.234,50/)
  assert.equal(labels.statusEventoLabels.INSCRICOES_ABERTAS, 'Inscrições abertas')
  assert.equal(labels.statusTarefaLabels.EM_ANDAMENTO, 'Em andamento')
  assert.equal(labels.prioridadeTarefaLabels.MEDIA, 'Média')
  assert.equal(labels.areaResponsabilidadeLabels.COORDENACAO_GERAL, 'Coordenação geral')
  assert.equal(labels.enumLabel(labels.situacaoDepartamentoLabels, 'EM_CONFIGURACAO'), 'Em configuração')
})
