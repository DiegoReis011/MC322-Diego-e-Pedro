# NotBiscoito

*aqui temos bolacha de verdade*

Tarefa 1 de MC322 - Programação Orientada a Objetos (Unicamp).

Simulação de uma planta industrial que recebe massa, assa bolachas e passa
elas pela inspeção, tudo pelo terminal.

## Integrantes

- Pedro Henrique Magalhães - RA 159928
- Diego Reis - RA 277140

## Como compilar e rodar

Da raiz do repositório:

```
javac -d bin $(find src -name "*.java")
java -cp bin Main
```

## Classes

- `MateriaPrima` - a massa que fica em estoque
- `Produto` - a bolacha
- `Maquina` - a estampadeira que assa
- `Esteira` - leva os itens de uma etapa pra outra
- `EstacaoInspecao` - confere a bolacha no final
- `Painel` - o que aparece no terminal
- `Main` - monta a fábrica e roda o menu
