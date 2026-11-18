import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal, Select } from '../../../components/ui'
import { categoriaDocumentoLabels } from '../../../lib/labels'
import { uploadDocumentoSchema, type UploadDocumentoValues } from '../schemas/documento'
import { categoriasDocumento } from '../types'

export function UploadDocumentoModal({
  aoFechar,
  aoSalvar,
  aoSalvo,
}: {
  aoFechar: () => void
  aoSalvar: (valores: UploadDocumentoValues) => Promise<boolean>
  aoSalvo: () => void
}) {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<UploadDocumentoValues>({
    resolver: zodResolver(uploadDocumentoSchema),
    defaultValues: {
      titulo: '',
      categoria: 'OUTRO',
      visibilidade: 'MEMBROS',
      descricao: '',
      observacao: '',
      gestaoOrigemId: '',
    },
  })
  const salvar = async (valores: UploadDocumentoValues) => {
    if (await aoSalvar(valores)) aoSalvo()
  }
  return (
    <Modal
      open
      title="Enviar documento"
      onClose={aoFechar}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={aoFechar}>
            Cancelar
          </Button>
          <Button type="submit" form="form-documento" loading={isSubmitting}>
            Publicar documento
          </Button>
        </>
      }
    >
      <form id="form-documento" onSubmit={handleSubmit(salvar)} noValidate>
        <div className="form-grid">
          <Input label="Título" maxLength={200} error={errors.titulo?.message} {...register('titulo')} />
          <Select label="Categoria" error={errors.categoria?.message} {...register('categoria')}>
            {categoriasDocumento.map((categoria) => (
              <option key={categoria} value={categoria}>
                {categoriaDocumentoLabels[categoria]}
              </option>
            ))}
          </Select>
          <Select label="Visibilidade" error={errors.visibilidade?.message} {...register('visibilidade')}>
            <option value="MEMBROS">Membros</option>
            <option value="DIRETORIA">Diretoria</option>
          </Select>
          <Input label="Descrição" maxLength={2000} error={errors.descricao?.message} {...register('descricao')} />
          <Input
            label="Observação da versão"
            maxLength={1000}
            error={errors.observacao?.message}
            {...register('observacao')}
          />
          <Input
            label="Arquivo PDF"
            type="file"
            accept="application/pdf,.pdf"
            hint="PDF, tamanho máximo de 10 MiB."
            error={errors.arquivo?.message}
            {...register('arquivo')}
          />
        </div>
      </form>
    </Modal>
  )
}
