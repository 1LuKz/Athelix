import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { Button, Input, useToast } from '../../components/ui'
import { authApi } from './api'
import { HttpError } from '../../lib/http'

const emailSchema = z.object({ email: z.string().trim().email('Informe um e-mail válido.') })
const passwordSchema = z.object({ novaSenha: z.string().min(10, 'A senha deve ter ao menos 10 caracteres.').max(64, 'A senha pode ter no máximo 64 caracteres.').refine(value => new TextEncoder().encode(value).length <= 72, 'A senha excede o limite aceito.'), confirmacao: z.string() }).refine(value => value.novaSenha === value.confirmacao, { path: ['confirmacao'], message: 'As senhas não coincidem.' })
type EmailValues = z.infer<typeof emailSchema>; type PasswordValues = z.infer<typeof passwordSchema>

export function EsqueciSenhaPage() {
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<EmailValues>({ resolver: zodResolver(emailSchema) }); const [message, setMessage] = useState<string | null>(null)
  const submit = async ({ email }: EmailValues) => { const response = await authApi.esqueciSenha(email); setMessage(response.message) }
  return <section className="auth-card password-card"><h1>Recuperar senha</h1><p>Informe seu e-mail institucional. Por segurança, a resposta será a mesma para qualquer endereço.</p>{message ? <SuccessMessage message={message} /> : <form onSubmit={handleSubmit(submit)} noValidate><Input label="E-mail institucional" type="email" autoComplete="email" error={errors.email?.message} {...register('email')} /><Button type="submit" loading={isSubmitting} className="login-submit">Enviar instruções</Button></form>}<Link className="forgot-link" to="/login">Voltar para entrar</Link></section>
}

export function RedefinirSenhaPage() { return <TokenPasswordPage kind="redefinir" /> }
export function DefinirSenhaPage() { return <TokenPasswordPage kind="definir" /> }

function TokenPasswordPage({ kind }: { kind: 'redefinir' | 'definir' }) {
  const [params] = useSearchParams(); const token = params.get('token'); const navigate = useNavigate(); const { success, error } = useToast(); const [invalid, setInvalid] = useState<string | null>(token ? null : 'Este link é inválido, expirou ou já foi utilizado.')
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<PasswordValues>({ resolver: zodResolver(passwordSchema), defaultValues: { novaSenha: '', confirmacao: '' } })
  const submit = async ({ novaSenha }: PasswordValues) => { if (!token) { setInvalid('Este link é inválido, expirou ou já foi utilizado.'); return } try { const response = kind === 'redefinir' ? await authApi.redefinirSenha(token, novaSenha) : await authApi.definirSenha(token, novaSenha); success(response.message); navigate('/login?senha=alterada', { replace: true }) } catch (cause) { const message = cause instanceof HttpError && cause.status === 400 ? 'Este link é inválido, expirou ou já foi utilizado.' : 'Não foi possível definir a senha. Tente novamente.'; setInvalid(message); error(message) } }
  const title = kind === 'redefinir' ? 'Redefinir senha' : 'Definir sua senha'
  return <section className="auth-card password-card"><h1>{title}</h1><p>Crie uma senha de 10 a 64 caracteres. Não reutilize senhas de outros serviços.</p>{invalid ? <><p className="form-error" role="alert">{invalid}</p><Link className="forgot-link" to="/esqueci-senha">Solicitar outro link</Link></> : <form onSubmit={handleSubmit(submit)} noValidate><Input label="Nova senha" type="password" autoComplete="new-password" hint="De 10 a 64 caracteres." error={errors.novaSenha?.message} {...register('novaSenha')} /><Input label="Confirmar nova senha" type="password" autoComplete="new-password" error={errors.confirmacao?.message} {...register('confirmacao')} /><Button type="submit" loading={isSubmitting} className="login-submit">{kind === 'redefinir' ? 'Redefinir senha' : 'Definir senha'}</Button></form>}<Link className="forgot-link" to="/login">Voltar para entrar</Link></section>
}

function SuccessMessage({ message }: { message: string }) { return <p className="success-message" role="status">{message}</p> }
