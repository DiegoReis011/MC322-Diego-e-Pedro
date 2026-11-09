# NotBiscoito

*aqui temos bolacha de verdade*

Tarefa 6 de MC322 - Programação Orientada a Objetos (Unicamp): persistência em
XML, testes automatizados com JUnit 5 e organização em pacotes.

Simulação de uma planta industrial de bolacha, pelo terminal. Você escolhe o
cenário (Ideal ou Apocalíptico), registra demandas, compra matéria-prima com um
budget limitado, escolhe a estratégia que decide qual demanda vai para o forno,
cuida das máquinas antes que elas parem a linha e **salva a fábrica num arquivo
XML para continuar depois**.

## Integrantes

- Pedro Henrique Magalhães - RA 159928
- Diego Reis - RA 277140

## Como compilar, rodar e testar

Precisa do JDK 17 ou mais novo. Da raiz do repositório (no Windows, troque
`./gradlew` por `gradlew.bat`):

```
./gradlew build      # compila, roda os testes e monta o projeto
./gradlew run        # abre a fábrica no terminal
./gradlew test       # roda só os testes automatizados (sem menu, sem teclado)
./gradlew javadoc    # gera a documentação em build/docs/javadoc
```

O relatório HTML dos testes sai em `build/reports/tests/test/index.html`.

### Salvar e carregar

No menu principal:

- `7 - Salvar estado da planta` grava `dados/planta.xml`;
- `8 - Carregar estado da planta` lê o arquivo e **substitui** o estado atual.

Ao abrir o programa, se `dados/planta.xml` existir, ele pergunta se você quer
continuar a planta salva ou começar uma fábrica nova. **Na primeira execução o
arquivo não existe, e isso é normal**: a fábrica abre com os dados padrão do
projeto (decisão da dupla: um programa que nunca foi salvo não tem o que
recuperar, então não faz sentido tratar isso como falha). Se o arquivo existir
mas estiver estragado, o programa mostra o motivo, não mexe no arquivo e abre
uma fábrica nova. Encerrar o programa nunca apaga nem altera o save.

A pasta `dados/` não vai para o repositório (está no `.gitignore`).

## Organização em pacotes

Código em `src/main/java/br/unicamp/ic/mc322/`, testes em
`src/test/java/br/unicamp/ic/mc322/` (mesma árvore de pacotes):

| Pacote | O que mora aqui |
|---|---|
| `(raiz)` | `Main`: monta a fábrica e roda o menu |
| `model` | `Auditavel`, `Cenario`, `MateriaPrima`, `Demanda`, `StatusDemanda` |
| `model.produto` | `Produto` (abstrata), `BolachaAmanteigada`, `BolachaTradicional`, `BolachaCracker`, `StatusBolacha` |
| `model.maquina` | `Maquina` (abstrata, o Todo), `Estampadeira`, `Forno`, `EstacaoInspecao`, `EstadoMaquina`, `LinhaDeProducao` (o agrupador), `ParteDeMaquina`, `Motor`, `PainelDeControle`, `Engrenagem` |
| `strategy` | `EstrategiaProducao` e as três estratégias (padrão Strategy) |
| `service` | `GerenciadorProducao`, `EstadoPlanta` (o retrato que vai e volta da persistência), `FabricaNotBiscoito` (fábrica padrão) |
| `persistence` | `PersistenciaPlanta`: o único código que conhece o XML |
| `exception` | `PlantaException` e as exceções da planta e da persistência |
| `ui` | `Painel`: tudo que aparece no terminal |

Sentido das dependências: `ui` e `Main` usam `service`; `service` usa `model`,
`strategy` e `exception`; `persistence` usa `model` e `service.EstadoPlanta`;
**o domínio (`model`) não conhece XML nem arquivos**.

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

Composição e agrupamento (Tarefa 5):

