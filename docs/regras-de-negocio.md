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
- O filtro de sessão preenche `ContextoAtletica` somente a partir do vínculo ativo selecionado; `atleticaId` informado pelo navegador é ignorado para fins de autorização.

## Atléticas e domínios de e-mail

- A atlética possui nome obrigatório, sigla, instituição, fuso horário IANA, situação ativa e CNPJ opcional.
- Quando informado, o CNPJ é normalizado para 14 dígitos, validado e único. A ausência de CNPJ é válida.
- Um domínio de e-mail é normalizado para minúsculas e deve ser um domínio completo, como `engenharia.exemplo.edu.br`.
- Domínios são únicos por atlética e são comparados por igualdade exata; uma correspondência por sufixo não é autorização.
- No protótipo, atléticas são criadas somente pela carga inicial do perfil `dev`; não existe endpoint público de administração global.

## Cargos

- Um cargo pertence a uma única atlética, possui perfil de acesso e pode ser inativado sem apagar referências históricas.
- O nome do cargo é único na atlética desconsiderando maiúsculas, minúsculas e espaços nas extremidades.
- Apenas administradores podem criar ou alterar um cargo com perfil de acesso. A remoção do último administrador ativo é bloqueada.
- Os perfis disponíveis são `ADMINISTRADOR`, `FINANCEIRO`, `GESTOR` e `MEMBRO`.

## Departamentos

- Um departamento pertence a uma única atlética e tem situação `EM_CONFIGURACAO`, `ATIVO` ou `INATIVO`.
- O nome é único por atlética. O total de membros não é armazenado no departamento: será calculado a partir dos vínculos de membros na B05.
- Departamentos novos começam em `EM_CONFIGURACAO`. Não aparecem na estrutura ativa, mas ficam disponíveis à administração para concluir o cadastro do primeiro membro.
- A ativação exige responsável ativo, da mesma atlética e vinculado ao próprio departamento. A B05 substituirá o identificador opcional por relação íntegra com `Membro` e efetivará essa validação.
- A inativação só é permitida após transferir membros ativos e substituir responsabilidades. Antes de B05, a transição é bloqueada para não burlar essa regra.
- Cargos e departamentos não são apagados quando houver histórico referenciado.

## Usuários e membros

- `Usuario` é uma identidade global: e-mail é normalizado e único globalmente; telefone é opcional e normalizado para `+` seguido dos dígitos informados.
- Senhas são armazenadas apenas como hash BCrypt. O hash, senhas em texto e tokens nunca são retornados em DTOs ou listagens.
- Uma conta criada por convite permanece sem hash e sem credencial ativa até a definição da senha.
- `Membro` é o vínculo entre usuário e atlética. Um mesmo usuário pode ter um vínculo, cargo e status diferentes em cada atlética, mas só um vínculo por atlética.
- Todo novo membro começa como `PENDENTE`. A ativação exige atlética ativa, cargo ativo, departamento não inativo e credencial ativa com senha definida.
- A validação do primeiro membro pode ocorrer em um departamento `EM_CONFIGURACAO`, sem validador-membro. Nas demais ativações, um membro ativo da mesma atlética valida o vínculo.
- Acesso à atlética exige membro `ATIVO`, atlética ativa e credencial definida e ativa.
- Inativação preserva o histórico. Ela é bloqueada enquanto o membro for responsável por departamento; módulos futuros, como Eventos, registram pendências abertas pelo contrato de responsabilidades antes da conclusão.
- A inativação do último administrador ativo também é bloqueada.

## Autenticação, sessão e autorização

