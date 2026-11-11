# Contratos da API

O contrato-base está em [openapi.yaml](./openapi.yaml). Todas as rotas são relativas a `/api/v1`; a documentação de cada endpoint será adicionada a esse arquivo no momento de sua implementação.

## Padrões transversais

- Não há exposição de entidades JPA nos contratos HTTP.
- DTOs de criação, alteração e resposta têm papéis distintos.
- A alteração de um recurso concorrente recebe `version`; conflitos de versão retornam `409`.
- Valores monetários são strings decimais com duas casas, como `"1250.90"`.
- A documentação de campos e ordenações permitidas é obrigatória para cada endpoint paginado.

## Escopo de atlética

Os endpoints de domínio usarão a atlética da sessão, e não um `atleticaId` fornecido pelo cliente. Recursos são buscados pelo par `id + atleticaId do contexto`; registros de outra atlética retornam `404`.

Nesta etapa não há endpoint público para criar ou administrar atléticas. O perfil local `dev` cria os dados de demonstração.

## Membros

- `GET /membros` aceita `busca`, `departamentoId`, `status`, `page`, `size` e `sort`; os únicos campos ordenáveis são `nome`, `email`, `curso`, `status`, `cargo`, `departamento` e `membroDesde`.
- `POST /membros` sempre cria o vínculo como `PENDENTE`. Caso o e-mail já pertença a uma conta, nome e telefone do pedido não mudam essa identidade global.
- `PUT /membros/{id}`, `PATCH /membros/{id}/aprovar` e `PATCH /membros/{id}/inativar` requerem `version`; são operações administrativas e retornam `409` em versão desatualizada.
- `GET /membros/exportacao` gera um CSV UTF-8 completo para os filtros informados, não apenas a página exibida. A saída escapa CSV e neutraliza fórmulas de planilha.
- Senhas, hashes e tokens nunca aparecem. E-mail e telefone são retornados apenas ao administrador; outros perfis autorizados recebem a visão sem contato.

## Cargos e departamentos

- `GET/POST /cargos` e `PUT /cargos/{id}` são administrativos. A inativação de cargo é feita por `PUT` com `ativo: false`, sem apagar histórico.
- `GET/POST /departamentos`, `GET/PUT /departamentos/{id}` e as ações `PATCH /responsavel`, `/ativar` e `/inativar` usam o mesmo escopo da sessão.
- Departamentos são criados em `EM_CONFIGURACAO`. A administração pode criar, aprovar e designar o primeiro responsável antes de ativá-los. A resposta inclui responsável e `quantidadeMembros`, calculada pelos vínculos `ATIVO` e nunca persistida manualmente.
- A inativação exige transferências e substituição das responsabilidades antes de preservar o departamento como `INATIVO`.

## Perfil, histórico e solicitações de vínculo

- `GET /perfil` traz o perfil do vínculo selecionado. `PUT /perfil` só altera nome, telefone, curso e período; o e-mail permanece somente leitura.
- `PUT /perfil/senha` exige senha atual, nova senha e `version`; retorna `204` e encerra as demais sessões da conta.
- `GET /perfil/atividades` lista os registros de atividade da própria conta na atlética selecionada, por padrão do mais recente ao mais antigo. As ordenações aceitas são `ocorridaEm`, `acao` e `tipoRecurso`.
- `POST /perfil/solicitacoes-vinculo` cria uma solicitação pendente. `GET /membros/solicitacoes-vinculo` e `PATCH /membros/solicitacoes-vinculo/{id}/decisao` são administrativos; a aprovação aplica a mudança de cargo/departamento na mesma transação.
- Cada solicitação conserva os IDs de cargo e departamento anteriores, além da decisão, avaliador, datas e versões necessárias para evitar aprovação de dados desatualizados.

## Gestões

- `GET /gestoes`, `GET /gestoes/{id}` e `POST /gestoes` são administrativos e sempre usam a atlética selecionada.
- A criação gera uma gestão `PLANEJADA`; sua ativação pertence ao fluxo de sucessão da etapa B24. A consulta de uma gestão `ENCERRADA` é somente histórica e não concede novamente as permissões do período.
- As datas de início e fim prevista são obrigatórias, e o início não pode ser posterior ao fim previsto.
- A infraestrutura de ativação bloqueia a atlética na transação e impede duas gestões `ATIVA` simultâneas. Ao ativar, abre os períodos de vínculo dos membros ativos.
- Mudanças de cargo ou departamento encerram o período de `VinculoGestao` em aberto e criam outro; o histórico anterior não é alterado.

## Eventos

- `GET /eventos` recebe `busca`, `inicioDe`, `inicioAte`, `status`, `grupoStatus`, `gestaoId`, `page`, `size` e `sort`. As ordenações permitidas são `nome`, `inicio`, `fim`, `status`, `orcamentoPrevisto` e `createdAt`.
- `grupoStatus=ATIVOS` inclui apenas `INSCRICOES_ABERTAS` e `EM_ANDAMENTO`. A página traz `contagensPorStatus` e `totalAtivos`, calculados com a mesma busca, período e gestão, sem o filtro da aba atual.
- `POST /eventos` cria o evento como `PLANEJADO` e associa automaticamente a gestão ativa, quando houver. O orçamento é texto decimal não negativo com duas casas; `"0.00"` é válido para evento gratuito.
- `PATCH /eventos/{id}/status` permite `PLANEJADO → INSCRICOES_ABERTAS` ou `EM_ANDAMENTO`, `INSCRICOES_ABERTAS → EM_ANDAMENTO` e `EM_ANDAMENTO → CONCLUIDO`. Correções para estado anterior exigem administração e motivo registrado. Não há status de cancelamento.

