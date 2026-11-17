import { useLocation } from 'react-router-dom'
import { Button, Modal } from '../../components/ui'
const help: Record<string, { title: string; text: string }> = {
  '/dashboard': {
    title: 'Ajuda do painel',
    text: 'Acompanhe os indicadores e use os links dos cartões para abrir a listagem correspondente.',
  },
  '/membros': {
    title: 'Ajuda de membros',
    text: 'Use busca e filtros para localizar vínculos. Aprovar, inativar e alterar acessos exigem autorização administrativa.',
  },
  '/eventos': {
    title: 'Ajuda de eventos',
    text: 'Crie eventos e acompanhe seus detalhes, participantes, tarefas e materiais.',
  },
  '/tarefas': {
    title: 'Ajuda de tarefas',
    text: 'Mova tarefas pelo controle de cada cartão. A alteração só é confirmada depois da resposta do servidor.',
  },
  '/financeiro': {
    title: 'Ajuda financeira',
    text: 'Os saldos seguem o regime de caixa. Escolha explicitamente a base de data antes de interpretar os filtros.',
  },
  '/patrimonio': {
    title: 'Ajuda de patrimônio',
    text: 'A disponibilidade é calculada pelo servidor. Retiradas, devoluções e manutenção podem mudar o saldo do item.',
  },
  '/documentos': {
    title: 'Ajuda de documentos',
    text: 'Envie somente PDFs autorizados. Uma nova versão preserva todas as versões anteriores.',
  },
  '/perfil': {
    title: 'Ajuda do perfil',
    text: 'Dados pessoais podem ser atualizados; alterações de cargo ou departamento dependem da diretoria.',
  },
  '/gestoes': {
    title: 'Ajuda de gestões',
    text: 'Consultas históricas não restauram privilégios. Confira a prévia completa antes de concluir uma transferência.',
  },
}
export function HelpPanel({ open, onClose }: { open: boolean; onClose: () => void }) {
  const location = useLocation()
  const current = help[location.pathname] ?? {
    title: 'Central de ajuda',
    text: 'Escolha uma área no menu para ver instruções específicas. Se precisar, consulte o suporte da sua atlética.',
  }
  return (
    <Modal open={open} title={current.title} onClose={onClose} footer={<Button onClick={onClose}>Entendi</Button>}>
      <p>{current.text}</p>
      <p className="form-note">
        Atalhos: use Tab para navegar, Enter para acionar controles e Esc para fechar esta janela.
      </p>
    </Modal>
  )
}