- Senhas usam BCrypt. A mesma política é aplicada na definição, alteração e recuperação: de 10 a 64 caracteres e no máximo 72 bytes UTF-8; a senha nunca é truncada silenciosamente.
- A sessão usa cookie `HttpOnly`, `SameSite=Lax`, expira após 30 minutos de inatividade e troca o identificador após login. Em implantação HTTPS, o cookie deve receber `Secure=true`.
- CSRF permanece ativo inclusive para login e logout. Primeiro obtenha `GET /api/v1/auth/csrf`; envie o token usando o cabeçalho retornado (por padrão, `X-XSRF-TOKEN`) em toda requisição mutável.
- `POST /auth/login` responde genericamente a credenciais incorretas e limita a demonstração a 5 falhas em uma janela de 15 minutos por combinação de e-mail normalizado e IP de origem, bloqueando-a por 15 minutos. O limitador é local e reinicia junto com a aplicação.
- Após login, a API expõe apenas os vínculos `ATIVO` com atlética e cargo ativos. Com um vínculo elegível ele é selecionado automaticamente; com vários, `selecaoNecessaria` obriga `POST /auth/selecionar-atletica` com um `membroId` pertencente à própria conta.
- A cada requisição de domínio, a API revalida credencial, vínculo, atlética e cargo. Inativar um vínculo, desativar credencial, atlética ou cargo impede o acesso sem exigir novo login.
- O nome livre do cargo não concede privilégios: as capacidades vêm exclusivamente de `perfilAcesso`. As capacidades retornadas em `/auth/me` são orientativas ao frontend; o backend continua sendo a fonte de autorização por operação e escopo.

| Operação | Administrador | Financeiro | Gestor | Membro |
| --- | --- | --- | --- | --- |
| Membros e departamentos | Total | Total | Total | Dados mínimos de membros |
| Membros, cargos e departamentos | Gerencia | Não | Não | Não |
| Eventos e tarefas | Gerencia | Se responsável | No próprio escopo | Apenas a própria tarefa |
| Financeiro | Gerencia | Gerencia | Consulta resumos | Consulta resumos |
| Patrimônio | Gerencia | Não | No próprio departamento | Não |
| Documentos `MEMBROS` | Sim | Sim | Sim | Sim, se ativo |
| Documentos `DIRETORIA` | Sim | Sim | Sim | Não |
| Publicar documentos / transferência de gestão | Sim | Não por padrão | Somente publicação no escopo futuro | Não |

Os resumos financeiros para associados não devem expor dados pessoais de contrapartes ou anexos restritos. As operações de eventos, patrimônio e documentos receberão a checagem contextual detalhada quando seus endpoints forem implementados.

## Convites e recuperação de senha

- `TokenCredencial` é de uso único e guarda somente o resumo SHA-256 de um segredo aleatório de 256 bits. O valor original aparece exclusivamente no link enviado por e-mail, nunca em DTO, log ou resposta HTTP.
- A finalidade `DEFINIR_SENHA` é associada ao `Membro` quando o fluxo for convite e vale 24 horas. A finalidade `RECUPERAR_SENHA` vale 30 minutos. Ambos registram `criadoEm`, `expiraEm` e `consumidoEm`.
- Um token inválido, expirado, usado ou de finalidade diferente é rejeitado. Ao consumir um token, os demais tokens ainda disponíveis da mesma conta e finalidade são invalidados na mesma transação.
- `POST /auth/esqueci-senha` sempre devolve a mesma mensagem. Para uma conta elegível, o token só é persistido se o SMTP aceitar o envio; caso a infraestrutura de e-mail falhe, a resposta não afirma que houve entrega e não deixa token utilizável no banco.
- `POST /auth/redefinir-senha` reaplica integralmente a política BCrypt e invalida sessões registradas anteriormente para a conta. `POST /auth/definir-senha` aceita apenas tokens de convite.
- O convite é emitido apenas para uma conta sem credencial definida. Se a conta já possui senha, o vínculo aprovado usa essa conta e nenhuma senha é substituída. O token não aprova nem ativa um vínculo por si só.
- Links usam somente `athletix.frontend-base-url`, definido no servidor; nenhum host enviado pelo navegador participa da composição do link.
- SMTP é real, via Spring Mail. Configure `ATHLETIX_SMTP_HOST`, `ATHLETIX_SMTP_PORT`, `ATHLETIX_SMTP_USERNAME`, `ATHLETIX_SMTP_PASSWORD`, `ATHLETIX_SMTP_AUTH`, `ATHLETIX_SMTP_STARTTLS` e `ATHLETIX_MAIL_FROM` no ambiente. Para desenvolvimento, pode-se usar uma caixa SMTP de teste visualizável no navegador, como Mailtrap, sem contêineres.

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
