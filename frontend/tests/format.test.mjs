import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import { URL } from 'node:url'
import vm from 'node:vm'
import ts from 'typescript'

function load(file) {
  const code = ts.transpileModule(fs.readFileSync(new URL(`../src/lib/${file}.ts`, import.meta.url), 'utf8'), {
    compilerOptions: { module: ts.ModuleKind.CommonJS },
  }).outputText
  const exports = {}
  vm.runInNewContext(code, { exports, Intl, Date, Number })
  return exports
}
const format = load('format'),
  labels = load('labels')
test('datas civis não mudam conforme o fuso do navegador', () => {
  assert.equal(format.formatDate('2026-09-30'), '30/09/2026')
  assert.equal(format.formatDate(null), '—')
  assert.equal(format.formatMonth('2026-09'), '09/2026')
})
test('hoje usa a data civil no fuso da atlética, inclusive após a virada do dia em UTC', () => {
  const agora = new Date('2026-07-10T01:30:00Z')

  assert.equal(format.hojeNoFuso('America/Sao_Paulo', agora), '2026-07-09')
  assert.equal(format.hojeNoFuso('Asia/Tokyo', agora), '2026-07-10')
  assert.equal(format.hojeNoFuso('fuso-invalido', agora), '2026-07-09')
  assert.equal(agora.toISOString(), '2026-07-10T01:30:00.000Z')
})
test('deslocamento de mês preserva o calendário nos dias 29 a 31 e nas viradas de ano', () => {
  const cenarios = [
    ['2026-12-31T15:00:00Z', -1, '2026-11'],
    ['2026-03-31T15:00:00Z', -1, '2026-02'],
    ['2026-03-30T15:00:00Z', -1, '2026-02'],
    ['2026-03-29T15:00:00Z', -1, '2026-02'],
    ['2026-01-31T15:00:00Z', -5, '2025-08'],
    ['2026-01-31T15:00:00Z', 0, '2026-01'],
    ['2026-12-31T15:00:00Z', 1, '2027-01'],
    ['2026-01-31T15:00:00Z', -17, '2024-08'],
  ]

  for (const [instante, deslocamento, mesEsperado] of cenarios) {
    const agora = new Date(instante)

    assert.equal(format.mesComDeslocamentoNoFuso(deslocamento, 'America/Sao_Paulo', agora), mesEsperado)
    assert.equal(agora.getTime(), new Date(instante).getTime())
  }
})
test('mês atual e início dos últimos seis meses respeitam a virada de mês no fuso da atlética', () => {
  const agora = new Date('2026-08-01T01:00:00Z')

  assert.equal(format.hojeNoFuso('America/Sao_Paulo', agora), '2026-07-31')
  assert.equal(format.mesComDeslocamentoNoFuso(0, 'America/Sao_Paulo', agora), '2026-07')
  assert.equal(format.mesComDeslocamentoNoFuso(-5, 'America/Sao_Paulo', agora), '2026-02')
  assert.equal(format.mesComDeslocamentoNoFuso(0, 'Asia/Tokyo', agora), '2026-08')
  assert.equal(format.mesComDeslocamentoNoFuso(0, 'fuso-invalido', agora), '2026-07')
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
test('variações percentuais usam sinal e formatação brasileira', () => {
  assert.equal(format.formatVariacaoPercentual(null), 'Sem comparação')
  assert.equal(format.formatVariacaoPercentual('8.5'), '+8,5%')
  assert.equal(format.formatVariacaoPercentual('-3.2'), '-3,2%')
  assert.equal(format.formatVariacaoPercentual('0.0'), '0%')
})
test('datetime-local usa o fuso da atlética, não o fuso do navegador', () => {
  const horarioLocal = '2026-07-10T09:30'
  const fusoDaAtletica = 'America/Sao_Paulo'

  assert.equal(format.toOffsetDateTime(horarioLocal, fusoDaAtletica), '2026-07-10T09:30:00-03:00')
  assert.equal(format.deLocalInputValue('2026-07-10T12:30:00Z', fusoDaAtletica), horarioLocal)
  assert.equal(format.toOffsetDateTime(horarioLocal, 'Asia/Tokyo'), '2026-07-10T09:30:00+09:00')
})

test('editar e salvar sem alterações preserva o instante em São Paulo, inclusive no horário de verão', () => {
  const fusoDaAtletica = 'America/Sao_Paulo'
  const cenarios = [
    {
      inicio: '2026-07-10T17:00:00.000Z',
      fim: '2026-07-10T18:30:00.000Z',
      inicioLocal: '2026-07-10T14:00',
      fimLocal: '2026-07-10T15:30',
      deslocamento: '-03:00',
    },
    {
      inicio: '2018-12-10T16:00:00.000Z',
      fim: '2018-12-10T17:30:00.000Z',
      inicioLocal: '2018-12-10T14:00',
      fimLocal: '2018-12-10T15:30',
      deslocamento: '-02:00',
    },
    {
      inicio: '2026-07-11T02:30:00.000Z',
      fim: '2026-07-11T03:30:00.000Z',
      inicioLocal: '2026-07-10T23:30',
      fimLocal: '2026-07-11T00:30',
      deslocamento: '-03:00',
    },
  ]

  for (const cenario of cenarios) {
    for (const campo of ['inicio', 'fim']) {
      let instanteAtual = cenario[campo]

      for (let i = 0; i < 3; i++) {
        const valorLocal = format.deLocalInputValue(instanteAtual, fusoDaAtletica)
        const valorEnviado = format.toOffsetDateTime(valorLocal, fusoDaAtletica)

        assert.equal(valorLocal, cenario[`${campo}Local`])
        assert.equal(valorEnviado, `${valorLocal}:00${cenario.deslocamento}`)
        assert.equal(new Date(valorEnviado).toISOString(), cenario[campo])
        instanteAtual = valorEnviado
      }
    }
  }
})
