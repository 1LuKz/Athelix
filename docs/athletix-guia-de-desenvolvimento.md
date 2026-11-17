# Etapas de desenvolvimento do Athletix

## Backend

### B01 Preparar o projeto e as configurações

**Cobre:** preparação do backend e base de RNF18 e RNF19.

Criar o backend com Java 17, Maven Wrapper e Spring Boot 3.4.1. Configurar compilação e execução para Java 17, sem usar APIs ou recursos exclusivos de versões superiores. Organizar os módulos por domínio, conforme a estrutura definida na conversa.

Adicionar as dependências de aplicação: Spring MVC, Spring Data JPA, Validation, Security, H2 em escopo runtime, Spring Mail e springdoc OpenAPI 2.7.0. Usar o gerenciamento de versões do Spring Boot para as dependências que ele cobre. Preservar as versões escolhidas nas próximas etapas.

Usar exclusivamente `application.properties` e `application-dev.properties` nas configurações. Ativar o perfil `dev` na execução local. Configurar o plugin Maven do Spring Boot e a execução pela IDE ou pelo Maven Wrapper.

Configurar H2 em memória com nome de banco próprio para desenvolvimento, `spring.jpa.hibernate.ddl-auto=create-drop` e `spring.jpa.open-in-view=false`. Usar a URL `jdbc:h2:mem:athletix;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`. Recriar a base na inicialização; não configurar armazenamento em disco. Os dados e arquivos dessa base são temporários.

Definir em `.properties` a porta do backend, o fuso padrão `America/Sao_Paulo`, o endereço do frontend, o tempo de sessão sugerido de 30 minutos, o limite de 10 MiB por arquivo, o limite compatível da requisição multipart e os parâmetros de e-mail. Receber credenciais sensíveis por variáveis de ambiente, sem gravá-las no repositório. O console H2, se usado, fica restrito ao perfil de desenvolvimento e ao acesso local. Não é necessário expor o console para o frontend funcionar.

### B02 Criar os contratos e o tratamento de erros

**Depende de:** B01. **Cobre:** RNF18 e RNF19.

Criar o formato de paginação, os erros padronizados e exceções para não encontrado, conflito e regra de negócio. Implementar o tratamento central de erros de validação, acesso e falhas de concorrência. Separar DTOs de criação, alteração e resposta.

Adotar a base `/api/v1` nas rotas; os caminhos das próximas etapas são relativos a essa base. Usar identificadores `Long` gerados, DTOs separados de entrada e saída e enums persistidos como texto. Não retornar entidades JPA diretamente nem expor senhas, tokens ou BLOBs em listagens.

Implementar paginação estável com `items`, `page`, `size`, `totalItems` e `totalPages`; tamanho padrão 20 e máximo 100; ordenações permitidas explicitamente. Erros devem conter `status`, `code`, `message`, `fieldErrors`, `path` e `timestamp`, sem stack trace.

Usar HTTP 201 ao criar, 200 ao consultar/alterar, 204 quando não há conteúdo, 400 em entrada inválida, 401 sem sessão, 403 sem permissão, 404 para recurso inexistente ou de outra atlética e 409 para conflitos. Tratar falhas de segurança no mesmo formato de resposta da API.

Guardar instantes de criação/alteração em `Instant`, vencimentos em `LocalDate` e programação de eventos em `OffsetDateTime`, respeitando o fuso da atlética. Usar `BigDecimal` com escala 2 e precisão SQL 19 para dinheiro; transmitir valor monetário como texto decimal, por exemplo `"1250.90"`. Definir arredondamento explícito no servidor.

Adicionar `@Version` às entidades alteráveis concorrentemente, receber a versão nos DTOs de alteração e retornar 409 quando houver conflito. Aprovação financeira e movimentação de estoque também exigem validação atômica. Não excluir em cascata os registros históricos.

Registrar essas convenções, os contratos OpenAPI e as regras em `docs/api/` e `docs/regras-de-negocio.md`. Atualizar a documentação ao concluir cada endpoint. Não criar um CRUD genérico que elimine as regras específicas de cada domínio.

### B03 Criar a entidade Atlética e seu contexto

**Depende de:** B02. **Cobre:** RNF06 e base de RNF09.

Criar `Atletica` com `id`, `nome` obrigatório até 120 caracteres, `sigla` até 20, `instituicao` até 160, `cnpj` opcional normalizado com 14 dígitos, `fusoHorario`, `ativa`, datas e versão. Se informado, o CNPJ deve ser validado e ser único. Não exigir CNPJ de toda atlética, pois a própria documentação reconhece organizações sem CNPJ.

Criar `DominioEmailPermitido` com `id`, `atleticaId` e `dominio` normalizado, único por atlética. Validar o domínio completo, sem aceitar outro apenas por terminar no mesmo texto.

Introduzir `ContextoAtletica` e a regra de resolução de recursos pelo par `id + atleticaId`. Aplicar esse escopo a consultas, alterações, agregados, exportações, arquivos e rotinas agendadas. Validar que cada relacionamento recebido também pertence à atlética atual; um ID enviado pelo navegador não concede acesso. O preenchimento autenticado desse contexto será concluído em B06. Para este protótipo, criar atléticas por carga inicial; não abrir um endpoint público de administração global.

### B04 Criar cargos e departamentos

**Depende de:** B03; integrar responsáveis em B05 e APIs protegidas em B08. **Cobre:** RF12 e RF15 a RF17.

Criar `Cargo` com `id`, `atleticaId`, `nome` obrigatório até 100, `descricao` até 500, `perfilAcesso` (`ADMINISTRADOR`, `FINANCEIRO`, `GESTOR`, `MEMBRO`), `ativo`, datas e versão. Nome único por atlética, desconsiderando diferenças de caixa e espaços nas extremidades. Perfil só pode ser atribuído por administrador.

Criar `Departamento` com `id`, `atleticaId`, `nome` obrigatório até 100, `descricao` até 1000, `responsavelMembroId`, `situacao` (`EM_CONFIGURACAO`, `ATIVO`, `INATIVO`), datas e versão. Nome único por atlética. A contagem de membros é calculada a partir dos vínculos, não armazenada manualmente.

O responsável tem referência a `Membro`, criado em B05. Em B04, manter seu identificador opcional sem depender de uma classe ainda inexistente; em B05, completar a relação e sua integridade referencial. Para resolver a criação circular nos dados, permitir criar o departamento em configuração, cadastrar e aprovar seu primeiro membro, designá-lo e então ativar o departamento. A ativação exige responsável ativo da mesma atlética e do próprio departamento. O departamento em configuração só aparece para a administração e nas seleções necessárias a esse fluxo; ainda não conta como estrutura ativa. A validação do primeiro membro pode ocorrer nessa situação para evitar dependência circular.

Inativação de departamento exige antes transferir seus membros ativos e substituir suas responsabilidades. Não apagar cargos ou departamentos referenciados por histórico.

### B05 Criar as entidades Usuário e Membro

**Depende de:** B04. **Cobre:** RF01, RF03, RF09, RF13 e RF14.

Dividir, se necessário, em B05a identidade e B05b vínculo.

**Criar `Usuario`:** `id`; `nome` obrigatório de 3 a 120 caracteres; `email` obrigatório, válido, normalizado e globalmente único até 254; `telefone` opcional normalizado até 20; `senhaHash` até 255, inicialmente ausente somente enquanto aguarda definição por convite; `credencialAtiva`; `ultimoAcessoEm`; `senhaAlteradaEm`; datas e versão. Não expor `senhaHash` em nenhuma resposta.

