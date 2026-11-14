type Props = { onOpen: () => void }
export function BotaoAjuda({ onOpen }: Props) { return <button className="help-button" type="button" onClick={onOpen}>Ajuda e suporte</button> }