## Participações de eventos

- `GET/POST /eventos/{eventoId}/participacoes`, a alteração de status e o resumo são exclusivos do organizador (membro que criou o evento) ou da administração. Participantes comuns não recebem a lista que contém dados de convidados externos.
- Uma participação é interna com `membroId` de vínculo ativo, ou externa com `nomeParticipante` e `emailParticipante`. O e-mail externo é normalizado e único por evento; nenhum convite ou conta é criado.
- Um membro só participa uma vez de cada evento. O status avança de `INSCRITO` para `CONFIRMADO` e `PRESENTE`; `CANCELADO` preserva o histórico. A retirada de presença volta para `CONFIRMADO`, exige motivo e é auditada.
- O resumo usa nomes explícitos: `inscritos` inclui inscritos, confirmados e presentes; `confirmados` inclui confirmados e presentes; `presentes` inclui somente presentes. `participantesEsperados` é apenas uma previsão e pode não estar preenchido.

## Cronograma e responsáveis

- `GET/POST /eventos/{eventoId}/cronograma`, `PUT/DELETE /eventos/{eventoId}/cronograma/{atividadeId}` são operações de organização. O cronograma ordena por data completa, horário, ordem e ID; início e fim devem caber no período do evento.
- `GET/POST /eventos/{eventoId}/responsaveis`, `PUT/DELETE /eventos/{eventoId}/responsaveis/{responsabilidadeId}` administram as áreas `COORDENACAO_GERAL`, `FINANCEIRO` e `COMUNICACAO`. Há um responsável ativo por área, mas uma pessoa pode acumular áreas.
- A substituição encerra a designação anterior e cria outra. A remoção também apenas encerra a designação; `incluirHistorico=true` permite consultar as anteriores. Todas as ações exigem `version` e são auditadas.

## Tarefas e checklist

- `GET /tarefas` aceita `eventoId`, `responsavelMembroId`, `departamentoId`, `prioridade`, `status`, `prazoDe`, `prazoAte`, `atrasada`, `page`, `size` e `sort` (`prazo`, `titulo`, `createdAt`). Membro comum só recebe as tarefas atribuídas a ele.
- `atrasada` é derivada: prazo anterior ao dia atual no fuso da atlética e status diferente de `CONCLUIDO`. Prioridade e atraso são atributos distintos.
- O responsável deve ser membro `ATIVO` da atlética; a troca de responsável (`PUT`) é de quem gerencia a tarefa.
- `PATCH /tarefas/{id}/status` serve ao quadro e ao checklist. Retorno para `EM_ANDAMENTO` (de `REVISAO` ou `CONCLUIDO`) exige `motivo`; concluir direto é permitido ao responsável e à coordenação autorizada.
- `GET /eventos/{eventoId}/checklist` deriva total, concluídas e progresso (0 quando não há tarefas, com mensagem explicativa).

## Notificações de prazo

- A rotina roda para cada atlética ativa com o fuso dela. Intervalo: `athletix.notificacoes.intervalo-ms` (padrão 1 h); espera inicial: `athletix.notificacoes.atraso-inicial-ms`.
- Avisa `PRAZO_PROXIMO` no dia anterior ao prazo e `ATRASADA` uma vez por dia enquanto a tarefa estiver aberta. Não notifica tarefas concluídas nem responsáveis não `ATIVO`. Tarefas pós-evento seguem notificáveis.
- Unicidade: destinatário + tarefa + tipo + data de referência. A reatribuição passa a avisar o responsável atual.
- `GET /notificacoes` (paginado; padrão mais recentes primeiro), `GET /notificacoes/resumo` e `PATCH /notificacoes/{id}/ler` só enxergam as notificações da própria pessoa.

## Financeiro — categorias e lançamentos

- `GET/POST /financeiro/categorias` são exclusivos de administrador e financeiro. Categoria é própria da atlética, tem nome único sem distinção de caixa/espaços externos e se aplica a `RECEITA`, `DESPESA` ou ambos.
- `GET/POST /financeiro/lancamentos` e `GET/PUT /financeiro/lancamentos/{id}` usam `valor` como texto decimal; o servidor persiste `DECIMAL(19,2)`, aplica arredondamento `HALF_UP` e responde com duas casas.
- Receita nasce em `ABERTO` com aprovação `NAO_APLICAVEL`; despesa nasce em `ABERTO` e `PENDENTE`. Vencimento é obrigatório enquanto aberto. Aprovação e liquidação são operações da B17.
- A listagem filtra por `modoData` (`COMPETENCIA`, `VENCIMENTO` ou `LIQUIDACAO`), intervalo inclusivo, categoria, tipo, situação, aprovação, evento e gestão. Sem `modoData`, o relatório usa competência. `VENCIDA` é calculada no fuso da atlética.