**Criar `Membro`:** `id`; `atleticaId`; `usuarioId`; `curso` obrigatório até 160; `periodoCurso` opcional de 1 a 30; `cargoId`; `departamentoId`; `status` com `PENDENTE`, `ATIVO` ou `INATIVO`; `membroDesde` como data de entrada; `ativadoEm`; `inativadoEm`; `validadoPorMembroId`; datas e versão. A combinação `usuarioId + atleticaId` é única. A mesma conta pode ter cargos e status diferentes em atléticas distintas.

Criar um membro como `PENDENTE`. Acesso exige vínculo `ATIVO`, atlética ativa e credencial definida. Inativar preserva tarefas, movimentações, autoria de documentos e histórico. Ao inativar alguém que seja responsável de departamento ou evento em aberto, exigir substituto ou apontar as pendências antes de concluir.

### B06 Implementar autenticação e autorização

**Depende de:** B05. **Cobre:** RF01, RF12 e RF14; RNF01, RNF03, RNF05 e RNF06.

Implementar BCrypt pelo `PasswordEncoder`, sem gravar senha em texto. Proposta de política: senha de 10 a 64 caracteres, respeitando o limite de bytes do BCrypt usado; validação idêntica na criação, alteração e recuperação. Não truncar senha silenciosamente.

Criar `GET /auth/csrf`, `POST /auth/login`, `POST /auth/logout`, `GET /auth/me` e `POST /auth/selecionar-atletica`. Login recebe e-mail e senha. Resposta não inclui tokens de senha; informa conta, vínculos ativos e capacidades. Com um vínculo, selecioná-lo; com vários, exigir seleção autorizada antes de acessar domínios. Nunca apresentar dados de um vínculo alheio.

Usar a infraestrutura de autenticação do Spring Security, inclusive proteção contra fixação de sessão e persistência de contexto. Manter CSRF. Erro de senha deve ser genérico; limitar tentativas por combinação de conta e origem com parâmetros documentados para a demonstração.

Implementar a seguinte matriz de capacidades no servidor. O cargo aponta para um perfil; seu nome livre não determina privilégios. Usar um departamento principal por membro.

| Operação | Administrador | Financeiro | Gestor | Membro |
| --- | --- | --- | --- | --- |
| Consultar membros e departamentos | Sim | Sim | Sim | Somente dados mínimos |
| Criar, aprovar, inativar ou mudar acesso de membros | Sim | Não | Não | Não |
| Configurar cargos e departamentos | Sim | Não | Não | Não |
| Gerenciar eventos e tarefas | Sim | Se responsável designado pelo evento | No próprio departamento ou evento sob sua responsabilidade | Não |
| Atualizar tarefa atribuída a si | Sim | Sim | Sim | Sim |
| Consultar saldos e relatório para associados | Sim | Sim | Sim | Sim |
| Lançar, aprovar e liquidar movimentações financeiras | Sim | Sim | Não | Não |
| Gerenciar patrimônio | Sim | Não | No departamento responsável | Não |
| Consultar documentos | Conforme visibilidade | Conforme visibilidade | Conforme visibilidade | Conforme visibilidade |
| Publicar e versionar documentos | Sim | Não por padrão | Se responsável pelo departamento administrativo | Não |
| Concluir transferência de gestão | Sim | Não | Não | Não |

Documentos com visibilidade `MEMBROS` ficam acessíveis aos membros ativos; `DIRETORIA` exige perfil administrador, financeiro ou gestor. Os resumos financeiros para associados não expõem dados pessoais de contrapartes nem anexos restritos. Retornar as capacidades efetivas ao frontend e aplicar as mesmas restrições nos indicadores do dashboard.

Obter a atlética atual do vínculo autenticado selecionado. Não confiar em um `atleticaId` livre enviado pelo navegador. Usar cookie de sessão `HttpOnly`, política `SameSite` apropriada e `Secure` quando houver HTTPS. Manter CSRF ativo, inclusive em login e logout, e expiração sugerida de 30 minutos de inatividade. Evitar que consultas de fundo prolonguem uma sessão sem interação do usuário.

Usar o proxy do Vite para `/api` no desenvolvimento local. Se configurar acesso entre origens, permitir somente a origem exata do frontend com credenciais, sem curinga.

Aplicar as capacidades em cada operação. Validar que vínculo, status e perfil continuam válidos, para que uma inativação ou retirada de cargo não dependa de novo login para fazer efeito. Proteger o último administrador ativo contra remoção acidental.

Preparar agora uma carga mínima de desenvolvimento, com atlética, cargo administrativo, departamento e primeiro usuário ativo com senha de demonstração em hash para o primeiro acesso. Pode ser um inicializador restrito a `dev`. A etapa B27 ampliará essa carga para a demonstração completa; não esperar B27 para criar o primeiro acesso.

### B07 Implementar convites e recuperação de senha

**Depende de:** B06. **Cobre:** RF02 e apoio a RF14.

Criar `TokenCredencial` com `id`, `usuarioId`, `membroId` quando for convite, `finalidade` (`DEFINIR_SENHA` ou `RECUPERAR_SENHA`), `tokenHash`, `criadoEm`, `expiraEm`, `consumidoEm`. Gerar valor aleatório seguro, enviar o valor original somente no link e armazenar apenas seu hash. O token tem uso único; sugerir validade de 30 minutos para recuperação e 24 horas para convite.

Criar `POST /auth/esqueci-senha`, `POST /auth/redefinir-senha` e `POST /auth/definir-senha`. O primeiro sempre retorna mensagem genérica, exista ou não a conta. Os outros recebem token e nova senha, rejeitam tokens inválidos/expirados/reutilizados e invalidam tokens anteriores da mesma finalidade depois do uso. Após redefinir a senha, invalidar as sessões anteriores da conta.

O convite para um usuário que já possui credencial não deve redefinir sua senha: após aprovação, ele usa a conta existente. Token nunca substitui a validação administrativa do vínculo. Usar endereço de frontend configurado, não um host arbitrário recebido na requisição.

Configurar envio pelo Spring Mail usando um serviço SMTP definido pelo desenvolvedor, com parâmetros em `.properties` e credenciais por variáveis de ambiente. Para a demonstração, pode ser uma caixa SMTP de desenvolvimento acessada pelo navegador, sem instalação de contêineres. Não retornar o link secreto como resposta pública e não simular que houve entrega de e-mail quando só existir uma implementação falsa.

### B08 Criar a API de membros cargos e departamentos

**Depende de:** B06 e B07. **Cobre:** RF09 a RF17.

Criar `GET/POST /membros`, `GET/PUT /membros/{id}`, `PATCH /membros/{id}/aprovar`, `PATCH /membros/{id}/inativar` e `GET /membros/exportacao`. O cadastro recebe nome, e-mail, telefone, curso, período, cargo e departamento; o status inicial é definido pelo servidor. Ao encontrar uma conta global existente, vinculá-la sem permitir editar seus dados pessoais por esse cadastro.

Listagem pesquisa nome, curso e e-mail sem distinguir caixa. Aceita `departamentoId`, `status`, paginação e ordenação. Resposta inclui cargo, departamento, status e nome, sem senha ou credenciais. Não entregar e-mails/telefones a papéis que não precisam deles.