- `LinhaDeProducao` - o agrupador: as máquinas na ordem da bolacha (agregação), com auditoria em cascata
- `ParteDeMaquina` - peça interna, com saúde e desgaste próprios
- `Motor` - 1:1 com a máquina; motor gasto encarece a operação, e motor quebrado para a máquina
- `PainelDeControle` - 1:1; painel descalibrado aumenta a chance de falha
- `Engrenagem` - 1:N; cada engrenagem gasta aumenta a chance de falha

A máquina cria as próprias peças (composição) e ninguém de fora as enxerga. O
diagrama UML está em `docs/diagrama-uml-tarefa5.png`.

Persistência (Tarefa 6):

- `PersistenciaPlanta` - salva e carrega o XML; valida tudo antes de devolver o estado
- `EstadoPlanta` - retrato da planta (budget, despensa, máquinas, demandas, armazém), sem XML
- `FabricaNotBiscoito` - monta a fábrica padrão (usada na primeira execução e nos testes)

O resto:

- `MateriaPrima` - a massa e os ingredientes de sabor
- `Demanda` - quanto de cada bolacha foi pedido
- `StatusDemanda` - enum: pendente, em produção, concluída ou cancelada
- `Auditavel` - interface de diagnóstico, de máquinas e de bolachas
- `Cenario` - enum com budget, desgaste e falha de cada cenário
- `GerenciadorProducao` - o cérebro: budget, armazém, demandas, a linha e a estratégia atual
- `Painel` - tudo que aparece no terminal

Erros (todos herdam de `PlantaException`):

- `RecursoInsuficienteException` - faltou massa ou ingrediente
- `OrcamentoInsuficienteException` - o budget não cobre a conta
- `DemandaInvalidaException` - demanda inexistente ou em estado que não aceita a operação
- `MaquinaIndisponivelException` - máquina quebrada ou desligada
- `PersistenciaException` - falha de leitura ou escrita, arquivo vazio ou XML malformado
- `DadosInvalidosException` (é uma `PersistenciaException`) - XML válido, mas com dados que contrariam as regras da fábrica
- `ArquivoInexistenteException` (é uma `PersistenciaException`) - não há save para carregar

## Formato do XML

A hierarquia do arquivo espelha a composição da Tarefa 5 (máquina contém motor,
painel e engrenagens):

```xml
<planta versao="1">
  <cenario>IDEAL</cenario>
  <budget>9982.5</budget>
  <lotes>1</lotes>
  <despensa>
    <materiaPrima funcao="massa" id="MP001">
      <nome>massa da casa</nome> <quantidade>4925.0</quantidade> <unidade>g</unidade>
      <quantidadeMinima>200.0</quantidadeMinima> <custoPorUnidade>0.3</custoPorUnidade>
    </materiaPrima>
    <!-- um <materiaPrima funcao="ingrediente"> por sabor -->
  </despensa>
  <linha>
    <maquina tipo="Forno">
      <capacidadeMaxima>500.0</capacidadeMaxima> <probabilidadeFalhaBase>0.3</probabilidadeFalhaBase>
      <custoOperacional>2.0</custoOperacional>
      <saude>98.1</saude> <ciclos>1</ciclos> <falhas>0</falhas>
      <motor saude="97.0"/>
      <painelDeControle saude="99.2"/>
      <engrenagens>
        <engrenagem numero="1" saude="96.4"/> <!-- ... -->
      </engrenagens>
    </maquina>
  </linha>
  <demandas>
    <demanda tipoProduto="Bolacha cracker de coco" quantidade="20" status="CONCLUIDA" custoUnitarioEstimado="3.5"/>
  </demandas>
  <armazem>
    <bolacha tipo="BolachaCracker" id="B003-L1-1" sabor="coco" status="APROVADA" lote="1" risco="0.0"/>
  </armazem>
</planta>
```

## Escopo e Limitações

### Dados persistidos vs. não persistidos

