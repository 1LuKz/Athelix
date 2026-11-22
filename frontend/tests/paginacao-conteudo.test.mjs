import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import vm from 'node:vm'
import { URL } from 'node:url'
import ts from 'typescript'

const caminhoModulo = new URL('../src/lib/paginacaoConteudo.ts', import.meta.url)
const codigoModulo = ts.transpileModule(fs.readFileSync(caminhoModulo, 'utf8'), {
  compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2022 },
}).outputText
const modulo = { exports: {} }
vm.runInNewContext(codigoModulo, { exports: modulo.exports })
const { montarPaginasConteudo, paginaContemBloco } = modulo.exports

test('conteúdo vazio ou que cabe na janela usa uma única página', () => {
  for (const alturaConteudo of [0, 100, 400]) {
    const paginas = montarPaginasConteudo(alturaConteudo, 400, [])
    assert.equal(paginas.length, 1)
    assert.equal(paginas[0].inicio, 0)
    assert.equal(paginas[0].fim, alturaConteudo)
  }
})

test('linhas de tabela e campos são exibidos inteiros em pelo menos uma página', () => {
  const blocos = Array.from({ length: 23 }, (_, indice) => ({ inicio: indice * 48, fim: (indice + 1) * 48 }))
  const paginas = montarPaginasConteudo(1104, 310, blocos)

  for (const bloco of blocos) {
    assert.equal(paginas.some((pagina) => paginaContemBloco(pagina, bloco)), true)
  }
  assert.equal(paginas.at(-1).fim, 1104)
})

test('cartões lado a lado com alturas diferentes continuam acessíveis sem cortes', () => {
  const blocos = [
    { inicio: 0, fim: 280 },
    { inicio: 230, fim: 490 },
    { inicio: 470, fim: 720 },
  ]
  const paginas = montarPaginasConteudo(720, 320, blocos)

  for (const bloco of blocos) {
    assert.equal(paginas.some((pagina) => paginaContemBloco(pagina, bloco)), true)
  }
})

test('páginas cobrem todo o conteúdo, inclusive intervalos e blocos maiores que a janela', () => {
  const paginas = montarPaginasConteudo(1750, 200, [{ inicio: 650, fim: 1600 }])
  let fimCoberto = 0

  for (const pagina of paginas) {
    assert.ok(pagina.inicio <= fimCoberto)
    assert.ok(pagina.fim - pagina.inicio <= 200)
    fimCoberto = Math.max(fimCoberto, pagina.fim)
  }
  assert.equal(fimCoberto, 1750)
})

test('redimensionamento e zoom recalculam páginas mantendo todos os campos disponíveis', () => {
  const blocos = Array.from({ length: 12 }, (_, indice) => ({ inicio: indice * 75, fim: indice * 75 + 65 }))

  for (const alturaPagina of [150, 260, 450, 900]) {
    const paginas = montarPaginasConteudo(900, alturaPagina, blocos)
    for (const bloco of blocos) {
      assert.equal(paginas.some((pagina) => paginaContemBloco(pagina, bloco)), true)
    }
  }
})

test('medidas fracionárias não escondem campos que estão no limite de uma página', () => {
  const bloco = { inicio: 100.4, fim: 175.8 }
  const paginas = montarPaginasConteudo(250.5, 100.9, [bloco])

  assert.equal(paginas.some((pagina) => paginaContemBloco(pagina, bloco)), true)
  assert.equal(paginas.at(-1).fim, 251)
})

test('página não marca como visível um campo que está cortado ou fora da área útil', () => {
  const pagina = { inicio: 100, fim: 200 }
  assert.equal(paginaContemBloco(pagina, { inicio: 110, fim: 190 }), true)
  assert.equal(paginaContemBloco(pagina, { inicio: 90, fim: 120 }), false)
  assert.equal(paginaContemBloco(pagina, { inicio: 190, fim: 210 }), false)
})

test('continuação de uma tabela reserva espaço para repetir o cabeçalho acima das linhas', () => {
  const alturaCabecalho = 30
  const blocos = Array.from({ length: 20 }, (_, indice) => ({
    inicio: indice * 45,
    fim: alturaCabecalho + (indice + 1) * 45,
  }))
  const paginas = montarPaginasConteudo(930, 200, blocos)

  for (const bloco of blocos) {
    const pagina = paginas.find((candidata) => paginaContemBloco(candidata, bloco))
    assert.ok(pagina)
    assert.ok(bloco.inicio + alturaCabecalho - pagina.inicio >= alturaCabecalho - 1)
  }
})

test('última página começa no próximo bloco em vez de repetir quase toda a página anterior', () => {
  const paginas = montarPaginasConteudo(500, 400, [
    { inicio: 0, fim: 30 },
    { inicio: 350, fim: 500 },
  ])

  assert.equal(paginas.length, 2)
  assert.equal(paginas[1].inicio, 350)
  assert.equal(paginas[1].fim, 500)
})