Exportar CSV UTF-8 com filtros equivalentes à tela, incluindo todas as linhas filtradas, não só a página visível. Escapar separadores, aspas e quebras de linha; neutralizar células que possam ser interpretadas como fórmulas por planilhas. Exportação respeita a capacidade e a atlética.

Criar `GET/POST /cargos`, `PUT /cargos/{id}`, `GET/POST /departamentos`, `GET/PUT /departamentos/{id}` e `PATCH /departamentos/{id}/ativar`. Disponibilizar ações de inativação com as regras de B04. Mostrar situação, contagem derivada e responsável no DTO de departamento. O fluxo de configuração deve permitir cadastrar/aprovar seu primeiro responsável antes da ativação do departamento.

### B09 Criar histórico de atividades e perfil

**Depende de:** B08. **Cobre:** RF03 a RF05.

Criar `AtividadeUsuario` com `id`, `atleticaId`, `usuarioId`, `membroId`, `acao`, `tipoRecurso`, `recursoId`, `resumo`, `ocorridaEm` e identificação da gestão quando aplicável. Registrar ações reais no servidor: criação/ativação de membro, mudança de cargo, atualização de evento, conclusão de tarefa, aprovação/pagamento financeiro, empréstimo e versão documental. Conectar os módulos seguintes a esse serviço.

Registrar a alteração e sua auditoria na mesma transação quando forem operações de banco. Não aceitar do navegador o autor ou a data do histórico. Guardar resumos mínimos, sem senhas, tokens ou conteúdo integral de documentos.

Criar `GET /perfil`, `PUT /perfil`, `PUT /perfil/senha` e `GET /perfil/atividades`. Alteração de senha exige a atual e a nova e invalida outras sessões. A edição direta do perfil modifica nome, telefone e curso/período. E-mail fica somente leitura nesta versão, como o RF03 não exige sua alteração.

Criar `SolicitacaoAlteracaoVinculo` com membro, cargo/departamento pretendidos, motivo, status `PENDENTE/APROVADA/REJEITADA`, datas e avaliador. Criar `POST /perfil/solicitacoes-vinculo`, `GET /membros/solicitacoes-vinculo` e `PATCH /membros/solicitacoes-vinculo/{id}/decisao`. Mudança só passa a valer após aprovação administrativa. O histórico precisa guardar o vínculo anterior.

### B10 Criar gestões e histórico de vínculos

**Depende de:** B09. **Cobre:** base de RF47 e RF48.

Criar `Gestao` com `id`, `atleticaId`, `nome` até 80, `dataInicio`, `dataFimPrevista`, `encerradaEm`, `status` (`PLANEJADA`, `ATIVA`, `ENCERRADA`), datas e versão. Início não pode ser posterior ao fim previsto. Só uma gestão fica ativa por atlética, garantido em transação com bloqueio da atlética durante a ativação.

Criar `VinculoGestao` com membro, gestão, cargo, departamento, início e fim de vigência. Abrir novo período quando cargo/departamento mudar; não sobrescrever o passado. Esses registros permitem responder qual cargo a pessoa tinha na época da ação.

Criar `GET/POST /gestoes` e `GET /gestoes/{id}`; a transição ficará em B24. Os módulos seguintes registram `gestaoOrigemId` quando pertinente. Patrimônio e biblioteca documental pertencem continuamente à atlética, com origem e versões associadas à gestão que os produziu.

Eventos em andamento, tarefas abertas, empréstimos e contas pendentes podem atravessar uma troca de gestão: mantêm origem e recebem continuidade auditada. Não arquivar uma conta pendente de forma que ela desapareça das obrigações atuais. A consulta de uma gestão encerrada é histórica e não restaura privilégios antigos.

### B11 Criar a entidade Evento e sua API

**Depende de:** B10. **Cobre:** RF18 e RF22.

Criar `Evento` com `id`, `atleticaId`, `gestaoOrigemId`, `nome` obrigatório até 160, `descricao` até 5000, `inicio`, `fim`, `local` obrigatório até 200, `cidade` até 120, `uf` com duas letras, `orcamentoPrevisto` não negativo, `participantesEsperados` inteiro não negativo opcional, `status`, `criadoPorMembroId`, datas e versão.

Status: `PLANEJADO`, `INSCRICOES_ABERTAS`, `EM_ANDAMENTO`, `CONCLUIDO`. Fim deve ser igual ou posterior ao início. Permitir evento gratuito com orçamento zero. Proposta de transições: planejado pode abrir inscrições ou começar diretamente; inscrições abertas pode avançar para andamento; em andamento pode concluir. Correção de estado anterior exige administrador e registro de motivo. Não inventar um status cancelado como requisito obrigatório.

Criar `GET/POST /eventos`, `GET/PUT /eventos/{id}` e `PATCH /eventos/{id}/status`. Filtros: pesquisa por nome, intervalo de datas, `status`, grupo de status e gestão. Grupo `ATIVOS` inclui inscrições abertas e em andamento. A contagem das abas considera os mesmos filtros de busca, exceto o filtro da própria aba.

### B12 Criar participantes e contagens de eventos

**Depende de:** B11. **Cobre:** RF19.

Criar `ParticipacaoEvento` com `id`, `atleticaId`, `eventoId`, `membroId` opcional para participante interno, `nomeParticipante` até 120 e `emailParticipante` até 254 quando externo, `status` (`INSCRITO`, `CONFIRMADO`, `PRESENTE`, `CANCELADO`), `inscritoEm`, `confirmadoEm`, `presencaEm`, datas e versão. Nome e identificação são obrigatórios para externos; minimizar dados coletados.

Um membro não pode ter duas participações no mesmo evento. Para externos, e-mail normalizado é único no evento. Participação avança inscrito → confirmado → presente; cancelamento conserva o registro. Correções de presença ficam auditadas. Não exigir que um participante externo crie conta.

Criar `GET/POST /eventos/{id}/participacoes`, `PATCH /eventos/{id}/participacoes/{participacaoId}/status` e `GET /eventos/{id}/participacoes/resumo`, restritos aos organizadores e administração; participantes comuns não recebem a lista de dados pessoais.

No resumo, `inscritos` conta inscritos, confirmados e presentes; `confirmados` conta confirmados e presentes; `presentes` conta apenas presentes. São totais cumulativos: uma pessoa presente não vira três participantes. `participantesEsperados` é previsão independente. Na interface, usar esses nomes explícitos em vez de alternar um único número de significado incerto.

### B13 Criar cronograma e responsáveis do evento

**Depende de:** B12. **Cobre:** RF20 e RF21.

Criar `AtividadeCronograma` com `id`, `atleticaId`, `eventoId`, `titulo` até 160 obrigatório, `descricao` até 1000, `inicio`, `fim` opcional, `status` (`AGENDADO`, `EM_ANDAMENTO`, `CONCLUIDO`), `ordem` e versão. Datas devem caber no período do evento; ordenar por dia, horário e ordem. Usar data completa, pois o evento pode ter vários dias e atividades às mesmas horas em dias diferentes.

Criar `ResponsabilidadeEvento` com `id`, `atleticaId`, `eventoId`, `membroId` e `area` (`COORDENACAO_GERAL`, `FINANCEIRO`, `COMUNICACAO`). Proposta: um responsável por área, podendo a mesma pessoa acumular áreas. Exigir membro ativo da mesma atlética. Ao substituir alguém, conservar a ação no histórico.

