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

## Como compactar o projeto para entrega

Na raiz do projeto, com `bash` e `zip` instalados, execute:

```bash
./scripts/empacotar.sh
```

O script gera `frontend.zip`, `backend.zip` e `docs.zip` em `entrega/` e imprime seus tamanhos.
Dependências, builds e metadados do macOS são excluídos. Evite o "Comprimir" do Finder, que cria entradas `__MACOSX`.
Quem recebe deve descompactar os arquivos e rodar `npm ci` na pasta `frontend` para instalar as dependências.

## Dados locais e contas de demonstração

O H2 usado no perfil `dev` é somente em memória. Os dados são perdidos quando o backend é encerrado e, na próxima inicialização, a base é recriada com a carga de demonstração sintética.

A carga completa é ativada por `athletix.demo.completa=true` no perfil `dev` e inicializada por
`DemoDataInitializer`. Para executar somente a carga mínima:

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.arguments=--athletix.demo.completa=false
```

As contas pendente e inativa não podem autenticar enquanto o status não permitir acesso.

| E-mail | Atlética | Perfil | Status | Senha | Carga |
| --- | --- | --- | --- | --- | --- |
| `admin@athletix.local` | Engenharia | Administrador | Ativo | `Athletix@2026` | Ambas |
| `financeiro@athletix.local` | Engenharia | Financeiro | Ativo | `Financeiro@2026` | Ambas |
| `gestor@athletix.local` | Engenharia | Gestor | Ativo | `Gestor@2026` | Completa |
| `membro@athletix.local` | Engenharia | Membro | Ativo | `Membro@2026` | Completa |
| `pendente@athletix.local` | Engenharia | Membro | Pendente | `Pendente@2026` | Completa |
| `inativo@athletix.local` | Engenharia | Membro | Inativo | `Inativo@2026` | Completa |
| `admin@saude.athletix.local` | Saúde | Administrador | Ativo | `Saude@2026` | Completa |

As senhas são exclusivas de demonstração e persistidas como hash. As datas são relativas ao momento da carga,
no fuso da atlética (`America/Sao_Paulo`). Arquivos e participantes externos são sintéticos.
A primeira atlética concentra o cenário abaixo; Saúde mantém apenas acesso e estrutura para demonstrar isolamento.

| Área | Dados da carga completa de Engenharia |
| --- | --- |
| Eventos | 4 eventos: planejado, inscrições abertas, em andamento e concluído |
| Cronograma | 6 atividades; no evento em andamento: 2 concluídas, 1 em andamento e 1 agendada |
| Responsáveis | 5 designações; coordenação geral, financeiro e comunicação no evento em andamento |
| Participações | 12 no evento em andamento, internas e externas, nos 4 status; 2 presentes no concluído |
| Materiais | 2 anexos sintéticos no evento em andamento: arte e regulamento em PDF |
| Tarefas | 22: 3 a fazer, 2 em andamento, 2 em revisão e 15 concluídas; prioridades variadas |
| Checklist | 14 de 18 tarefas concluídas no evento em andamento (78%); prazo hoje e tarefas atrasadas |
| Documentos | 5 categorias: estatuto, regimento, ata, contrato e outros; 7 versões, 2 em dois documentos |
| Patrimônio | 8 itens de 2 categorias, conservação e disponibilidade variadas |
| Movimentações | 2 empréstimos: 1 aberto e 1 devolvido; 1 devolução e 1 manutenção aberta |
| Inventário | 1 conferência aberta, com 8 itens e 2 contados; 1 divergência de quantidade |
| Financeiro | 12 lançamentos em 6 meses, receitas recebidas e despesas aprovadas e pagas |
| Contas e estorno | Despesa pendente, rejeitada, conta futura aprovada, conta vencida aprovada e 1 estorno |
| Notificações | 5 avisos: 3 de prazo próximo e 2 de atraso; o administrador recebe 3 deles |
| Perfil | 150 atividades auditadas pelos serviços: 137 do administrador e 13 do financeiro |
| Gestões | 1 anterior encerrada e 1 atual ativa; nenhuma transição criada |

O checklist usa as próprias tarefas: os 14 itens concluídos exigem mais que os 10 cards mínimos do quadro.
As notificações disponíveis no modelo são prazo próximo e atraso; o terceiro aviso corresponde a outra tarefa.
A carga mínima preserva as contagens e não inclui os dados operacionais da tabela.

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

Sem `ATHLETIX_SMTP_HOST` ou quando o provedor SMTP não confirma o envio, convites e recuperação de senha não são concluídos: a API responde que não conseguiu confirmar a entrega e nenhum token utilizável é persistido. A aplicação não simula envios, não expõe links nem tokens secretos pela API.

## Privacidade e retenção

Os dados pessoais são tratados para gerir vínculos e acessos, organizar atividades institucionais, controlar patrimônio e obrigações financeiras, disponibilizar documentos e manter auditoria da atlética. A carga `dev` contém somente identidades, arquivos e cenários sintéticos.

A política inicial é reter o histórico operacional e de auditoria enquanto houver obrigação institucional, financeira ou de prestação de contas. Antes de uma implantação com dados reais, a organização deve formalizar prazo de retenção, base legal, responsável pelo tratamento e processo de revisão ou eliminação dos dados pessoais.

## Documentação

- [Contrato OpenAPI](docs/api/openapi.yaml)
- [Convenções da API](docs/api/README.md)
- [Regras de negócio](docs/regras-de-negocio.md)
- [Guia de desenvolvimento](docs/athletix-guia-de-desenvolvimento.md)
- [Arquitetura](docs/arquitetura.md)
