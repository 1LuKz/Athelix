# Contratos da API

O contrato-base está em [openapi.yaml](./openapi.yaml). Todas as rotas são relativas a `/api/v1`; a documentação de cada endpoint será adicionada a esse arquivo no momento de sua implementação.

## Padrões transversais

- Não há exposição de entidades JPA nos contratos HTTP.
- DTOs de criação, alteração e resposta têm papéis distintos.
- A alteração de um recurso concorrente recebe `version`; conflitos de versão retornam `409`.
- Valores monetários são strings decimais com duas casas, como `"1250.90"`.
- A documentação de campos e ordenações permitidas é obrigatória para cada endpoint paginado.
