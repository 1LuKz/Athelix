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
