import { Children, Fragment, isValidElement, type ReactNode } from 'react'
import { Button } from './Button'
import { Table } from './Table'

type FieldProps = { label?: string; children?: ReactNode; className?: string; to?: string; type?: string }

function flattenFields(children: ReactNode): ReactNode[] {
  return Children.toArray(children).flatMap((child) => {
    if (
      isValidElement<FieldProps>(child) &&
      (child.type === Fragment || child.props.className?.split(' ').includes('form-grid'))
    ) {
      return flattenFields(child.props.children)
    }
    return [child]
  })
}

/** Campos e valores permanecem juntos em linhas; só a tabela pode rolar. */
export function FormFields({ children }: { children: ReactNode }) {
  return (
    <Table caption="Campos do formulário" columns={['Campo', 'Preenchimento']} className="field-table">
      {flattenFields(children).map((child, index) => {
        const field = isValidElement<FieldProps>(child) ? child : null
        const label = field?.props.label
        return label ? (
          <tr key={`${index}:${field.key ?? 'campo'}`}>
            <th scope="row">{label}</th>
            <td>{child}</td>
          </tr>
        ) : (
          <tr key={`${index}:${field?.key ?? 'informacao'}`}>
            <td colSpan={2}>{child}</td>
          </tr>
        )
      })}
    </Table>
  )
}

export function FormContent({ children }: { children: ReactNode }) {
  const nodes = Children.toArray(children)
  const isAction = (child: ReactNode) =>
    isValidElement<FieldProps>(child) &&
    (child.type === Button || child.type === 'button' || child.type === 'a' || Boolean(child.props.to))
  return (
    <>
      <FormFields>{nodes.filter((child) => !isAction(child))}</FormFields>
      {nodes.filter(isAction)}
    </>
  )
}

function fieldText(node: ReactNode): string {
  return Children.toArray(node)
    .map((child) =>
      typeof child === 'string' ? child : isValidElement<FieldProps>(child) ? fieldText(child.props.children) : '',
    )
    .join(' ')
    .trim()
}

export function FilterTable({ children }: { children: ReactNode }) {
  const fields = flattenFields(children)
  const labels = fields.map((field) =>
    isValidElement<FieldProps>(field) ? (field.props.label ?? (fieldText(field.props.children) || 'Opções')) : 'Opções',
  )
  return (
    <Table caption="Filtros da listagem" columns={labels} className="filter-table">
      <tr>
        {fields.map((field, index) => (
          <td key={index}>{field}</td>
        ))}
      </tr>
    </Table>
  )
}
