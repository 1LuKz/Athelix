import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal, Select, Textarea, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { type Evento } from '../../eventos/api'
import type { Membro } from '../../membros/types'
import { tarefasApi, type Tarefa } from '../api'
import { tarefaSchema, type TarefaValues } from '../schemas/tarefa'

export function TarefaModal({
  tarefa,
  eventos,
  membros,
  departamentos,
  aoFechar,
  aoSalvar,
}: {
  tarefa?: Tarefa
  eventos: Evento[]
  membros: Membro[]
  departamentos: { id: number; nome: string }[]
  aoFechar: () => void
  aoSalvar: () => void | Promise<void>
}) {
  const toast = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<TarefaValues>({
    resolver: zodResolver(tarefaSchema),
    defaultValues: {
      eventoId: tarefa ? String(tarefa.eventoId) : '',
      titulo: tarefa?.titulo ?? '',
      descricao: tarefa?.descricao ?? '',
      responsavelMembroId: tarefa?.responsavelMembroId ? String(tarefa.responsavelMembroId) : '',
      departamentoId: tarefa?.departamentoId ? String(tarefa.departamentoId) : '',
      prioridade: tarefa?.prioridade ?? 'MEDIA',
      prazo: tarefa?.prazo ?? '',
    },
  })
  const salvar = async (valores: TarefaValues) => {
    try {
      const dados = {
        titulo: valores.titulo,
        descricao: valores.descricao,
        responsavelMembroId: valores.responsavelMembroId ? Number(valores.responsavelMembroId) : null,
        departamentoId: valores.departamentoId ? Number(valores.departamentoId) : null,
        prioridade: valores.prioridade,
        prazo: valores.prazo || null,
      }
      if (tarefa) await tarefasApi.update(tarefa.id, { ...dados, version: tarefa.version })
      else await tarefasApi.create({ ...dados, eventoId: Number(valores.eventoId) })
      toast.success(tarefa ? 'Tarefa atualizada.' : 'Tarefa criada em A Fazer.')
      await aoSalvar()
    } catch (causa) {
      toast.error(errorFeedback(causa))
    }
  }
  return (
    <Modal
      open
      title={tarefa ? 'Editar tarefa' : 'Nova tarefa'}
      onClose={aoFechar}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={aoFechar}>
            Cancelar
          </Button>
          <Button type="submit" form="form-tarefa" loading={isSubmitting}>
            Salvar tarefa
          </Button>
        </>
      }
    >
      <form id="form-tarefa" onSubmit={handleSubmit(salvar)} noValidate>
        <div className="form-grid">
          {!tarefa && (
            <Select label="Evento" error={errors.eventoId?.message} {...register('eventoId')}>
              <option value="">Selecione</option>
              {eventos.map((evento) => (
                <option key={evento.id} value={evento.id}>
                  {evento.nome}
                </option>
              ))}
            </Select>
          )}
          <Input label="Título" maxLength={160} error={errors.titulo?.message} {...register('titulo')} />
          <Textarea label="Descrição" maxLength={3000} error={errors.descricao?.message} {...register('descricao')} />
          <Select label="Responsável" error={errors.responsavelMembroId?.message} {...register('responsavelMembroId')}>
            <option value="">Sem responsável</option>
            {membros.map((membro) => (
              <option key={membro.id} value={membro.id}>
                {membro.nome}
              </option>
            ))}
          </Select>
          <Select label="Departamento" error={errors.departamentoId?.message} {...register('departamentoId')}>
            <option value="">Sem departamento</option>
            {departamentos.map((departamento) => (
              <option key={departamento.id} value={departamento.id}>
                {departamento.nome}
              </option>
            ))}
          </Select>
          <Select label="Prioridade" error={errors.prioridade?.message} {...register('prioridade')}>
            <option value="ALTA">Alta</option>
            <option value="MEDIA">Média</option>
            <option value="BAIXA">Baixa</option>
          </Select>
          <Input label="Prazo" type="date" error={errors.prazo?.message} {...register('prazo')} />
        </div>
      </form>
    </Modal>
  )
}
