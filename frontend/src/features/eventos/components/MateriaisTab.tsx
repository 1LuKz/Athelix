import { zodResolver } from '@hookform/resolvers/zod'
import { useForm, useWatch } from 'react-hook-form'
import { materialSchema, type MaterialValues } from '../schemas/material'
import { tipoMaterialLabels as typeLabels } from '../../../lib/labels'
import { useState } from 'react'
import {
  Button,
  Card,
  ConfirmDialog,
  EmptyState,
  Input,
  Modal,
  Select,
  Textarea,
  Table,
  useToast,
} from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { detalheEventoApi } from '../detalheApi'
import type { AnexoEvento } from '../types'

export function MateriaisTab({
  eventoId,
  anexos,
  eventoConcluido,
  podeGerenciar,
  onChanged,
}: {
  eventoId: string
  anexos?: AnexoEvento[]
  eventoConcluido: boolean
  podeGerenciar: boolean
  onChanged: () => void
}) {
  const [uploading, setUploading] = useState(false)
  const [removing, setRemoving] = useState<AnexoEvento | null>(null)
  const [removingFile, setRemovingFile] = useState(false)
  const { success, error } = useToast()
  const remove = async () => {
    if (!removing) return
    setRemovingFile(true)
    try {
      await detalheEventoApi.removerAnexo(eventoId, removing.id, removing.version)
      success('Material removido do evento.')
      setRemoving(null)
      onChanged()
    } catch (cause) {
      error(errorFeedback(cause))
    } finally {
      setRemovingFile(false)
    }
  }
  return (
    <Card
      title="Materiais de divulgação"
      action={
        podeGerenciar ? (
          <Button icon="upload_file" onClick={() => setUploading(true)}>
            Enviar material
          </Button>
        ) : undefined
      }
    >
      {anexos?.length ? (
        <Table caption="Materiais do evento" columns={['Arquivo', 'Tipo', 'Descrição', 'Ação']}>
          {anexos.map((anexo) => (
            <tr key={anexo.id}>
              <td>
                <a href={`/api/v1/arquivos/${anexo.arquivoId}/conteudo`} target="_blank" rel="noreferrer">
                  <strong>{anexo.titulo}</strong>
                </a>
              </td>
              <td>{typeLabels[anexo.tipoMaterial]}</td>
              <td>{anexo.descricao ?? '—'}</td>
              <td>
                {podeGerenciar && !eventoConcluido && (
                  <Button
                    variant="ghost"
                    icon="delete"
                    loading={removingFile && removing?.id === anexo.id}
                    onClick={() => setRemoving(anexo)}
                    iconOnly
                    aria-label="Remover material"
                  />
                )}
              </td>
            </tr>
          ))}
        </Table>
      ) : (
        <EmptyState title="Sem materiais" description="Envie artes, regulamentos ou outros arquivos do evento." />
      )}
      {eventoConcluido && podeGerenciar && (
        <p className="form-note">Materiais de eventos concluídos não podem ser removidos.</p>
      )}
      {uploading && (
        <UploadModal
          eventoId={eventoId}
          onClose={() => setUploading(false)}
          onSaved={() => {
            setUploading(false)
            onChanged()
          }}
        />
      )}
      <ConfirmDialog
        open={Boolean(removing)}
        title="Remover material"
        message="O vínculo será removido do evento; o arquivo protegido será preservado."
        confirmLabel="Remover"
        loading={removingFile}
        onClose={() => setRemoving(null)}
        onConfirm={() => void remove()}
      />
    </Card>
  )
}

function UploadModal({ eventoId, onClose, onSaved }: { eventoId: string; onClose: () => void; onSaved: () => void }) {
  const { success, error } = useToast()
  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<MaterialValues>({
    resolver: zodResolver(materialSchema),
    defaultValues: { titulo: '', descricao: '', tipo: 'OUTRO' },
  })
  const tipo = useWatch({ control, name: 'tipo' })
  const save = async (valores: MaterialValues) => {
    try {
      const form = new FormData()
      const file = valores.arquivo[0]
      form.append('arquivo', file)
      form.append('titulo', valores.titulo || file.name)
      if (valores.descricao.trim()) form.append('descricao', valores.descricao)
      form.append('tipoMaterial', valores.tipo)
      await detalheEventoApi.upload(eventoId, form)
      success('Material enviado.')
      onSaved()
    } catch (cause) {
      error(errorFeedback(cause))
    }
  }
  return (
    <Modal
      open
      title="Enviar material"
      onClose={onClose}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={onClose}>
            Cancelar
          </Button>
          <Button type="submit" form="form-material" loading={isSubmitting}>
            Enviar
          </Button>
        </>
      }
    >
      <form id="form-material" onSubmit={handleSubmit(save)} noValidate>
        <div className="form-grid">
          <Input
            label="Arquivo"
            type="file"
            accept={tipo === 'REGULAMENTO' ? 'application/pdf' : 'application/pdf,image/png,image/jpeg'}
            error={errors.arquivo?.message}
            {...register('arquivo')}
          />
          <Input label="Título" maxLength={160} error={errors.titulo?.message} {...register('titulo')} />
          <Select label="Tipo" error={errors.tipo?.message} {...register('tipo')}>
            {Object.entries(typeLabels).map(([value, label]) => (
              <option key={value} value={value}>
                {label}
              </option>
            ))}
          </Select>
        </div>
        <Textarea label="Descrição" maxLength={1000} error={errors.descricao?.message} {...register('descricao')} />
      </form>
    </Modal>
  )
}