Criar operações de listar, criar e alterar sob `/eventos/{id}/cronograma` e `/eventos/{id}/responsaveis`, com rotas por ID para atualização e remoção controlada. Remover uma designação atual não remove seu histórico.

### B14 Criar tarefas quadro e checklist

**Depende de:** B13. **Cobre:** RF25 a RF27 e RF29.

Criar `Tarefa` com `id`, `atleticaId`, `gestaoOrigemId`, `eventoId` obrigatório nesta versão, `titulo` obrigatório até 160, `descricao` até 3000, `responsavelMembroId`, `departamentoId`, `prioridade` (`ALTA`, `MEDIA`, `BAIXA`), `prazo` como data, `status` (`A_FAZER`, `EM_ANDAMENTO`, `REVISAO`, `CONCLUIDO`), `concluidaEm`, `criadaPorMembroId`, datas e versão.

Criar `GET/POST /tarefas`, `GET/PUT /tarefas/{id}` e `PATCH /tarefas/{id}/status`. Listagem aceita evento, responsável, departamento, prioridade, status, prazo e atraso. O responsável deve estar ativo e pertencer à atlética do evento. Mudança de responsável só por quem gerencia a tarefa.

Proposta de transição: a fazer → em andamento → revisão → concluído, permitindo retorno para em andamento com motivo. Para o checklist, ação de concluir pode ir diretamente para concluído se o usuário tiver capacidade; desmarcar reabre em andamento. O responsável atualiza a própria tarefa; revisão/conclusão também pode ser feita pela coordenação autorizada. Todos os estados permanecem consistentes entre quadro e checklist.

Criar `GET /eventos/{id}/checklist` derivando suas tarefas, total e concluídas. Progresso = concluídas / total × 100; sem tarefas, mostrar 0 e texto adequado. Tarefa atrasada tem prazo anterior ao dia atual e status diferente de concluído. Prioridade e atraso são atributos distintos.

### B15 Criar notificações de prazo

**Depende de:** B14. **Cobre:** RF28.

Criar `Notificacao` com `id`, `atleticaId`, `destinatarioMembroId`, `tarefaId`, `tipo` (`PRAZO_PROXIMO` ou `ATRASADA`), `dataReferencia`, `titulo` até 160, `mensagem` até 500, `criadaEm` e `lidaEm`. Chave única: destinatário + tarefa + tipo + data de referência.

Criar rotina agendada com intervalo configurável. Para a demonstração, avisar no dia anterior e uma vez por dia enquanto estiver vencida. Não notificar tarefas concluídas nem responsáveis inativos. Tarefas pós-evento ainda abertas, como prestação de contas, continuam notificáveis mesmo após a conclusão do evento. Reatribuição envia ao responsável atual; não duplicar avisos do mesmo dia. Executar a rotina para cada atlética com seu fuso e escopo explícitos.

Criar `GET /notificacoes`, `GET /notificacoes/resumo` e `PATCH /notificacoes/{id}/ler`. Cada pessoa consulta e marca apenas suas notificações. Buscar avisos ao abrir o painel ou por ação do usuário é suficiente; não é necessário WebSocket.

### B16 Criar categorias e lançamentos financeiros

**Depende de:** B14. **Cobre:** RF30, RF32 e RF35.

Criar `CategoriaFinanceira` com `id`, `atleticaId`, `nome` até 100, `tipoAplicavel` (`RECEITA`, `DESPESA`, `AMBOS`), `ativa`, datas e versão. Nome único por atlética. Exemplos iniciais: eventos, esportivo e patrocínio; não presumir que categoria seja sempre um departamento.

Criar `LancamentoFinanceiro` com `id`, `atleticaId`, `gestaoOrigemId`, `eventoId` opcional, `tipo` (`RECEITA`, `DESPESA`), `descricao` obrigatória até 250, `categoriaId`, `valor` maior que zero, `dataCompetencia`, `dataVencimento`, `dataLiquidacao` opcional, `statusLiquidacao` (`ABERTO`, `LIQUIDADO`, `CANCELADO`), `statusAprovacao` (`NAO_APLICAVEL`, `PENDENTE`, `APROVADA`, `REJEITADA`), `criadoPorMembroId`, `aprovadoPorMembroId`, `aprovadoEm`, `motivoDecisao` até 1000, `liquidadoPorMembroId`, datas e versão.

Receita começa com aprovação não aplicável; despesa começa pendente. A conta a pagar é uma despesa em aberto; a conta a receber é uma receita em aberto. Vencida é condição calculada, não outro registro. Exigir vencimento para contas abertas. Lançamento liquidado recebe data efetiva e autor da baixa, conforme B17.

Criar `GET/POST /financeiro/lancamentos`, `GET/PUT /financeiro/lancamentos/{id}` e `GET/POST /financeiro/categorias`. Filtros: competência, vencimento ou liquidação com modo explícito, intervalo de datas, categoria, tipo, situação, evento e gestão. Manter padrão de data do relatório documentado.

### B17 Implementar aprovação baixa saldos e prestação de contas

**Depende de:** B16. **Cobre:** RF23, RF31 e RF33 a RF36.

Esta etapa pode ser enviada em três partes: B17a decisões e baixa; B17b consultas e cálculos; B17c relatório.

Criar `PATCH /financeiro/lancamentos/{id}/aprovacao`, `POST /financeiro/lancamentos/{id}/liquidacao` e `PATCH /financeiro/lancamentos/{id}/cancelamento`. Aprovação/rejeição registra autor, instante e motivo. Somente capacidades administrativas/financeiras podem decidir e liquidar. Proposta para este projeto: o autor não aprova a própria despesa; preparar dois usuários autorizados na carga inicial. Essa separação é uma escolha adicional, não especificação textual de RF33.

Liquidar receita registra recebimento; liquidar despesa exige aprovação. A operação ocorre uma única vez na mesma entidade. Requisição repetida não pode gerar novo movimento; usar versão e estado em transação. Uma despesa rejeitada permanece consultável e não pode ser paga. Conta aberta pode ser cancelada com motivo. Pagamento já efetivado exige estorno, sem apagar a transação.

Criar `EstornoFinanceiro` com `id`, `atleticaId`, `lancamentoId` único, `dataEstorno`, `motivo` obrigatório até 1000, `registradoPorMembroId`, `registradoEm`. Proposta: somente estorno integral, na data real, sem estorno parcial. Rota `POST /financeiro/lancamentos/{id}/estorno`. O estorno contribui no caixa com sinal oposto ao original na sua própria data. Manter original e estorno no relatório; não criar simultaneamente outra receita/despesa duplicando a reversão.

Criar `GET /financeiro/resumo`, `GET /financeiro/evolucao-mensal`, `GET /financeiro/contas` e `GET /financeiro/relatorio`. Regras:

- Receitas do mês = recebimentos liquidados naquele mês, menos estornos de receitas naquele mês.
- Despesas do mês = pagamentos de despesas aprovadas naquele mês, menos estornos de despesas naquele mês.
- Saldo mensal = receitas do mês − despesas do mês.
- Saldo acumulado até a data = saldo inicial cadastrado + todos os recebimentos − todos os pagamentos + efeitos dos estornos até a data.
- Sugestão inicial: saldo inicial zero. Se houver saldo anterior, cadastrá-lo uma vez como abertura auditada; nunca reiniciar o saldo a cada nova gestão.
- Evolução mensal retorna todos os meses solicitados, inclusive os sem movimentos, preenchidos com zero.
- Contas abertas retornam valor a pagar/receber, vencimento e indicador de atraso; não entram no saldo disponível.
- Relatório inclui período, critérios usados, saldo inicial do período, entradas, saídas, estornos, saldo final e pendências separadas. CSV é suficiente inicialmente.