**Persistidos obrigatórios** (o mínimo para recuperar um estado útil e comparável):

- **Estoque de matérias-primas**: a massa da casa e cada ingrediente de sabor, com
  quantidade atual, unidade, estoque mínimo e preço. Sem isso a fábrica
  recuperada produziria com uma despensa diferente da que foi salva.
- **Máquinas e subcomponentes (composição)**: o tipo de cada máquina, a ordem na
  linha, a configuração (capacidade, chance de falha base, custo, quantidade de
  engrenagens) e o estado (saúde, fornadas feitas, falhas no histórico, e a
  saúde do motor, do painel de controle e de cada engrenagem). Ao carregar, a
  máquina é recriada pelo próprio construtor, que cria as suas peças
  (composição), e só então o estado é devolvido a elas. Nenhum getter da
  máquina entrega as peças.
- **Demandas**: bolacha, quantidade, custo unitário estimado e `StatusDemanda`.
  O status é reconstruído pelas transições da própria `Demanda`
  (`iniciarProducao`, `atender`, `cancelar`), então o arquivo não consegue criar
  uma demanda num estado que o domínio proíbe.

**Persistidos opcionais, escolhidos pela dupla:**

- **Budget**: o dinheiro em caixa é parte do estado do jogo; sem ele, carregar
  devolveria o caixa cheio e apagaria o efeito das compras e dos reparos.
- **Cenário**: define o fator de falha e o desgaste das máquinas.
- **Número de lotes**: mantém a numeração das fornadas (`B001-L3-2`) sem repetir ids.
- **Armazém**: as bolachas aprovadas (tipo, sabor, status, lote e risco acumulado).

**Mantidos só em memória, de propósito:**

- **Catálogo de bolachas**: é configuração fixa do programa (três moldes criados
  em `FabricaNotBiscoito`), não estado que muda durante o uso. Ele também serve
  de regra de validação: demanda ou bolacha de um tipo que o catálogo não tem
  é recusada ao carregar.
- **Estratégia de produção atual**: é uma preferência da sessão, trocada em
  um clique; ao abrir o programa volta para a Fila do Forno.
- **Máquina ligada ou desligada**: a linha só liga as máquinas durante uma
  fornada e desliga no fim, então no momento de salvar elas estão sempre desligadas.
- **Bolachas reprovadas (refugo)**: a fábrica nunca as guardou, vão para o lixo.
- **Contadores estatísticos**: `EstacaoInspecao.getTotalInspecionados()` e
  `Produto.getTotalProdutosFabricados()` só servem para relatórios e não
  influenciam nenhuma regra.
- **Sorteios**: o gerador de números aleatórios das máquinas não é salvo.
  Dois carregamentos do mesmo arquivo seguem caminhos diferentes quando a
  produção recomeça, e isso é intencional (a simulação é probabilística).

### Limitações conhecidas

- Há um único arquivo de save (`dados/planta.xml`); salvar de novo substitui o anterior.
- O arquivo só vale para a versão `1` do formato; outra versão é recusada com mensagem.
- Mudar o catálogo no código (por exemplo, renomear uma bolacha) invalida os saves antigos que a usam.
- A fábrica precisa ter ao menos uma máquina e uma engrenagem por máquina; um arquivo com a linha vazia é recusado.
- Se a gravação falhar no meio, o save anterior é preservado (gravamos num arquivo
  temporário e só depois o movemos para o lugar). Se o computador desligar no
  instante exato da troca, o arquivo temporário `planta*.tmp` pode sobrar na pasta `dados/`.

### Como a persistência trata erros

Todos os erros viram `PersistenciaException` (ou subclasse) com mensagem para o
operador, e **carregar nunca altera a planta em uso a menos que o arquivo inteiro
seja lido e validado**:

| Situação | Exceção |
|---|---|
| arquivo não existe | `ArquivoInexistenteException` |
| arquivo vazio, texto que não é XML, DOCTYPE, falha de leitura ou escrita | `PersistenciaException` |
| XML válido com valores fora da regra (saúde 150, estoque negativo, status ou tipo desconhecido, número ilegível, elemento faltando) | `DadosInvalidosException` |
| demanda ou bolacha de um tipo que a fábrica não faz | `DadosInvalidosException` (lançada por `GerenciadorProducao.restaurarEstado`) |

### Suíte de testes (JUnit 5)

A estratégia é testar **comportamento observável**, sem depender de detalhes
internos, em três frentes: regras de negócio, persistência e situações de
erro. Cada teste monta os seus próprios dados (`@BeforeEach` ou no próprio
método), nenhum depende da ordem de execução, nenhum lê o teclado e os testes
de persistência usam `@TempDir`, nunca o arquivo real `dados/planta.xml`.
`PlantaDeTeste` é só um apoio que cria uma fábrica nova com o painel sem teclado.

| Classe de teste | O que avalia |
|---|---|
| `DemandaTest` | **Regras de negócio**: transições de `StatusDemanda` (fila, produção, concluída, cancelada, reabertura), cálculo de massa e custo. **Erros**: quantidade zero ou negativa, nome vazio, concluir sem iniciar, alterar demanda em produção, custo negativo. |
| `MateriaPrimaTest` | **Regras**: consumir, repor, verificar disponibilidade e estoque mínimo. **Erros**: consumir mais do que há (`RecursoInsuficienteException` e estoque intacto), quantidades inválidas. |
| `ProdutoTest` | **Regras**: qualidade e massa por tipo, risco acumulado limitado a 100%, auditoria da bolacha, `criarCopia`. |
| `MaquinaTest` | **Composição (Tarefa 5)**: a máquina nasce com motor, painel e engrenagens; desgaste e reparo se propagam às partes; motor gasto encarece, painel e engrenagens gastos aumentam a falha; quebra por saúde ou por motor; **diagnóstico em cascata** indentado; lista de saúdes imutável. **Erros**: `restaurarEstado` com valores inválidos não altera nada; zero engrenagens. |
| `LinhaDeProducaoTest` | **Agregação**: ordem, `getMaquinas()` imutável, máquina nula ou repetida recusada, remoção, custo por bolacha, busca de máquina quebrada, relatório em cascata (linha > máquinas > peças). |
| `EstrategiaProducaoTest` | **Strategy**: Fila do Forno, Lote de Festa e Cesta Cheia escolhem a demanda certa; sem demanda elegível devolvem `null`. |
| `GerenciadorProducaoTest` | **Regras de negócio**: registrar e atualizar demandas, fabricar (gasta massa, ingrediente e budget e conclui), comprar, trocar estratégia, reparar. **Erros**: falta de massa e de budget (demanda cancelada, nada gasto), máquina quebrada (demanda continua na fila), bolacha fora do catálogo, compra acima do budget, restaurar estado inválido preserva a planta. |
| `PersistenciaPlantaTest` | **Persistência**: salvar cria o XML; carregar preserva budget, cenário, lotes, despensa, máquinas (tipo, ordem, saúde, motor, painel, engrenagens), demandas com `StatusDemanda` e armazém; ciclo completo salvar/carregar/continuar; carregar substitui o estado sem juntar; salvar de novo atualiza. **Erros**: arquivo inexistente, vazio, texto que não é XML, DOCTYPE, raiz e versão erradas, elemento faltando, número ilegível, saúde fora de 0 a 100, budget e estoque negativos, cenário, status e tipo de máquina desconhecidos, máquina sem engrenagem, sem massa, demanda inválida, falha de escrita, falha de carregamento preserva a planta em uso e save anterior sobrevive a uma falha. |

## Documentação

`./gradlew javadoc` gera a documentação em `build/docs/javadoc`. Todas as
classes, inclusive as de persistência, estão documentadas.
