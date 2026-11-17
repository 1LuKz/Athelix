import type { AjudaModulo } from './types'

export const ajudaPorModulo: AjudaModulo[] = [
  {
    id: 'primeiro-acesso',
    titulo: 'Primeiro acesso',
    resumo: 'Entre com a conta convidada e selecione sua atlética.',
    passos: [
      'Abra o convite recebido.',
      'Defina uma senha segura.',
      'Entre e escolha a atlética quando houver mais de um vínculo.',
    ],
  },
  {
    id: 'convite',
    titulo: 'Convites',
    resumo: 'Convites vinculam uma pessoa a uma atlética; não alteram senhas já existentes.',
    passos: [
      'A administração cadastra o vínculo.',
      'A pessoa recebe o link de definição de senha quando necessário.',
      'O vínculo precisa ser aprovado para liberar o acesso.',
    ],
  },
  {
    id: 'membros',
    titulo: 'Cadastro de membros',
    resumo: 'Mantenha cargo, departamento e situação do vínculo atualizados.',
    passos: [
      'Cadastre os dados acadêmicos.',
      'Selecione cargo e departamento da própria atlética.',
      'Aprove o vínculo antes de liberar acesso.',
    ],
  },
  {
    id: 'eventos',
    titulo: 'Organização de eventos',
    resumo: 'Planeje o evento, defina responsáveis e acompanhe tarefas.',
    passos: [
      'Cadastre data, local e orçamento.',
      'Designe as áreas responsáveis.',
      'Use cronograma, participantes e checklist para acompanhar a execução.',
    ],
  },
  {
    id: 'financeiro',
    titulo: 'Aprovação e baixa financeira',
    resumo: 'Aprovação e liquidação são operações diferentes.',
    passos: [
      'Registre a receita ou despesa.',
      'Aprove a despesa com pessoa diferente do autor.',
      'Faça a baixa somente na data efetiva do pagamento ou recebimento.',
    ],
  },
  {
    id: 'emprestimos',
    titulo: 'Empréstimos de patrimônio',
    resumo: 'A disponibilidade é calculada a partir de empréstimos e manutenção.',
    passos: [
      'Registre a retirada e o responsável.',
      'Registre devoluções, inclusive parciais.',
      'Envie item danificado para manutenção quando necessário.',
    ],
  },
  {
    id: 'documentos',
    titulo: 'Documentos',
    resumo: 'Documentos conservam versões; uma publicação não substitui o arquivo anterior.',
    passos: [
      'Envie PDF com título, categoria e visibilidade.',
      'Publique nova versão para qualquer alteração de conteúdo.',
      'Arquive para ocultar da lista corrente sem apagar o histórico.',
    ],
  },
  {
    id: 'gestao',
    titulo: 'Troca de gestão',
    resumo: 'Use a prévia antes de concluir a sucessão.',
    passos: [
      'Prepare a composição de destino.',
      'Resolva responsáveis e pendências apontadas na prévia.',
      'Conclua apenas depois de garantir um administrador ativo na nova gestão.',
    ],
  },
]