### B18 Criar armazenamento temporário e protegido de arquivos

**Depende de:** B09. **Cobre:** infraestrutura de RF24, RF43, RF44 e RF46.

Criar `Arquivo` com `id`, `atleticaId`, `nomeOriginal` até 255, `mimeType` permitido, `tamanhoBytes`, `hashConteudo`, `enviadoPorMembroId`, `enviadoEm` e `conteudo` como BLOB. O responsável e a data vêm do servidor. Conteúdo deve ser carregado somente nas operações de leitura do arquivo, sem entrar nas listagens.

Proposta de limite: 10 MiB por arquivo, com limite de requisição multipart compatível e orçamento total de armazenamento por atlética configurável para não esgotar a memória. Para documentos, aceitar PDF. Para divulgação, aceitar PDF, PNG e JPEG. Não aceitar SVG, HTML, scripts ou executáveis nesta versão. Verificar assinatura do arquivo além da extensão e do MIME declarado.

Criar um serviço de armazenamento usado pelos módulos; uploads são feitos por rotas de evento/documento, já vinculando o arquivo ao recurso autorizado. Evitar uma rota pública que receba qualquer arquivo sem finalidade.

Criar leitura protegida em `GET /arquivos/{id}/conteudo`. Conferir acesso ao recurso pai e sua visibilidade, não apenas à atlética. Usar cabeçalhos de tipo, disposição e `nosniff`; PDF pode ser visualizado inline após autorização. Nome original não pode formar caminho de sistema. Evitar escrever em diretório público.

### B19 Integrar anexos e orçamento dos eventos

**Depende de:** B13, B17 e B18. **Cobre:** RF23 e RF24.

Criar `AnexoEvento` com `id`, `atleticaId`, `eventoId`, `arquivoId`, `titulo` até 160, `descricao` até 1000 e `tipoMaterial` (`ARTE`, `REGULAMENTO`, `OUTRO`). Criar `GET/POST /eventos/{id}/anexos`; `POST` usa multipart. Remoção controlada é permitida antes do encerramento, registrando a ação e sem apagar arquivos ainda referenciados.

Criar `GET /eventos/{id}/financeiro/resumo` reutilizando a regra financeira, com `orcamentoPrevisto`, `realizado`, `saldoOrcamentario`, `percentualUtilizado`, `receitasRealizadas` e `despesasAbertas`. Realizado = despesas aprovadas e pagas do evento menos seus estornos até a data consultada. Saldo orçamentário = previsto − realizado; receitas não são abatidas automaticamente do realizado.

Percentual = realizado / previsto × 100. Se previsto for zero, retornar percentual ausente e indicador `SEM_ORCAMENTO`, evitando divisão por zero. Acima de 100%, mostrar estouro do orçamento; não ocultar o excesso limitando o valor retornado.

### B20 Criar itens categorias e consultas de patrimônio

**Depende de:** B10. **Cobre:** RF37, RF40 e RF41.

Criar `CategoriaPatrimonio` com atlética, nome até 100 e ativo. Criar `ItemPatrimonio` com `id`, `atleticaId`, `gestaoOrigemId`, `nome` obrigatório até 160, `descricao` até 2000, `categoriaId`, `quantidadeTotal` inteira maior que zero, `conservacao` (`OTIMO`, `BOM`, `REGULAR`, `RUIM`), `localizacao` obrigatória até 200, `departamentoResponsavelId`, `valorEstimadoUnitario` não negativo, `ativo`, datas e versão.

Um cadastro representa unidades homogêneas com a mesma conservação, localização e estimativa unitária. Se um notebook tem estado diferente dos demais, separar seu lote, em vez de dar um único estado enganoso a todos.

Criar `GET/POST /patrimonio/itens`, `GET/PUT /patrimonio/itens/{id}` e `GET /patrimonio/resumo`. Filtros: nome, categoria, conservação e situação operacional. Conservação (`BOM`) e situação (`EMPRESTADO`) são conceitos separados; a tela deve distinguir ambos.

Calcular quantidade disponível a partir das movimentações de B21. Valor estimado total = soma de quantidade total × valor unitário dos itens ativos, incluindo unidades temporariamente emprestadas ou em manutenção. Reduzir quantidade total exige justificativa e validação para não retirar unidades indisponíveis.

### B21 Implementar empréstimos devoluções e manutenção

**Depende de:** B20. **Cobre:** RF38 e RF39.

Criar `EmprestimoPatrimonio` com `id`, `atleticaId`, `itemId`, `membroResponsavelId`, `quantidade`, `retiradoEm`, `devolucaoPrevistaEm`, `quantidadeDevolvida` calculada a partir de devoluções, `observacao` até 1000, `registradoPorMembroId`, datas e versão. Criar `DevolucaoPatrimonio` com empréstimo, quantidade, data, condição de retorno, observação e autor. Permitir devoluções parciais sem superar o saldo emprestado.

Criar `ManutencaoPatrimonio` com `id`, `atleticaId`, `itemId`, `quantidade`, `motivo` até 1000, `iniciadaEm`, `previsaoConclusaoEm`, `concluidaEm`, `resultado` até 1000 e responsável pelo registro. Quantidade em manutenção volta ao disponível quando a manutenção termina. Neste primeiro fluxo, levar à manutenção somente unidades disponíveis; unidade devolvida danificada pode ser devolvida e enviada à manutenção na mesma transação.

Disponível = total − saldo de empréstimos abertos − quantidade em manutenção aberta. Executar retirada, devolução e manutenção em transação, com bloqueio ou estratégia equivalente para impedir duas reservas da mesma disponibilidade.

Criar `GET/POST /patrimonio/emprestimos`, `POST /patrimonio/emprestimos/{id}/devolucoes`, `GET/POST /patrimonio/manutencoes` e `PATCH /patrimonio/manutencoes/{id}/concluir`. Não permitir inativar item com empréstimo ou manutenção em aberto.

### B22 Implementar conferência de inventário

**Depende de:** B21. **Cobre:** RF42.

Criar `ConferenciaInventario` com `id`, `atleticaId`, `gestaoId`, `nome` até 120, `iniciadaEm`, `encerradaEm`, `status` (`ABERTA`, `ENCERRADA`) e criador. Ao abrir, gerar `ItemConferencia` com campanha, item, quantidade esperada como fotografia do momento, quantidade verificada, divergência, observação, conferente e data.

Criar `GET/POST /patrimonio/conferencias`, `GET /patrimonio/conferencias/{id}`, `PUT /patrimonio/conferencias/{id}/itens/{itemId}` e `PATCH /patrimonio/conferencias/{id}/encerrar`. A quantidade verificada é não negativa e não pode exceder a esperada sem registrar uma divergência identificada; separar quantidade localizada e quantidade validada se necessário.

Proposta de indicador: unidades verificadas válidas / unidades esperadas no início da campanha × 100. A fotografia evita que um novo cadastro altere silenciosamente o denominador. Quantidade encontrada diferente da esperada gera divergência; não ajuste automaticamente o patrimônio sem uma ação autorizada e auditada.

