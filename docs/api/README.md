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
- Receita nasce em `ABERTO` com aprovação `NAO_APLICAVEL`; despesa nasce em `ABERTO` e `PENDENTE`. Vencimento é obrigatório enquanto aberto.
- A listagem filtra por `modoData` (`COMPETENCIA`, `VENCIMENTO` ou `LIQUIDACAO`), intervalo inclusivo, categoria, tipo, situação, aprovação, evento e gestão. Sem `modoData`, o relatório usa competência. `VENCIDA` é calculada no fuso da atlética.

## Financeiro — decisões, caixa e prestação de contas

- `PATCH /financeiro/lancamentos/{id}/aprovacao`, `POST /liquidacao` e `PATCH /cancelamento` exigem administrador ou financeiro e `version`. Uma despesa é decidida uma única vez; quem a criou não pode aprová-la ou rejeitá-la.
- Receitas podem ser liquidadas diretamente; despesas só depois de `APROVADA`. Uma baixa repetida não cria outro movimento. Lançamento aberto pode ser cancelado com motivo; pagamento/recebimento efetivado somente é revertido por `POST /estorno` integral.
- Associados ativos consultam `GET /financeiro/resumo`, `/evolucao-mensal`, `/contas` e `/relatorio`, sempre sem dados pessoais de contrapartes ou anexos. O resumo calcula saldo disponível pelo regime de caixa; a evolução preenche meses sem movimento e contas abertas não entram no saldo disponível. Despesa rejeitada é consultável em lançamentos, mas não é pendência pagável.
- `GET /financeiro/relatorio` exporta CSV UTF-8 do intervalo solicitado, com saldo inicial, entradas, saídas, estornos, saldo final e pendências a pagar/receber separados. O saldo inicial cadastrado é zero nesta etapa.
- No perfil `dev`, há um segundo usuário autorizado para validar a separação de aprovação: `financeiro@athletix.local` / `Financeiro@2026`.

## Arquivos protegidos

- Não existe rota pública de upload. Os módulos de documento, divulgação e evento devem autorizar a operação, armazenar o arquivo pela infraestrutura comum e persistir o vínculo ao recurso pai na mesma transação.
- O limite por arquivo é 10 MiB (`athletix.arquivos.max-tamanho-bytes`); `spring.servlet.multipart.max-file-size` e `max-request-size` têm limite compatível. O teto por atlética é configurável em `athletix.arquivos.orcamento-por-atletica-bytes`.
- Documento aceita PDF; divulgação aceita PDF, PNG e JPEG. A assinatura binária, extensão e MIME declarado devem coincidir. SVG, HTML, scripts e executáveis são rejeitados.
- `GET /arquivos/{id}/conteudo` exige sessão, escopo de atlética e autorização explícita do recurso pai. Não há listagem com BLOB. A resposta usa `nosniff`, cache privado sem armazenamento e PDF inline após autorização.

## Anexos e financeiro de eventos

- `GET/POST /eventos/{eventoId}/anexos` lista ou envia anexos multipart. `ARTE` e `OUTRO` aceitam PDF/PNG/JPEG; `REGULAMENTO` aceita PDF. As operações são de organização do evento ou administração; a lista traz metadados, nunca BLOB.
- `DELETE /eventos/{eventoId}/anexos/{anexoId}?version=` remove apenas o vínculo, antes do encerramento do evento, e preserva o arquivo. A leitura continua exclusivamente em `GET /arquivos/{id}/conteudo`, que revalida a autorização do evento.
- `GET /eventos/{eventoId}/financeiro/resumo?dataReferencia=` entrega agregados financeiros a membros ativos. `realizado` considera despesas aprovadas e liquidadas menos seus estornos; `receitasRealizadas` é independente e não reduz o orçamento gasto. Para orçamento zero, `percentualUtilizado` é `null` e `situacao` é `SEM_ORCAMENTO`; percentuais acima de 100 não são limitados.

## Patrimônio

- `GET/POST /patrimonio/categorias` permite listar categorias da atlética e criar novas categorias administrativas.
- `GET/POST /patrimonio/itens`, `GET/PUT /patrimonio/itens/{id}` usam lote homogêneo e valor unitário decimal textual. A redução de quantidade exige `justificativaReducao`, `version` e não pode ultrapassar a disponibilidade.
- `GET /patrimonio/resumo` traz quantitativos e valor estimado total dos lotes ativos. Conservação e situação operacional são campos independentes; empréstimos e manutenção compõem o cálculo de disponibilidade.
- `GET/POST /patrimonio/emprestimos` registra retiradas e consulta saldo aberto calculado. `POST /patrimonio/emprestimos/{id}/devolucoes` aceita devolução parcial e, opcionalmente, inicia manutenção na mesma transação.
- `GET/POST /patrimonio/manutencoes` e `PATCH /patrimonio/manutencoes/{id}/concluir` controlam indisponibilidade temporária. Disponibilidade é `total − empréstimos abertos − manutenções abertas`; as operações bloqueiam o item para não reservar a mesma unidade duas vezes.

## Conferência de inventário

- `GET/POST /patrimonio/conferencias` lista ou abre uma campanha e fotografa os lotes ativos no instante da abertura.
- `GET /patrimonio/conferencias/{id}` traz a campanha, os itens fotografados e o progresso; `PUT /patrimonio/conferencias/{id}/itens/{itemId}` registra a contagem com versão.
- Diferenças em relação à quantidade esperada exigem observação. `PATCH /patrimonio/conferencias/{id}/encerrar` recebe a versão da campanha e encerra sem alterar o patrimônio.

## Biblioteca documental

- `GET/POST /documentos` consulta ou cria documentos; a criação é `multipart/form-data` e inclui um PDF como versão 1.
- `GET/PUT /documentos/{id}` consulta ou altera metadados, inclusive arquivamento, sempre com `version` na alteração.
- `GET/POST /documentos/{id}/versoes` lista ou publica versão imutável; `GET /documentos/{id}/versoes/{versaoId}/conteudo` devolve o PDF após conferir a visibilidade do documento.
- A lista pesquisa título e nome da versão atual, filtra categoria/gestão e omite arquivados por padrão. Cada resposta contém somente a versão atual, sem BLOB.

## Transferência de gestão

- `POST /gestoes/{id}/transicoes` abre o rascunho para a gestão planejada; `PUT /gestoes/transicoes/{id}` define a composição do destino com versão.
- `GET /gestoes/transicoes/{id}/previa` apresenta entradas, saídas, substituições e pendências; `POST /gestoes/transicoes/{id}/concluir` efetiva a troca atomicamente.
