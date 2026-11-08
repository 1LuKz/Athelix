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

## Organizacao

- `docs/`: regras do negocio e documentacao da API
- `backend/`: API e regras de negocio
- `frontend/`: interface web