### B23 Criar biblioteca documental com versões

**Depende de:** B18 e B10. **Cobre:** RF43 a RF46 e apoio a RF48.

Criar `Documento` com `id`, `atleticaId`, `titulo` obrigatório até 200, `categoria` (`ESTATUTO`, `REGIMENTO`, `ATA`, `CONTRATO`, `OUTRO`), `descricao` até 2000, `visibilidade` (`MEMBROS`, `DIRETORIA`), `gestaoOrigemId`, `arquivado`, datas e versão.

Criar `VersaoDocumento` com `id`, `atleticaId`, `documentoId`, `numeroVersao`, `arquivoId`, `gestaoId`, `observacao` até 1000, `enviadoPorMembroId` e `enviadoEm`. A combinação documento + número é única; gerar número sequencial no servidor com proteção contra uploads concorrentes. Uma versão publicada é imutável. Nova versão não sobrescreve o BLOB anterior.

Criar `GET/POST /documentos`, `GET/PUT /documentos/{id}`, `GET/POST /documentos/{id}/versoes` e `GET /documentos/{id}/versoes/{versaoId}/conteudo`. Criação com arquivo e nova versão usam multipart. Listagem pesquisa título e nome de arquivo, filtra categoria/gestão, ordena por mais recente e retorna a versão atual e seu responsável.

Contadores por categoria contam documentos lógicos, não todas as versões. Visualizar/download de versão antiga exige a mesma autorização do documento atual. Arquivar retira da lista corrente, mantendo consulta autorizada no histórico.

### B24 Implementar a transferência de gestão

**Depende de:** B10 e módulos de domínio. **Cobre:** RF47 e RF48.

Criar `TransicaoGestao` com `id`, `atleticaId`, `gestaoOrigemId`, `gestaoDestinoId`, `status` (`RASCUNHO`, `CONCLUIDA`), `preparadaPorMembroId`, `concluidaPorMembroId`, datas e versão. Criar itens de transição com membro, cargo/departamento atuais e pretendidos, condição de continuidade e substituições de responsáveis.

Criar `POST /gestoes/{id}/transicoes`, `PUT /gestoes/transicoes/{id}`, `GET /gestoes/transicoes/{id}/previa` e `POST /gestoes/transicoes/{id}/concluir`. A prévia exibe acessos que saem/entram, responsáveis substituídos e pendências: contas, eventos, tarefas, empréstimos e manutenção. Exigir ao menos um administrador ativo na composição de destino.

Concluir em uma transação: encerrar gestão antiga; finalizar períodos dos vínculos; iniciar gestão nova; abrir os novos períodos; aplicar cargos/status autorizados e substituições; invalidar sessões com privilégios removidos; registrar auditoria. Não copiar senhas nem compartilhar contas da diretoria anterior.

Dados concluídos da gestão anterior ficam consultáveis por gestão e sem edição operacional comum. Documentos anteriores e suas versões continuam disponíveis. Obrigações abertas recebem continuidade explícita com origem preservada. Saldo da atlética, quantidade de patrimônio e arquivos institucionais não são zerados.

### B25 Implementar os dados do dashboard

**Depende de:** B08, B11, B14, B17, B20, B22 e B24. **Cobre:** RF06 a RF08.

Criar `GET /dashboard/resumo`, `/dashboard/financeiro`, `/dashboard/departamentos`, `/dashboard/proximos-eventos`, `/dashboard/tarefas-pendentes` e `/dashboard/ultimas-movimentacoes`.

Definir os indicadores antes de consultar:

- Total de membros = todos os vínculos cadastrados da atlética, com contagens por status separadas. Se a opção do grupo for mostrar somente ativos, o rótulo deve ser membros ativos em toda a aplicação.
- Eventos ativos = inscrições abertas + em andamento. Eventos futuros planejados também podem aparecer na lista de próximos eventos.
- Saldo atual = caixa acumulado até a data, usando a mesma regra do financeiro.
- Patrimônios cadastrados = quantidade de cadastros ativos de itens, separada do total de unidades; o patrimônio usa rótulos explícitos para ambos.
- Receitas × despesas = seis meses incluindo o mês atual, com zeros nos meses vazios e efeitos dos estornos.
- Participação departamental = membros ativos de cada departamento / total de membros ativos × 100. Criar grupo sem departamento somente se o fluxo permitir essa condição temporária. Não contar todos os participantes de eventos nesse gráfico.
- Próximos eventos = ordenação por data, sem eventos concluídos; tarefas pendentes excluem concluídas e respeitam o escopo do usuário; últimas movimentações usam data de liquidação/estorno.
- Variações percentuais comparam períodos equivalentes, definidos no contrato. Quando a base anterior for zero, retornar ausência de percentual e texto sem comparação.

### B26 Criar a ajuda e a central de suporte

**Depende de:** B06. **Cobre:** RNF14 e RNF15.

Criar conteúdo de ajuda por módulo: primeiro acesso, convite, cadastro de membros, organização de eventos, aprovação/baixa financeira, empréstimos, documentos e troca de gestão. Pode ser um conteúdo estático versionado junto ao frontend; não é obrigatório criar entidades de chamados.

Se houver configuração por atlética, criar DTO de suporte com título, instruções e canal de contato fornecido pelo grupo, sem inventar telefone ou e-mail. O roteiro não exige enviar mensagens externas automaticamente. A central deve poder ser aberta de todas as áreas autenticadas e retornar à tela anterior.

### B27 Criar os dados de demonstração

**Depende de:** entidades e serviços anteriores.

Criar inicializador apenas em `dev`, usando serviços e regras reais. Inserir duas atléticas para demonstrar isolamento; domínios de e-mail; departamentos e cargos; usuários com diferentes perfis e vínculos; membros ativos, pendentes e inativos; uma gestão anterior e uma atual; eventos nos quatro estados; tarefas nas quatro colunas; receitas recebidas, despesas aprovadas/pagas, despesas pendentes/rejeitadas e contas futuras/vencidas; itens disponíveis, emprestados e em manutenção; uma conferência parcial; documentos com pelo menos duas versões; notificações e ações auditadas.

Usar senhas de demonstração documentadas somente para esse perfil, sempre armazenadas como hash. Usar arquivos sintéticos sem dados pessoais reais. Datas relativas a um relógio de demonstração facilitam apresentar prazos e seis meses de histórico sem depender das datas de setembro do protótipo.

### B28 Preparar a execução local e a documentação de entrega

**Depende de:** B01 a B27.

Criar README com requisitos de execução, comandos do Maven Wrapper, ativação do perfil `dev`, porta da API, endereço do Swagger UI e contas de demonstração. Documentar como configurar o envio de e-mail em `.properties` e variáveis de ambiente.

Descrever que o H2 em memória perde os dados ao encerrar a aplicação e que o inicializador recria o cenário de demonstração. Incluir os contratos da API, as regras financeiras, as capacidades por perfil e as regras da transferência de gestão.

Manter consultas paginadas, usar projeções e agregações para os resumos e carregar BLOBs somente ao solicitar o conteúdo. Evitar consultas repetidas para cada linha de uma listagem e exposição de dados pessoais além do necessário. Registrar finalidade de uso, perfis com acesso e a política definida pelo grupo para retenção dos dados pessoais.

Preparar a entrega para execução local pela IDE ou pelo Maven Wrapper. Configurar HTTPS e cookie seguro se o grupo disponibilizar uma demonstração pela internet. Não acrescentar infraestrutura de contêineres nem publicação em nuvem como pré-requisito da execução local.

