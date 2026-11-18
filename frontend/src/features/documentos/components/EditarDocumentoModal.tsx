import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal, Select } from '../../../components/ui'
import { categoriaDocumentoLabels } from '../../../lib/labels'
import type { Documento } from '../api'
import { documentoSchema, type DocumentoValues } from '../schemas/documento'
import { categoriasDocumento } from '../types'

export function EditarDocumentoModal({
  documento,
  aoFechar,
  aoSalvar,
  aoSalvo,
}: {
  documento: Documento
  aoFechar: () => void
  aoSalvar: (documento: Documento, valores: DocumentoValues) => Promise<boolean>
  aoSalvo: () => void
}) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<DocumentoValues>({
    resolver: zodResolver(documentoSchema),
    defaultValues: {
      titulo: documento.titulo,
      categoria: documento.categoria,
      descricao: documento.descricao ?? '',
      visibilidade: documento.visibilidade,
      gestaoOrigemId: documento.gestaoOrigemId ? String(documento.gestaoOrigemId) : '',
    },
  })
  const salvar = async (valores: DocumentoValues) => {
    if (await aoSalvar(documento, valores)) aoSalvo()
  }
  return (
    <Modal
      open
      title="Editar metadados"
      onClose={aoFechar}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={aoFechar}>
            Cancelar
          </Button>
          <Button type="submit" form="form-metadata" loading={isSubmitting}>
            Salvar
          </Button>
        </>
      }
    >
      <form id="form-metadata" onSubmit={handleSubmit(salvar)} noValidate>
        <div className="form-grid">
          <Input label="Título" maxLength={200} error={errors.titulo?.message} {...register('titulo')} />
          <Select label="Categoria" error={errors.categoria?.message} {...register('categoria')}>
            {categoriasDocumento.map((categoria) => (
              <option key={categoria} value={categoria}>
                {categoriaDocumentoLabels[categoria]}
              </option>
            ))}
          </Select>
          <Input label="Descrição" maxLength={2000} error={errors.descricao?.message} {...register('descricao')} />
          <Select label="Visibilidade" error={errors.visibilidade?.message} {...register('visibilidade')}>
            <option value="MEMBROS">Membros</option>
            <option value="DIRETORIA">Diretoria</option>
          </Select>
          <Input
            label="ID da gestão de origem (opcional)"
            type="number"
            min="1"
            error={errors.gestaoOrigemId?.message}
            {...register('gestaoOrigemId')}
          />
        </div>
      </form>
    </Modal>
  )
}
