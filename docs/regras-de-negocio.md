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
- A API de membros é paginada e pesquisa nome, curso e e-mail sem distinguir maiúsculas de minúsculas. Os filtros `departamentoId` e `status` são sempre combinados com a atlética selecionada.
- O cadastro administrativo recebe dados de identidade e de vínculo, mas fixa o status inicial como `PENDENTE`. Ao encontrar uma conta pelo e-mail normalizado, a operação só cria o vínculo e não altera nome, telefone, senha ou qualquer dado pessoal dessa conta.
- A alteração do vínculo recebe versão e pode trocar curso, período, cargo e departamento somente por recursos pertencentes à atlética atual. Não altera a identidade global do usuário.
- E-mail e telefone do diretório são dados de contato administrativos: só são retornados para administrador. Senhas, hashes, tokens e BLOBs não são retornados em nenhuma consulta.
- A exportação de membros é exclusiva de administrador, usa todos os registros filtrados no escopo atual, codificação UTF-8 e escape de CSV. Valores com aparência de fórmula de planilha recebem prefixo seguro antes da exportação.

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

## Perfil, histórico de atividades e solicitações de vínculo

- `AtividadeUsuario` é imutável e registra somente fatos produzidos pelo servidor. A autoria (usuário, membro, atlética) vem da sessão selecionada; data, autor e atlética recebidos pelo navegador são ignorados.
- As operações de banco registradas no histórico gravam a alteração e o evento de auditoria na mesma transação. Os resumos têm no máximo 500 caracteres e não contêm senhas, tokens, BLOBs ou conteúdo integral de documentos.
- Nesta etapa são auditadas criação, ativação e inativação de membro, atualização de vínculo, criação e alteração de cargo, atualização de perfil, alteração de senha e decisões de solicitação de vínculo. Eventos, tarefas, financeiro, patrimônio e documentos devem chamar o mesmo serviço ao serem implementados.
- O perfil permite alterar somente nome, telefone, curso e período do vínculo selecionado. O e-mail é somente leitura. Alterações verificam as versões de usuário e membro.
- A troca de senha exige a senha atual, reaplica a política BCrypt e encerra todas as outras sessões da conta. A sessão que realizou a troca permanece válida.
- Uma solicitação de alteração de vínculo nasce `PENDENTE`, guarda os IDs de cargo e departamento anteriores e não muda o vínculo imediatamente. Somente administrador pode aprová-la ou rejeitá-la.
- A aprovação exige que a solicitação e o membro estejam nas versões informadas, que o vínculo atual ainda corresponda ao estado registrado e que cargo e departamento pretendidos continuem elegíveis. A mudança do vínculo, a decisão e a atividade são atômicas.

## Gestões e histórico de vínculos

- Uma gestão pertence a uma única atlética, tem nome, início, fim previsto, status `PLANEJADA`, `ATIVA` ou `ENCERRADA` e controle otimista de versão. O início nunca pode ser posterior ao fim previsto.
- Existe no máximo uma gestão `ATIVA` por atlética. A ativação usa bloqueio pessimista da atlética dentro da transação, verifica o status e só então abre os vínculos vigentes dos membros ativos.
- `VinculoGestao` preserva o membro, a gestão, o cargo, o departamento, o início e o fim de cada período. Troca de cargo ou departamento encerra o período aberto e abre outro no mesmo instante; o passado não é sobrescrito.
- Ativação e inativação de membro abrem ou encerram, respectivamente, o período da gestão ativa. Se não houver gestão ativa, nenhuma linha artificial é criada.
- As atividades registradas com uma gestão ativa recebem seu `gestaoId`. Os módulos futuros devem guardar `gestaoOrigemId` nos recursos para os quais a origem é relevante; patrimônio e biblioteca documental permanecem da atlética, ainda que suas versões tenham origem em uma gestão.
- Eventos em andamento, tarefas abertas, empréstimos e obrigações financeiras pendentes podem atravessar uma troca de gestão. Mantêm a origem e recebem continuidade auditada, sem desaparecer das obrigações atuais. Consultar uma gestão encerrada é histórico e não restaura privilégios antigos.