## Frontend

### F01 Preparar React TypeScript Vite e rotas

**Depende de:** contratos de B02 e estrutura definida na conversa.

Criar o frontend com o modelo `react-ts` do Vite. Instalar React Router, TanStack Query, React Hook Form, Zod, resolvers, Recharts e Lucide React, conforme a lista definida na conversa. Manter TypeScript, ESLint e o plugin React do Vite. Configurar scripts de desenvolvimento, build e lint, além do proxy de `/api` para o backend local. Variáveis públicas como a base da API podem ser usadas; nenhum segredo deve entrar em variáveis `VITE_`.

Definir rotas públicas `/login`, `/esqueci-senha`, `/redefinir-senha` e `/definir-senha`; rota de seleção de atlética; rotas autenticadas para dashboard, membros, departamentos, eventos, tarefas, financeiro, patrimônio, documentos, perfil, gestões e suporte. Criar telas 404 e acesso negado.

### F02 Criar o visual e os componentes compartilhados

**Referência:** protótipo inteiro. **Cobre:** RNF11 a RNF16.

Criar variáveis de cor, tipografia, espaçamento, borda, raio e sombra. Reproduzir barra lateral escura, cor primária roxa, fundo claro, cartões brancos, cabeçalho com usuário e área de ajuda. As cores devem ser extraídas visualmente ou ajustadas com cuidado; não apresentar valores aproximados como especificações do PDF.

Criar Button, Input, Select, Badge, Card, Modal, Table, Pagination, Tabs, EmptyState, LoadingState, ErrorState, ConfirmDialog e mensagens de sucesso/erro. Botões de envio mostram progresso e evitam duplicidade. Formulários têm rótulos, instruções e erros associados aos campos.

No celular, menu lateral vira painel recolhível; cartões empilham; tabelas mantêm leitura por rolagem controlada ou apresentação em cartões. Modais gerenciam foco e fecham por teclado quando apropriado. Contraste e status não podem depender somente de cor.

### F03 Criar o cliente HTTP e a integração de dados

**Depende de:** F01, B02 e B06.

Centralizar `fetch`, JSON, erros e cookies em `lib/http.ts`. Usar credenciais de sessão, obter token CSRF e enviá-lo nos métodos de alteração. Atualizar o token após login/logout conforme o contrato de segurança. Não definir manualmente `Content-Type` para FormData, pois o navegador monta o multipart.

Configurar TanStack Query, chaves contendo a atlética atual e os filtros, cancelamento de requisições antigas, carregamento e tratamento de falha. Ao trocar de atlética ou sair, limpar dados da sessão e do cache; dados de outra atlética não podem aparecer nem por um instante devido a cache compartilhado.

Não armazenar senha, cookie de sessão ou token secreto de recuperação no `localStorage`. Em 401, encerrar o estado autenticado e levar ao login com mensagem de sessão expirada. Em 403, mostrar falta de permissão; em 409, pedir recarregamento do registro antes de reenviar. Nunca converter resposta de erro em uma lista vazia que pareça sucesso.

### F04 Criar login e seleção de atlética

**Depende de:** F02, F03 e B06. **Referência:** página 1. **Cobre:** RF01.

Montar login com e-mail institucional, senha, botão entrar e link esqueci minha senha, preservando a composição de duas áreas no computador. Validar campos, apresentar erro genérico de credenciais e impedir envio repetido. Depois do login, consultar os dados reais da conta.

Com vários vínculos ativos, apresentar seletor de atlética e confirmar a escolha na API. Com apenas um, entrar diretamente. Usar as capacidades retornadas para navegação, sem tratar o menu oculto como proteção suficiente. Logout deve chamar o backend e limpar cache.

### F05 Criar as telas de senha

**Depende de:** F04 e B07. **Cobre:** RF02 e apoio à entrada de membros.

Criar solicitação de recuperação por e-mail com mensagem genérica; redefinição com nova senha e confirmação; definição de senha por convite. Mostrar quando o link expirou ou já foi usado e permitir solicitar outro.

Após consumir um token, removê-lo da URL e do estado temporário. Não enviar o token para ferramentas de análise nem carregá-lo em recursos de terceiros. A senha de confirmação é validação de formulário; não precisa ser persistida nem retornar da API.

### F06 Criar a tela de membros e seus formulários

**Depende de:** F04 e B08. **Referência:** página 3. **Cobre:** RF09 a RF14.

Montar tabela com membro, curso, cargo, departamento, status e ações. Incluir busca por nome/curso/e-mail, filtros combinados por departamento/status, paginação e total. Aplicar pequeno atraso na busca digitada e reiniciar a página quando filtros mudarem.

Criar formulários de cadastro e edição com os campos de B05 e B08; selects vêm da API. Mostrar pendente até aprovação, ação aprovar, reenviar convite quando permitido e inativar com apresentação de dependências. Inativação não usa texto de exclusão definitiva.

Exportar a lista filtrada pela rota própria. Exibir solicitações de alteração de vínculo para administradores e permitir decisão fundamentada. Se cargo demandar gerenciamento específico, criar aba administrativa de cargos/perfis, sem permitir que o membro edite o próprio acesso.

### F07 Criar departamentos

**Depende de:** F06 e B08. **Referência:** página 4. **Cobre:** RF15 a RF17.

Criar cartões com nome, descrição, responsável, avatar de iniciais e quantidade real de membros. Criar formulário com nome, descrição e responsável ativo elegível. Se ainda não houver responsável, orientar o fluxo criar em configuração → cadastrar/aprovar primeiro membro → designar responsável → ativar. Mostrar o estado em configuração apenas para a administração, sem apresentar um cadastro incompleto como departamento ativo.

Menu de ações permite editar e inativar conforme a capacidade. Se houver membros ativos ou responsabilidades em aberto, apresentar os impedimentos retornados pela API e o caminho para transferi-los.

### F08 Criar a lista e o formulário de eventos

**Depende de:** F04 e B11. **Referência:** página 5. **Cobre:** RF18 e RF22.

Criar abas todos, ativos, planejados e concluídos com contagens reais. Cartões mostram período, status, local, previsão/inscritos/confirmados/presentes com significado explícito, orçamento e ações ver detalhes/editar.

Criar formulário com nome, descrição, início/fim, local, cidade/UF, orçamento previsto, participantes esperados e status conforme transições permitidas. Valores de moeda são normalizados antes de enviar como decimal; não enviar `R$ 1.250,00` como valor numérico da API.

### F09 Criar detalhes e operação do evento

**Depende de:** F08, B12, B13, B14 e B19. **Referência:** página 6. **Cobre:** RF19 a RF24 e RF29.

Montar cabeçalho do evento e seções cronograma, responsáveis, checklist e resumo financeiro. Acrescentar abas de participantes e materiais de divulgação, ausentes da tela, mas exigidas pelos requisitos.

Cronograma permite gerir atividades por dia e horário; responsáveis mostram as três áreas. Participantes permite registrar, confirmar, marcar presença e cancelar conforme capacidade. Materiais permitem enviar e visualizar anexos. Para cada subárea, usar o respectivo contrato e validações.

Checklist atualiza as mesmas tarefas do quadro. Resumo mostra previsto, realizado, saldo e percentual; orçamento zero tem texto específico e excesso de orçamento permanece visível. Não recalcular esses valores a partir dos cartões de tarefas ou dos exemplos do protótipo.

