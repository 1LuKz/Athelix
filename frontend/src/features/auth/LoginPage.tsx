import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { Button, Input, LoadingState, useToast } from '../../components/ui'
import { useAuthSession } from './AuthSessionProvider'
import { HttpError } from '../../lib/http'

const schema = z.object({
  email: z.string().trim().email('Informe um e-mail institucional válido.'),
  senha: z.string().min(1, 'Informe sua senha.'),
})
type LoginValues = z.infer<typeof schema>

export function LoginPage() {
  const { state, loading, login } = useAuthSession()
  const navigate = useNavigate()
  const [params] = useSearchParams()
  const { error: toastError } = useToast()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setError,
  } = useForm<LoginValues>({ resolver: zodResolver(schema), defaultValues: { email: '', senha: '' } })
  if (loading) return <LoadingState label="Verificando sua sessão..." />
  if (state) return <Navigate to={state.selecaoNecessaria ? '/selecionar-atletica' : '/dashboard'} replace />
  const onSubmit = async (values: LoginValues) => {
    try {
      const next = await login(values)
      navigate(next.selecaoNecessaria ? '/selecionar-atletica' : (params.get('next') ?? '/dashboard'), {
        replace: true,
      })
    } catch (cause) {
      const message =
        cause instanceof HttpError && cause.status === 401
          ? 'E-mail ou senha inválidos.'
          : 'Não foi possível entrar agora. Tente novamente.'
      setError('root', { message })
      toastError(message)
    }
  }
  return (
    <section className="auth-card login-card">
      <h1>Bem-vindo de volta</h1>
      <p>Entre para acessar sua atlética.</p>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <Input
          label="E-mail institucional"
          type="email"
          autoComplete="email"
          placeholder="voce@atletica.edu.br"
          error={errors.email?.message}
          {...register('email')}
        />
        <Input
          label="Senha"
          type="password"
          autoComplete="current-password"
          error={errors.senha?.message}
          {...register('senha')}
        />
        {errors.root && (
          <p className="form-error" role="alert">
            {errors.root.message}
          </p>
        )}
        <Link className="forgot-link" to="/esqueci-senha">
          Esqueci minha senha
        </Link>
        <Button type="submit" loading={isSubmitting} className="login-submit">
          Entrar
        </Button>
      </form>
      <small className="auth-footer">© 2026 Athletix</small>
    </section>
  )
}