## Eventos

- Todo evento pertence à atlética atual e guarda a gestão ativa como `gestaoOrigemId`, quando existir. A autoria vem do membro autenticado; navegador não informa nem autoria nem gestão de origem.
- Nome, início, fim e local são obrigatórios. O fim não pode ser anterior ao início. UF, quando informada, possui duas letras; participantes esperados não podem ser negativos.
- O orçamento previsto usa `DECIMAL(19,2)`, arredondamento `HALF_UP` no servidor e contrato textual com duas casas. Valores negativos são inválidos e `0.00` permite evento gratuito.
- O status começa em `PLANEJADO`. As transições comuns são `PLANEJADO → INSCRICOES_ABERTAS|EM_ANDAMENTO`, `INSCRICOES_ABERTAS → EM_ANDAMENTO` e `EM_ANDAMENTO → CONCLUIDO`. Retornos a estados anteriores exigem administrador, motivo e atividade auditada; não existe status cancelado nesta etapa.
- Criação, atualização e transição/correção de status são registradas na mesma transação da alteração. A consulta e os filtros são sempre limitados à atlética selecionada; um `gestaoId` de outra atlética retorna `404`.

## Participações em eventos

- `ParticipacaoEvento` é sempre limitada à atlética e ao evento atual. Para participação interna, o membro precisa estar ativo e não pode repetir inscrição no mesmo evento. Para externa, somente nome e e-mail normalizado são coletados, sem criar usuário, e o e-mail é único no evento.
- A lista e os dados pessoais de participantes externos são acessíveis apenas ao organizador inicial do evento ou a administrador. Enquanto não há equipe própria de organização, o membro que criou o evento é seu organizador.
- A participação inicia `INSCRITO`, pode avançar para `CONFIRMADO` e `PRESENTE`, e pode ser `CANCELADO` sem apagar o registro. A correção de `PRESENTE` para `CONFIRMADO` requer motivo e é gravada na atividade do servidor.
- O resumo é cumulativo por estágio: `inscritos` = INSCRITO + CONFIRMADO + PRESENTE; `confirmados` = CONFIRMADO + PRESENTE; `presentes` = PRESENTE. Cada campo é um total próprio, não uma soma de pessoas novas. `participantesEsperados` permanece uma previsão independente.

## Cronograma e responsabilidades de evento

- `AtividadeCronograma` usa data e horário completos, com início obrigatório, fim opcional e ordem. Os instantes devem estar dentro do período do evento; quando houver fim, ele não pode ser anterior ao início. A ordenação é por início, ordem e ID, para distinguir dias e horários iguais.
- Atividades podem ser criadas, alteradas ou removidas somente por organizadores ou administração. A remoção exige versão e mantém a atividade de auditoria.
- `ResponsabilidadeEvento` aceita as áreas `COORDENACAO_GERAL`, `FINANCEIRO` e `COMUNICACAO`. Só membros ativos da mesma atlética podem ser designados; há no máximo uma designação ativa por área, embora o mesmo membro possa acumular áreas.
- Trocar ou remover responsável encerra a designação, com data e autor, sem apagar o registro anterior. A consulta padrão mostra designações ativas; o histórico é disponível explicitamente. Criação, substituição e remoção são auditadas na mesma transação.

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

## Tarefas, checklist e notificações de prazo

- Toda tarefa pertence a um evento e à atlética atual; guarda a gestão ativa como `gestaoOrigemId`. O responsável precisa ser membro `ATIVO` da mesma atlética.
- Gerenciam tarefas: administrador; financeiro quando responsável ativo do evento; gestor quando responsável ativo do evento ou no próprio departamento. O responsável atualiza o status da própria tarefa. Membro comum só consulta as suas.
- Transições: a fazer → em andamento → revisão → concluído; concluir direto vale para quem pode atualizar a tarefa; retornos a em andamento exigem motivo e ficam na atividade auditada.
- Tarefa atrasada: prazo anterior ao dia atual (fuso da atlética) e status diferente de concluído.
- Notificações: no dia anterior ao prazo e uma vez por dia enquanto atrasada; não para tarefas concluídas nem responsáveis não ativos; reatribuição avisa o responsável atual.