### F10 Criar o quadro de tarefas

**Depende de:** F09 e B14. **Referência:** página 7. **Cobre:** RF25 a RF27 e RF29.

Criar colunas A Fazer, Em Andamento, Revisão e Concluído. Cartões mostram título, prioridade, responsável e prazo com indicação de atraso. Filtros: evento, responsável e prioridade; adicionar departamento quando útil à matriz de escopo.

Formulário de tarefa contém campos de B14. Inicialmente, usar botão/menu mover para coluna com teclado. Atualização visual deve esperar confirmação da API ou desfazer alteração otimista se falhar. Contagem das colunas precisa refletir os filtros.

### F11 Criar o painel de notificações

**Depende de:** F04 e B15. **Cobre:** RF28.

Adicionar indicador de avisos não lidos no cabeçalho. Abrir painel/lista com título, prazo, data e link para a tarefa; marcar como lida via API. Não simular avisos somente a partir da data do computador do usuário.

Evitar consultas automáticas que renovem indefinidamente uma sessão inativa. Nesta versão, carregar avisos ao abrir o painel e em navegação/interação relevante; o servidor deve aplicar a expiração configurada.

### F12 Criar o financeiro e a prestação de contas

**Depende de:** F04, B16 e B17. **Referência:** página 8. **Cobre:** RF30 a RF36.

Dividir, se necessário, em F12a visão e lançamentos; F12b aprovação e contas; F12c relatório.

Criar cartões de receitas, despesas, saldo do mês e saldo acumulado; gráfico por mês; filtros de período, categoria e tipo; tabela com data, descrição, categoria, valor e tipo. Deixar explícito se o filtro usa liquidação, vencimento ou competência.

Criar formulários nova receita/nova despesa, incluindo vencimento e evento opcional. Apresentar abas ou seções para pendências de aprovação, contas a pagar/receber e contas vencidas. Ações de aprovar, rejeitar, liquidar, cancelar ou estornar exigem capacidade e confirmação com dados da operação. Não mostrar botão para o autor aprovar a própria despesa se a política proposta for adotada.

Criar visualização de prestação de contas com os critérios do período e botão exportar CSV. Associados acessam a apresentação autorizada sem dados restritos de contrapartes/anexos. Revalidar resumo, tabela e gráfico após uma operação financeira.

### F13 Criar patrimônio e seus fluxos

**Depende de:** F04 e B20 a B22. **Referência:** página 9. **Cobre:** RF37 a RF42.

Criar cartões com total de unidades disponíveis, emprestadas, em manutenção e valor estimado. Mostrar também quantidade de cadastros quando usada. Tabela contém nome, categoria, quantidade total/disponível, conservação, localização e situação operacional. Lote com mais de uma situação pode mostrar quantidades e múltiplos indicadores.

Criar busca por nome, categoria, conservação e situação, cadastro/edição de item, retirada, devolução parcial e manutenção. Formulários exibem disponibilidade retornada pela API; o servidor faz a validação final de concorrência.

Criar área de conferências com abertura da campanha, quantidades esperadas/verificadas, divergências, responsável e progresso. Percentual conferido mostra qual campanha foi usada.

### F14 Criar documentos upload e versões

**Depende de:** F04 e B23. **Referência:** página 10. **Cobre:** RF43 a RF46.

Criar cartões por categoria e tabela com título/arquivo, categoria, envio, responsável e ações. Incluir busca, filtro e ordenação. Modal de upload recebe título, categoria, visibilidade, descrição e arquivo PDF, mostrando limite de tamanho.

Detalhe do documento lista as versões e permite publicar nova versão para quem tem capacidade. Exibir número, data, responsável e observação de cada versão. Não tratar nova versão como outro documento sem vínculo.

Visualizar o PDF por rota autenticada. Se for usado um Blob URL temporário, revogá-lo ao fechar e não guardá-lo como URL permanente. Download não usa diretório público nem acesso ao arquivo por um endereço sem autorização.

### F15 Criar perfil e histórico

**Depende de:** F04 e B09. **Referência:** página 11. **Cobre:** RF03 a RF05.

Criar abas Dados pessoais, Alterar senha e Histórico de atividades. Dados mostram nome, e-mail, telefone, cargo, departamento, curso/período, status, data de entrada, último acesso e permissão efetiva.

Nome, telefone e curso seguem a edição direta. Para cargo/departamento, apresentar ação solicitar alteração e sua situação; administradores autorizados usam o fluxo de decisão. Explicar na própria ação que a mudança depende da diretoria, sem deixar um campo aparentemente editável que não funciona.

Alterar senha pede atual, nova e confirmação; histórico lista ações reais paginadas e atividades recentes do resumo. Campos não previstos como editáveis não ganham funções adicionais automaticamente.

### F16 Criar administração e consulta de gestões

**Depende de:** F06, F07 e B24. **Cobre:** RF47 e RF48.

Criar área restrita para listar gestões, planejar a próxima e preparar transferência. Exibir composição atual e nova, substituições, pendências e prévia dos acessos. A confirmação final mostra exatamente quem perderá/receberá cargos e que registros continuarão abertos.

Criar seletor de gestão para consulta histórica nas áreas pertinentes. Deixar visível quando a consulta é de uma gestão encerrada; não disponibilizar edições comuns em registros arquivados. Consultar histórico não troca a atlética da sessão nem devolve privilégios da época.

### F17 Criar o dashboard com dados reais

**Depende de:** telas dos domínios e B25. **Referência:** página 2. **Cobre:** RF06 a RF08.

Montar os quatro indicadores, gráfico de receitas e despesas de seis meses, participação por departamento, próximos eventos, tarefas pendentes e últimas movimentações. Links levam para listagens com filtros coerentes. Os percentuais e variações vêm do contrato de B25.

Usar componentes responsivos do Recharts e alternativa textual/tabela para leitura dos dados. Não usar gráfico como único acesso a informação importante. Mostrar estados vazios, carregamento parcial e falha de uma seção sem inutilizar toda a página.

### F18 Implementar ajuda acessibilidade e ajustes finais de interface

**Depende de:** F01 a F17 e B26/B28. **Cobre:** desenvolvimento de RNF10 a RNF17.

Disponibilizar ajuda em todas as telas pelo layout principal, com instruções curtas por contexto e central de suporte. Usar textos em português do Brasil, datas em pt-BR e moeda BRL. Formatar datas de calendário sem conversão acidental para o dia anterior.

Implementar navegação por teclado, foco visível, rótulos nos campos, mensagens de erro associadas, contraste adequado, leitura de tabelas e alternativas textuais para gráficos, considerando WCAG 2.1 AA. Modais devem direcionar o foco para seu conteúdo e devolvê-lo ao controle de origem ao fechar. Estados de prioridade, sucesso e erro também precisam de texto ou ícone, sem depender apenas da cor.

Adaptar menu, cartões, formulários, tabelas e quadros a celular, tablet e computador. Usar recursos compatíveis com Chrome, Firefox, Edge e Safari atuais. Aplicar carregamento de rotas sob demanda, paginação e mensagens úteis de falha, sem carregar arquivos nas listagens.

Criar no README os comandos para instalar dependências com npm, iniciar o frontend e gerar o build. Informar a porta e a configuração do proxy para o backend. Manter execução local com Maven e npm, sem infraestrutura adicional obrigatória.
