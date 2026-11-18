export type AbaPatrimonio = 'itens' | 'emprestimos' | 'manutencoes' | 'conferencias'
export type ModalPatrimonio = 'categoria' | 'item' | 'emprestimo' | 'manutencao' | 'conferencia' | null
export type FiltrosPatrimonio = { nome: string; categoriaId: string; conservacao: string; situacao: string }
