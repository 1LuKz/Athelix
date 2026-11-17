# Athletix

Plataforma acadêmica para a gestão de atléticas, com isolamento por atlética, autorização por perfil e sessão HTTP.

## Requisitos

- Java 17 ou superior
- Node.js 20 ou superior, com npm

Não é necessário instalar banco de dados, Docker ou serviço em nuvem para executar o projeto localmente.

## Executar o backend

Na raiz do projeto, execute:

```bash
cd backend && ./mvnw spring-boot:run
```

O perfil local ativo é `dev`. A API inicia em `http://localhost:8080/api/v1`.

- Swagger UI: `http://localhost:8080/api/v1/swagger-ui.html`
- Console H2: `http://localhost:8080/api/v1/h2-console`
- Especificação OpenAPI gerada: `http://localhost:8080/api/v1/v3/api-docs`

No console H2, use a URL JDBC `jdbc:h2:mem:athletix;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`, usuário `sa` e senha em branco.

## Executar o frontend

Em outro terminal, na raiz do projeto:

```bash
cd frontend && npm install && npm run dev
```

O Vite inicia em `http://localhost:5173`. Durante o desenvolvimento, o proxy encaminha as chamadas iniciadas por `/api` para o backend em `http://localhost:8080`.

## Build e testes

Backend:

```bash
cd backend
./mvnw test
./mvnw package
```

Frontend:

```bash
cd frontend
npx tsc -b
npx eslint .
npx vite build
```

Também é possível usar `npm run build`, que executa a verificação TypeScript e o build do Vite.

## Dados locais e contas de demonstração

O H2 usado no perfil `dev` é somente em memória. Os dados são perdidos quando o backend é encerrado e, na próxima inicialização, a base é recriada com a carga de demonstração sintética.

As contas abaixo são criadas por `InitialAtleticasConfiguration`. As contas pendente e inativa existem para testar os fluxos de vínculo, mas não podem autenticar até que seu status permita acesso.

| E-mail | Perfil | Status | Senha de demonstração |
| --- | --- | --- | --- |
| `admin@athletix.local` | Administrador | Ativo | `Athletix@2026` |
| `financeiro@athletix.local` | Financeiro | Ativo | `Financeiro@2026` |
| `gestor@athletix.local` | Gestor | Ativo | `Gestor@2026` |
| `membro@athletix.local` | Membro | Ativo | `Membro@2026` |
| `pendente@athletix.local` | Membro | Pendente | `Pendente@2026` |
| `inativo@athletix.local` | Membro | Inativo | `Inativo@2026` |
| `admin@saude.athletix.local` | Administrador | Ativo | `Saude@2026` |

As senhas são exclusivas da carga `dev` e são persistidas como hash. O cenário inclui duas atléticas, permitindo validar o isolamento de escopo; a segunda atlética foi reduzida intencionalmente.

## Configuração de e-mail

Convites e recuperação de senha usam SMTP real. Defina as variáveis de ambiente antes de testar esses fluxos:

```bash
export ATHLETIX_SMTP_HOST=sandbox.smtp.mailtrap.io
export ATHLETIX_SMTP_PORT=587
export ATHLETIX_SMTP_USERNAME=seu_usuario_mailtrap
export ATHLETIX_SMTP_PASSWORD=sua_senha_mailtrap
export ATHLETIX_MAIL_FROM=no-reply@athletix.local
export ATHLETIX_FRONTEND_BASE_URL=http://localhost:5173
```

O exemplo usa uma caixa de testes do Mailtrap; substitua host e credenciais pelos valores exibidos no inbox criado para o projeto. As configurações opcionais `ATHLETIX_SMTP_AUTH` e `ATHLETIX_SMTP_STARTTLS` têm valor padrão `true`.

Sem SMTP configurado, a aplicação não confirma entrega de mensagens e não expõe links ou tokens secretos pela API.

## Privacidade e retenção

Os dados pessoais são tratados para gerir vínculos e acessos, organizar atividades institucionais, controlar patrimônio e obrigações financeiras, disponibilizar documentos e manter auditoria da atlética. A carga `dev` contém somente identidades, arquivos e cenários sintéticos.

A política inicial é reter o histórico operacional e de auditoria enquanto houver obrigação institucional, financeira ou de prestação de contas. Antes de uma implantação com dados reais, a organização deve formalizar prazo de retenção, base legal, responsável pelo tratamento e processo de revisão ou eliminação dos dados pessoais.

## Documentação

- [Contrato OpenAPI](docs/api/openapi.yaml)
- [Convenções da API](docs/api/README.md)
- [Regras de negócio](docs/regras-de-negocio.md)
- [Guia de desenvolvimento](docs/athletix-guia-de-desenvolvimento.md)
- [Arquitetura](docs/arquitetura.md)
