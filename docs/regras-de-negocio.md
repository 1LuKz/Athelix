# Regras de negocio

Este documento concentra as regras transversais do Athletix. Regras específicas serão acrescentadas junto com cada módulo.

## Princípios iniciais

- Cada dado pertence a uma atlética.
- Usuários só podem acessar recursos permitidos pelo seu cargo.
- O backend concentra validações, regras e cálculos.
- O frontend apresenta dados e solicita operações à API.

## Convenções técnicas obrigatórias

- Rotas públicas usam a base `/api/v1`.
- Os identificadores são `Long` gerados pelo banco. Entidades JPA nunca são retornadas diretamente.
- Cada recurso terá DTOs distintos de criação, alteração e resposta. DTOs de resposta não expõem senhas, tokens nem BLOBs em listagens.
- Enums são persistidos como texto com `@Enumerated(EnumType.STRING)`.
- Recursos modificáveis usam `@Version`. DTOs de alteração levam a versão lida e uma versão desatualizada retorna `409 CONCURRENT_MODIFICATION`.
- Registros históricos não recebem exclusão em cascata.
- Datas de criação e alteração usam `Instant`; vencimentos usam `LocalDate`; datas programadas de evento usam `OffsetDateTime` com o fuso da atlética.
- Valores monetários usam `BigDecimal`, `DECIMAL(19,2)` e arredondamento `HALF_UP`. Nos contratos HTTP, valores monetários são strings decimais, por exemplo `"1250.90"`.
- Aprovações financeiras e movimentações de estoque devem ser executadas em transações atômicas quando esses módulos forem implementados.

## Escopo por atlética

- Toda operação sobre dados de domínio usa a atlética presente em `ContextoAtletica`; um `atleticaId` recebido no corpo, query ou rota não autoriza acesso.
- Consultas, alterações, agregados, exportações, arquivos e rotinas agendadas resolvem recursos pelo par `(id do recurso, atleticaId do contexto)`.
- Um recurso de outra atlética recebe a mesma resposta `404` de um recurso inexistente.
- Todo relacionamento recebido deve ser resolvido no escopo atual antes de ser associado. Não basta confiar no ID enviado pelo navegador.
- O preenchimento do contexto a partir da sessão autenticada será implementado em B06. Até lá, apenas carga inicial e fluxos internos controlados podem defini-lo.

## Atléticas e domínios de e-mail

- A atlética possui nome obrigatório, sigla, instituição, fuso horário IANA, situação ativa e CNPJ opcional.
- Quando informado, o CNPJ é normalizado para 14 dígitos, validado e único. A ausência de CNPJ é válida.
- Um domínio de e-mail é normalizado para minúsculas e deve ser um domínio completo, como `engenharia.exemplo.edu.br`.
- Domínios são únicos por atlética e são comparados por igualdade exata; uma correspondência por sufixo não é autorização.
- No protótipo, atléticas são criadas somente pela carga inicial do perfil `dev`; não existe endpoint público de administração global.

## Paginação

- A página é indexada a partir de zero.
- O tamanho padrão é 20 e o máximo é 100.
- A resposta contém `items`, `page`, `size`, `totalItems` e `totalPages`.
- Cada endpoint declara explicitamente os campos aceitos por `sort=campo,direcao`. A ordenação sempre recebe `id` como critério final para manter estabilidade.

## Erros e status HTTP

Todos os erros usam os campos `status`, `code`, `message`, `fieldErrors`, `path` e `timestamp`, sem stack trace. Recursos inexistentes e recursos de outra atlética retornam a mesma resposta `404`.

| Situação | Status | Código |
| --- | ---: | --- |
| Entrada inválida ou regra de negócio | 400 | `INVALID_REQUEST` ou `BUSINESS_RULE_VIOLATION` |
| Sem sessão | 401 | `UNAUTHENTICATED` |
| Sem permissão | 403 | `FORBIDDEN` |
| Recurso inexistente ou de outra atlética | 404 | `RESOURCE_NOT_FOUND` |
| Conflito de dados | 409 | `RESOURCE_CONFLICT` |
| Conflito de versão | 409 | `CONCURRENT_MODIFICATION` |

Criações retornam `201`, consultas e alterações bem-sucedidas retornam `200` e operações sem conteúdo retornam `204`.
