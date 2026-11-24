# UI/UX do Athletix

## Referências e escopo

O protótipo Athletix orienta a identidade, a navegação e os agrupamentos das telas.
O guia UI/UX Apple + Spotify orienta hierarquia, legibilidade, estados e acessibilidade.
As preferências do projeto definem a apresentação final: janela fixa, sem paginação
na interface e com rolagem de conteúdo restrita às tabelas.

## Estrutura e rolagem

- Todas as telas, o menu, o login e os pop-ups usam conteúdo contínuo, sem botões
  de página anterior/próxima. O conteúdo principal não tem rolagem.
- Login, recuperação e definição de senha preservam formulários convencionais:
  rótulos acima dos campos, campos empilhados e uma ação principal. Não utilizar
  `FormFields` ou tabelas para credenciais. O login mantém a composição em duas
  colunas do protótipo, com o brasão centralizado conforme a preferência do projeto.
  O painel direito centraliza os textos e o brasão. Usar apresentação curta,
  títulos menores e espaçamento de 8 px entre os elementos do formulário.
- Listas de eventos, departamentos e tarefas usam tabelas. Documentos, membros,
  financeiro, patrimônio, atividades e gestões seguem o mesmo padrão.
- Cronograma, responsáveis, checklist, materiais e notificações também usam tabelas.
- Tabelas têm altura limitada pelo painel, rolagem vertical/horizontal interna,
  cabeçalhos fixos, nome acessível e foco por teclado.
- Em todas as tabelas, ações das linhas usam ícones com `aria-label`, dica de
  identificação e alvo de 44 px.
  As ações ficam lado a lado sem quebra de linha, inclusive em telas estreitas.
  As tabelas de dados usam espaçamento compacto e informações em uma linha por
  célula, sem textos secundários empilhados. Conteúdo extenso permanece completo
  pela rolagem horizontal da própria tabela. Campos de formulários mantêm dicas e erros.
  Usar `Button` com `iconOnly`; ações de navegação continuam como links.
- Seletores de ações nas linhas mantêm rótulos acessíveis, ocultos visualmente.
  Notificações exibem o título e o prazo, sem a descrição abaixo do título.
  O painel de notificações não tem rolagem horizontal: a tabela ocupa a largura
  disponível e os títulos usam até duas linhas, com o texto completo na dica do link.
- Os formulários operacionais extensos apresentam cada campo e seu valor em uma linha de tabela.
  Rótulos, dicas, erros e registros do formulário são preservados. Botões de salvar,
  cancelar e navegar permanecem fora da área rolável.
- Filtros são apresentados em tabelas horizontais próprias, com os controles
  associados aos seus rótulos, sem consumir a altura de várias linhas de campos.
- Em janelas estreitas ou baixas, abas e navegação lateral oferecem seletores
  compactos com os mesmos destinos e permissões.
- Evolução financeira tem uma aba própria. Dados pessoais e vínculo têm seções
  próprias. Os grupos de detalhes de evento podem ser selecionados por assunto.
- O brasão fica centralizado no menu e acima do login, sem nome ao lado. O menu
  não exibe o nome da atlética.

## Carregamento dos registros

Os contratos HTTP continuam aceitando lotes paginados. O frontend usa
`carregarColecao` para reunir todos os lotes dos filtros ativos e exibir uma lista
contínua. Falhas rejeitam a consulta completa, e cancelamento impede novas
requisições; o frontend não apresenta um lote parcial como se fosse a coleção toda.
Permissões e isolamento por atlética permanecem nas consultas existentes.

## Dashboard

O dashboard exibe indicadores e painéis analíticos em uma área fixa. As capacidades
da sessão controlam as consultas e os destinos disponíveis. O excedente da
participação por departamento é agrupado em “Outros departamentos”. Os valores do
gráfico têm uma tabela acessível a leitores de tela.

## Organização dos estilos

- `tokens.css`: cores semânticas, tipografia, espaços, raios, sombras e movimento.
- `global.css`, `compact.css`, `experience.css`: estrutura e componentes compartilhados.
- `dashboard.css`: composição fixa do dashboard.
- `fixed-layout.css`: regra final de tamanho dos painéis e rolagem somente em tabelas.
- `auth.css`: composição das telas de acesso conforme o protótipo e o guia de UI/UX,
  independente das regras de tabelas das telas operacionais.

Os arquivos ficam em `frontend/src/styles` e são importados nessa ordem em `main.tsx`.
Reutilizar `Table`, `FormFields`, `FormContent` e `FilterTable` nas telas operacionais.
Nas telas de acesso, usar os componentes de campo diretamente no formulário.
Não reintroduzir contêineres de rolagem no menu, nas telas ou no corpo de modais.

## Acessibilidade e validação

Manter foco visível, navegação por teclado, Escape e retorno de foco dos modais.
Mensagens de erro podem ser dispensadas e não apagam o trabalho digitado.
Preferências de movimento reduzido, mais contraste e cores forçadas são respeitadas.

Na pasta `frontend`, executar:

```sh
npm run build
npm run lint
npm test
```

Os testes incluem carregamento de coleções completas, falhas intermediárias,
cancelamento, apresentação dos formulários em tabelas e permissões do dashboard.
A validação visual em navegador, Safari real, zoom de 200% e leitor de tela
permanece pendente: o acesso ao navegador local foi recusado pelo controle de permissões.
