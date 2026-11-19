import { TituloPagina } from '../../../components/TituloPagina'
import { Card } from '../../../components/ui'
export function SuportePage() {
  return (
    <>
      <div className="page-title">
        <TituloPagina>
          <h1>Central de suporte</h1>
          <p className="page-subtitle">Orientação</p>
        </TituloPagina>
      </div>
      <section className="support-grid">
        <Card title="Primeiro acesso">
          <p>Entre com o e-mail institucional e defina sua senha pelo convite recebido.</p>
        </Card>
        <Card title="Convites e senha">
          <p>Use “Esqueci minha senha” para solicitar um novo link. Links expirados não podem ser reutilizados.</p>
        </Card>
        <Card title="Operação">
          <p>Consulte as instruções contextuais pelo ícone de ajuda no cabeçalho de cada tela.</p>
        </Card>
        <Card title="Contato">
          <p>
            Os canais de suporte são definidos pela sua atlética. Procure a diretoria responsável caso precise de
            atendimento.
          </p>
        </Card>
      </section>
    </>
  )
}
