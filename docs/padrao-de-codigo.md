# Padrão de código

Estas regras valem para todo código novo ou alterado no Athletix.

1. Escreva uma instrução por linha e limite cada linha a 120 caracteres.
2. Use nomes completos e descritivos, em português do domínio ou em inglês técnico, sem misturar
   idiomas no mesmo arquivo. São proibidos nomes de variáveis, parâmetros, campos e atributos com
   uma ou duas letras, exceto `i` e `j` em laços de índice e `e` em `catch`. Evite abreviações
   obscuras: prefira `membroAtual` a `m`, `atleticaId` a `a`, `requisicao` a `r`,
   `corpoDaRequisicao` a `b`, `membrosDaAtletica` a `ms` e `valores`/`setValores` a `v`/`setV`.
   Nomeie funções de intenção, como `montarQueryString`, em vez de `q` ou `qs`.
3. Mantenha métodos com aproximadamente 30 linhas no máximo e uma única responsabilidade. Classes
   de serviço não devem conter lógica de apresentação.
4. Não use `any` no TypeScript nem `Object` ou `List<?>` em DTOs Java.
5. Limite páginas React a aproximadamente 200 linhas; extraia componentes, hooks `useXxx` e
   utilitários para arquivos próprios.

Use `./mvnw spotless:apply` no backend e `npm run format` no frontend antes de enviar mudanças.
Use `./mvnw spotless:check` e `npm run format:check` para verificar o padrão sem reformatar.
