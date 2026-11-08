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

## Cargos e departamentos

Os contratos de `Cargo` e `Departamento` já estão descritos no OpenAPI, mas as rotas serão incluídas em B08, quando a autorização por perfil estiver conectada à sessão. Departamentos são criados em configuração e não trazem contagem de membros persistida.

## Usuários e membros

`Usuario` não expõe senha, hash ou token. `Membro` representa o vínculo por atlética, iniciado como pendente; o acesso só é elegível depois de sua ativação e da definição de credencial.
