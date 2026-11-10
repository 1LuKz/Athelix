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
