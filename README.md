# NotBiscoito

*aqui temos bolacha de verdade*

Tarefa 4 de MC322 - Programação Orientada a Objetos (Unicamp).

Simulação de uma planta industrial de bolacha, pelo terminal. Você escolhe o
cenário (Ideal ou Apocalíptico), registra demandas, compra matéria-prima com um
budget limitado, escolhe a estratégia que decide qual demanda vai para o forno,
e cuida das máquinas antes que elas parem a linha.

## Integrantes

- Pedro Henrique Magalhães - RA 159928
- Diego Reis - RA 277140

## Como compilar e rodar

Precisa do JDK 17 ou mais novo. Da raiz do repositório (no Windows, troque
`./gradlew` por `gradlew.bat`):

```
./gradlew build      # compila e monta o projeto
./gradlew run        # abre a fábrica no terminal
./gradlew javadoc    # gera a documentação em build/docs/javadoc
```

## Classes

Produtos (hierarquia por **tipo**, sabor é atributo):

- `Produto` - classe abstrata da bolacha
- `BolachaAmanteigada` - qualidade 0.9, come 15 g de massa
- `BolachaTradicional` - qualidade 0.7, come 10 g
- `BolachaCracker` - qualidade 0.5, come 6 g
- `StatusBolacha` - enum: da massa crua até aprovada ou reprovada

Máquinas (hierarquia por **comportamento**):

- `Maquina` - classe abstrata do equipamento
- `Estampadeira` - corta a massa; às vezes o corte sai torto
- `Forno` - assa; queima mais fácil a bolacha de qualidade alta
- `EstacaoInspecao` - aprova ou reprova, e às vezes erra
- `EstadoMaquina` - enum: rodando, pedindo manutenção ou quebrada

Estratégias de produção (padrão Strategy):

- `EstrategiaProducao` - interface que escolhe a próxima demanda
- `EstrategiaFilaDoForno` - quem pediu primeiro, assa primeiro
- `EstrategiaLoteFesta` - a maior encomenda passa na frente
- `EstrategiaCestaCheia` - a maior encomenda que o budget consegue pagar

O resto:

- `MateriaPrima` - a massa e os ingredientes de sabor
- `Demanda` - quanto de cada bolacha foi pedido
- `StatusDemanda` - enum: pendente, em produção, concluída ou cancelada
- `Auditavel` - interface de diagnóstico, de máquinas e de bolachas
- `Cenario` - enum com budget, desgaste e falha de cada cenário
- `GerenciadorProducao` - o cérebro: budget, armazém, demandas, a linha e a estratégia atual
- `Painel` - tudo que aparece no terminal
- `Main` - monta a fábrica e roda o menu

Erros da planta (todos herdam de `PlantaException`):

- `PlantaException` - raiz dos erros de regra de negócio
- `RecursoInsuficienteException` - faltou massa ou ingrediente
- `OrcamentoInsuficienteException` - o budget não cobre a conta
- `DemandaInvalidaException` - demanda inexistente ou em estado que não aceita a operação
- `MaquinaIndisponivelException` - máquina quebrada ou desligada
