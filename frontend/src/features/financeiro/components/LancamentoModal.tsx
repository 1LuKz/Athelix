import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Button, Input, Modal, Select, useToast } from '../../../components/ui'
import { errorFeedback } from '../../../lib/errorFeedback'
import { normalizeMoney } from '../../../lib/formSchemas'
import { useAtleticaTimeZone } from '../../auth/AuthSessionProvider'
import type { Lancamento, TipoLancamento } from '../api'
import { financeiroApi } from '../api'
import { useLancamentoFormQueries } from '../hooks/useLancamentoFormQueries'
import { lancamentoSchema, type LancamentoValues } from '../schemas/lancamento'
import { dataAtual } from '../utils/financeiro'

type LancamentoModalProps = {
  tipo: TipoLancamento
  lancamento?: Lancamento
  aoFechar: () => void
  aoSalvar: () => Promise<void>
}

export function LancamentoModal({ tipo, lancamento, aoFechar, aoSalvar }: LancamentoModalProps) {
  const fusoHorario = useAtleticaTimeZone() ?? 'America/Sao_Paulo'
  const toast = useToast()
  const { categorias, eventos } = useLancamentoFormQueries()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LancamentoValues>({
    resolver: zodResolver(lancamentoSchema),
    defaultValues: {
      descricao: lancamento?.descricao ?? '',
      categoriaId: lancamento ? String(lancamento.categoriaId) : '',
      valor: lancamento?.valor ?? '',
      dataCompetencia: lancamento?.dataCompetencia ?? dataAtual(fusoHorario),
      dataVencimento: lancamento?.dataVencimento ?? '',
      eventoId: lancamento?.eventoId ? String(lancamento.eventoId) : '',
    },
  })
  const categoriasValidas = (categorias.data ?? []).filter(
    (categoria) => categoria.ativa && (categoria.tipoAplicavel === 'AMBOS' || categoria.tipoAplicavel === tipo),
  )
  const salvarLancamento = async (valores: LancamentoValues) => {
    try {
      const dadosLancamento = {
        descricao: valores.descricao,
        categoriaId: Number(valores.categoriaId),
        valor: normalizeMoney(valores.valor)!,
        dataCompetencia: valores.dataCompetencia,
        dataVencimento: valores.dataVencimento,
        eventoId: valores.eventoId ? Number(valores.eventoId) : undefined,
      }
      if (lancamento) {
        await financeiroApi.atualizar(lancamento.id, { ...dadosLancamento, version: lancamento.version })
      } else {
        await financeiroApi.criar({ ...dadosLancamento, tipo })
      }
      toast.success(lancamento ? 'Lançamento atualizado.' : `${tipo === 'RECEITA' ? 'Receita' : 'Despesa'} criada.`)
      await aoSalvar()
    } catch (causa) {
      toast.error(errorFeedback(causa))
    }
  }

  return (
    <Modal
      open
      title={lancamento ? 'Editar lançamento' : `Nova ${tipo === 'RECEITA' ? 'receita' : 'despesa'}`}
      onClose={aoFechar}
      footer={
        <>
          <Button variant="secondary" disabled={isSubmitting} onClick={aoFechar}>
            Cancelar
          </Button>
          <Button type="submit" form="form-lancamento" loading={isSubmitting}>
            Salvar lançamento
          </Button>
        </>
      }
    >
      <form id="form-lancamento" onSubmit={handleSubmit(salvarLancamento)} noValidate>
        <div className="form-grid">
          <Input label="Descrição" maxLength={250} error={errors.descricao?.message} {...register('descricao')} />
          <Select label="Categoria" error={errors.categoriaId?.message} {...register('categoriaId')}>
            <option value="">Selecione</option>
            {categoriasValidas.map((categoria) => (
              <option key={categoria.id} value={categoria.id}>
                {categoria.nome}
              </option>
            ))}
          </Select>
          <Input
            label="Valor (R$)"
            inputMode="decimal"
            placeholder="1.250,90"
            error={errors.valor?.message}
            {...register('valor')}
          />
          <Input
            label="Data de competência"
            type="date"
            error={errors.dataCompetencia?.message}
            {...register('dataCompetencia')}
          />
          <Input
            label="Vencimento"
            type="date"
            error={errors.dataVencimento?.message}
            {...register('dataVencimento')}
          />
          <Select label="Evento (opcional)" error={errors.eventoId?.message} {...register('eventoId')}>
            <option value="">Sem evento</option>
            {eventos.data?.items.map((evento) => (
              <option key={evento.id} value={evento.id}>
                {evento.nome}
              </option>
            ))}
          </Select>
        </div>
      </form>
    </Modal>
  )
}
