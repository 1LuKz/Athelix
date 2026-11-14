# Athletix

Plataforma local para gestão de atléticas acadêmicas, com isolamento por atlética, sessão HTTP e autorização baseada no perfil do cargo.

## Requisitos

- Java 17 ou superior (o projeto compila para Java 17)
- Node.js 20 ou superior e npm
- Nenhum banco externo, Docker ou serviço em nuvem é necessário para a execução básica

## Execução local

Em dois terminais, na raiz do projeto:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

No Windows, use `mvnw.cmd` no lugar de `./mvnw`.

```bash
cd frontend
npm install
npm run dev
```

Para gerar a versão de produção do frontend:

```bash
cd frontend
npm run build
```

Também é possível iniciar `AthletixApplication` pela IDE, com o perfil ativo `dev`. A API fica em `http://localhost:8080/api/v1`; o frontend local fica em `http://localhost:5173` e encaminha chamadas `/api` para o backend.

Para validar o backend antes de entregar:

```bash
cd backend
./mvnw test
```

## Interfaces e contratos

- Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
- Especificação gerada: `http://localhost:8080/api/v1/v3/api-docs`
- Contrato versionado e revisado do projeto: [docs/api/openapi.yaml](docs/api/openapi.yaml)
- Regras de negócio: [docs/regras-de-negocio.md](docs/regras-de-negocio.md)
- Convenções e detalhes dos endpoints: [docs/api/README.md](docs/api/README.md)

Antes de uma chamada mutável, obtenha o CSRF em `GET /api/v1/auth/csrf` e envie o token no cabeçalho indicado pela resposta. O Swagger é uma ferramenta de consulta; as operações protegidas continuam exigindo sessão e CSRF.

## Dados locais e demonstração

O H2 é exclusivamente em memória. Seus dados são perdidos ao encerrar a aplicação. A cada nova execução com perfil `dev`, o inicializador recria o cenário de demonstração com dados sintéticos e datas relativas ao dia atual.

No perfil `dev`, a carga recria um cenário sintético completo em cada inicialização. As senhas abaixo existem somente nesse perfil e são persistidas apenas como hash BCrypt:

- `admin@athletix.local` / `Athletix@2026` — administrador
- `financeiro@athletix.local` / `Financeiro@2026` — financeiro
- `gestor@athletix.local` / `Gestor@2026` — gestor de eventos
- `membro@athletix.local` / `Membro@2026` — membro
- `admin@saude.athletix.local` / `Saude@2026` — administrador da segunda atlética, para validar isolamento

O cenário inclui duas atléticas, vínculos ativos/pendentes/inativos, gestão atual e histórica, eventos, tarefas, financeiro, patrimônio, inventário, documentos sintéticos e auditoria. As datas são calculadas a partir do dia de execução.

Antes de chamadas mutáveis da API, obtenha o CSRF em `GET /api/v1/auth/csrf` e envie o token no cabeçalho informado. O Vite já encaminha `/api` para o backend local.

## E-mail de desenvolvimento

Convites e recuperação usam SMTP real; a API não devolve links secretos. Configure uma caixa SMTP de desenvolvimento (por exemplo, Mailtrap) por variáveis de ambiente antes de testar esses fluxos:

```bash
export ATHLETIX_SMTP_HOST=...
export ATHLETIX_SMTP_PORT=587
export ATHLETIX_SMTP_USERNAME=...
export ATHLETIX_SMTP_PASSWORD=...
export ATHLETIX_MAIL_FROM=no-reply@athletix.local
export ATHLETIX_FRONTEND_BASE_URL=http://localhost:5173
```

Os mesmos valores podem ser definidos pela configuração da IDE. Sem SMTP configurado, a aplicação não afirma que houve envio e não expõe links ou tokens secretos.

## Segurança em uma demonstração pública

A execução local usa HTTP e, por isso, `ATHLETIX_COOKIE_SECURE=false`. Caso o grupo publique temporariamente uma demonstração em HTTPS, defina `ATHLETIX_COOKIE_SECURE=true`, use um certificado válido e mantenha `SameSite=Lax` e `HttpOnly`. Não exponha o console H2 nem as contas de demonstração fora de um ambiente controlado.

## Desempenho e privacidade

As listagens seguem paginação estável (`items`, `page`, `size`, `totalItems`, `totalPages`) e ordenações explicitamente permitidas. Resumos devem ser obtidos por agregações/projeções no banco, e não pela carga de coleções completas. BLOBs são carregados somente nas rotas de conteúdo autorizado; listagens retornam apenas metadados. DTOs não expõem senhas, tokens, BLOBs e contatos fora das capacidades autorizadas.

Finalidades de tratamento: administrar vínculos e acessos, organizar atividades, registrar obrigações financeiras e patrimoniais, disponibilizar documentos institucionais e manter auditoria. Perfis autorizados e visibilidades são definidos nas regras de negócio. Para uma implantação real, o grupo responsável deve formalizar a base legal, o responsável pelo tratamento, canal de solicitação e prazos de retenção antes de inserir dados pessoais reais; a carga `dev` não contém dados pessoais reais.

## Organização

- `docs/`: regras do negocio e documentacao da API
- `backend/`: API e regras de negocio
- `frontend/`: interface web
