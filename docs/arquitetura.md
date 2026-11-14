# Arquitetura do Athletix

O Athletix usa uma arquitetura modular orientada a domínio. Cada módulo é dono das suas regras, persistência, contratos e pontos de entrada HTTP. A organização foi consolidada a partir do padrão de separação de responsabilidades do projeto de referência, preservando a implementação funcional existente.

## Backend

O código em `backend/src/main/java/br/com/athletix` é dividido por contexto de negócio: `membro`, `evento`, `financeiro`, `patrimonio`, `documento`, `gestao` e demais módulos.

Em cada módulo, as responsabilidades são separadas em:

- `controller`: entrada HTTP e composição das respostas;
- `dto`: contratos de entrada e saída, sem expor entidades;
- `entity`: modelo persistido e regras locais da entidade;
- `repository`: acesso a dados;
- `service`: regras de negócio, autorização de domínio e transações.

Subdomínios com ciclo próprio, como participantes e cronograma de eventos, empréstimos e inventário de patrimônio, e transição de gestão, permanecem aninhados em seu módulo pai.

O módulo `shared` concentra convenções transversais: erros padronizados, paginação, moeda, normalização, tempo, persistência e isolamento por atlética. `config` contém configuração de execução e inicialização; `auth` e `security` contêm autenticação, sessão e capacidades. Esses módulos não devem acumular regras específicas de um domínio.

Dependências seguem o sentido `controller → service → repository/entity`. Um módulo pode consultar outro por serviço público para validar referências da mesma atlética; não deve acessar repositórios internos de outro domínio diretamente.

## Frontend

O frontend organiza cada área em `src/features/<dominio>`, com páginas, cliente da API, tipos, componentes e validações quando necessários. Componentes compartilhados ficam em `src/components`, infraestrutura de HTTP e cache em `src/lib`, rotas em `src/app` e composição visual em `src/layouts`.

As páginas de domínio são carregadas sob demanda. As consultas de dados usam chaves contendo a atlética ativa, evitando reaproveitamento de cache entre atléticas.

## Convenções de evolução

- Criar código novo no módulo de domínio responsável; não criar CRUD genérico para regras diferentes.
- Manter DTOs separados de entidades e nunca retornar credenciais, tokens ou BLOBs em listagens.
- Atualizar OpenAPI e regras de negócio ao expor um endpoint.
- Incluir testes no módulo correspondente, com cenários unitários para serviços e integração para APIs.
- Não versionar dependências instaladas, resultados de build, arquivos de IDE, caches, logs ou segredos locais.
