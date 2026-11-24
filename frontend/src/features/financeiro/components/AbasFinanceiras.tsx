import { Tabs } from '../../../components/ui'

type AbasFinanceirasProps = {
  podeGerenciar: boolean
  abaSelecionada: string
  aoAlterarAba: (aba: string) => void
}

export function AbasFinanceiras({ podeGerenciar, abaSelecionada, aoAlterarAba }: AbasFinanceirasProps) {
  const abasGerenciais = [
    { id: 'lancamentos', label: 'Lançamentos' },
    { id: 'aprovacoes', label: 'Pendências de aprovação' },
  ]
  const abasFinanceiras = [
    { id: 'evolucao', label: 'Evolução mensal' },
    { id: 'contas', label: 'Contas a pagar e receber' },
    { id: 'relatorio', label: 'Prestação de contas' },
  ]

  return (
    <Tabs
      active={abaSelecionada}
      onChange={aoAlterarAba}
      tabs={podeGerenciar ? [...abasGerenciais, ...abasFinanceiras] : abasFinanceiras}
    />
  )
}
