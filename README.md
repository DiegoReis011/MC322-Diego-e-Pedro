# NotBiscoito

*aqui temos bolacha de verdade*

Tarefa 2 de MC322 - Programação Orientada a Objetos (Unicamp).

Simulação de uma planta industrial de bolacha, pelo terminal. Você registra
demandas, compra matéria-prima com um budget limitado, manda a fornada passar
pela linha de produção e torce pra inspeção aprovar.

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

Produtos (hierarquia por **tipo**, sabor é atributo):

- `Produto` - classe abstrata da bolacha
- `BolachaAmanteigada` - qualidade 0.9, come 15 g de massa
- `BolachaTradicional` - qualidade 0.7, come 10 g
- `BolachaCracker` - qualidade 0.5, come 6 g

Máquinas (hierarquia por **comportamento**):

- `Maquina` - classe abstrata do equipamento
- `Estampadeira` - corta a massa; às vezes o corte sai torto
- `Forno` - assa; queima mais fácil a bolacha de qualidade alta
- `EstacaoInspecao` - aprova ou reprova, e às vezes erra

O resto:

- `MateriaPrima` - a massa e os ingredientes de sabor
- `Demanda` - quanto de cada bolacha foi pedido
- `GerenciadorProducao` - o cérebro: budget, armazém, demandas e a linha
- `Painel` - tudo que aparece no terminal
- `Main` - monta a fábrica e roda o menu
