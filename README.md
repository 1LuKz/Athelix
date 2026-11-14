# Athletix

Plataforma para centralizar a gestao de atleticas academicas, com isolamento de dados por atletica e acesso baseado em cargos.

## Tecnologias

- Backend: Java 17, Spring Boot, Maven e H2 em memoria
- Frontend: React, TypeScript e Vite

## Como executar

Em dois terminais:

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

```bash
cd frontend
npm install
npm run dev
```

O backend inicia em `http://localhost:8080` e o frontend em `http://localhost:5173`. Os dados do H2 sao recriados a cada inicializacao.

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

## Organizacao

- `docs/`: regras do negocio e documentacao da API
- `backend/`: API e regras de negocio
- `frontend/`: interface web