## Financeiro: categorias e lançamentos

- Categoria financeira pertence à atlética, possui nome único normalizado e pode ser aplicável a `RECEITA`, `DESPESA` ou ambos. Categorias inativas não podem receber novos lançamentos.
- Todo lançamento pertence à atlética e guarda a gestão ativa como origem quando houver. Evento e gestão informados são sempre resolvidos no escopo da atlética atual; o navegador não define autor, aprovação nem baixa.
- Receita inicia `ABERTO` e `NAO_APLICAVEL`; despesa inicia `ABERTO` e `PENDENTE`. Todo lançamento aberto exige data de vencimento.
- `VENCIDA` é condição derivada: lançamento `ABERTO` com vencimento anterior ao dia atual no fuso da atlética. Não é status persistido.
- Valores monetários usam `DECIMAL(19,2)`, arredondamento `HALF_UP` no servidor e contrato textual decimal com duas casas. O valor deve ser maior que zero.
- A consulta financeira detalhada é exclusiva de administrador e financeiro. Filtros de datas usam modo explícito: competência por padrão, vencimento ou liquidação; intervalos são inclusivos.
- A criação e a alteração de lançamentos, assim como a criação de categorias, registram atividade de auditoria na mesma transação.

## Financeiro: aprovação, baixa, saldos e prestação de contas

- Aprovação e rejeição são exclusivas de administrador e financeiro. A despesa deve estar `PENDENTE` e o autor não pode decidir o próprio lançamento; decisão, autor, instante e motivo são conservados.
- Receita pode ser liquidada em aberto. Despesa só é liquidada em aberto depois de `APROVADA`; rejeitada permanece consultável e não pode ser paga. Cada liquidação grava a data real e o autor, com versão e estado verificados na mesma transação.
- Lançamento aberto pode ser cancelado com motivo. Lançamento já liquidado não é apagado nem cancelado: recebe no máximo um `EstornoFinanceiro` integral, imutável e único, na data real. O estorno de receita reduz entrada; o de despesa reduz saída.
- O saldo disponível usa regime de caixa: recebimentos menos pagamentos, menos estornos de receita, mais estornos de despesa. Contas abertas são pendências a receber/a pagar e não entram no saldo disponível.
- Todos os associados ativos podem consultar saldos, evolução, contas e relatório agregados; essas respostas não contêm dados pessoais de contrapartes nem anexos restritos. Somente administrador e financeiro gerenciam decisões, baixas, cancelamentos e estornos.
- A evolução mensal retorna inclusive meses sem movimentos. O saldo inicial cadastrado é zero nesta etapa e não é reiniciado por gestão. Relatório CSV informa período, critérios, saldo inicial, entradas, saídas, estornos, saldo final e pendências separadas.

## Arquivos protegidos e temporários

- `Arquivo` pertence à atlética e guarda nome original seguro, MIME validado, tamanho, hash SHA-256, autor e instante definidos pelo servidor; o conteúdo fica em BLOB e não compõe projeções/listagens de metadados.
- O envio é interno aos módulos que já autorizaram o recurso pai. Não existe upload público sem finalidade. O módulo vinculante precisa salvar sua referência ao arquivo na mesma transação.
- Cada arquivo tem limite de 10 MiB e a atlética possui orçamento total configurável. A reserva é serializada no banco para impedir ultrapassar o teto em envios concorrentes.
- PDF é aceito para documentos; PDF, PNG e JPEG para divulgação. Assinatura binária, extensão e MIME declarado precisam coincidir; SVG, HTML, scripts e executáveis são recusados.
- A leitura resolve `id + atleticaId`, mas só entrega o BLOB quando o módulo proprietário confirma o acesso ao recurso pai e sua visibilidade. Sem vínculo reconhecido retorna `404`; recusa de visibilidade retorna `403`. O download usa MIME seguro, `Content-Disposition`, `nosniff` e não grava em diretório público.
