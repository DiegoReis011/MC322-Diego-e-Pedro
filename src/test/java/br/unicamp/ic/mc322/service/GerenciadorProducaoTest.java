package br.unicamp.ic.mc322.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.PlantaDeTeste;
import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.OrcamentoInsuficienteException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.strategy.EstrategiaCestaCheia;

/** Regras de negócio do gerenciador: demandas, produção, compras, manutenção e erros. */
class GerenciadorProducaoTest {
    private GerenciadorProducao fabrica;
    private String amanteigada;
    private String cracker;

    @BeforeEach
    void preparar() {
        fabrica = PlantaDeTeste.novoGerenciador();
        amanteigada = PlantaDeTeste.nomeDaBolacha(fabrica, 0);
        cracker = PlantaDeTeste.nomeDaBolacha(fabrica, 2);
    }

    private Demanda demandaDe(String nome) {
        for (Demanda demanda : fabrica.getDemandas()) {
            if (demanda.getTipoProduto().equals(nome)) {
                return demanda;
            }
        }
        return null;
    }

    private double massaAtual() {
        return fabrica.getInsumos().get(0).getQuantidade();
    }

    @Test
    @DisplayName("a fábrica nasce com a linha de três máquinas, o catálogo e o budget do cenário")
    void estadoInicial() {
        assertEquals(3, fabrica.getMaquinas().size());
        assertEquals(3, fabrica.getCatalogo().size());
        assertEquals(10000.0, fabrica.getBudget(), 1e-9);
        assertEquals(4, fabrica.getInsumos().size());
        assertTrue(fabrica.getDemandas().isEmpty());
    }

    @Test
    @DisplayName("registrar demanda cria uma demanda pendente com o custo unitário")
    void registrar() {
        fabrica.registrarDemanda(amanteigada, 5);
        Demanda demanda = demandaDe(amanteigada);
        assertNotNull(demanda);
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(3.5, demanda.getCustoUnitarioEstimado(), 1e-9);
    }

    @Test
    @DisplayName("registrar de novo a mesma bolacha troca a quantidade, sem duplicar")
    void registrarDeNovo() {
        fabrica.registrarDemanda(amanteigada, 5);
        fabrica.registrarDemanda(amanteigada, 9);
        assertEquals(1, fabrica.getDemandas().size());
        assertEquals(9, demandaDe(amanteigada).getQuantidadeProdutos());
    }

    @Test
    @DisplayName("demanda de bolacha fora do catálogo ou com quantidade inválida é recusada")
    void registrarInvalido() {
        assertThrows(DemandaInvalidaException.class, () -> fabrica.registrarDemanda("Pão de queijo", 3));
        assertThrows(DemandaInvalidaException.class, () -> fabrica.registrarDemanda(amanteigada, 0));
        assertThrows(DemandaInvalidaException.class, () -> fabrica.atualizarDemanda(amanteigada, 4));
        assertTrue(fabrica.getDemandas().isEmpty());
    }

    @Test
    @DisplayName("fabricar gasta massa, ingrediente e budget e conclui a demanda")
    void fabricar() {
        fabrica.registrarDemanda(amanteigada, 5);
        double massaAntes = massaAtual();
        double ingredienteAntes = fabrica.getInsumos().get(1).getQuantidade();

        fabrica.executarProximaProducao();

        assertEquals(StatusDemanda.CONCLUIDA, demandaDe(amanteigada).getStatus());
        double massaGasta = 5 * fabrica.getCatalogo().get(0).getQuantidadeMateriaPrimaPorUnidade();
        assertEquals(massaAntes - massaGasta, massaAtual(), 1e-9);
        assertEquals(ingredienteAntes - massaGasta * 0.20, fabrica.getInsumos().get(1).getQuantidade(), 1e-9);
        assertEquals(10000.0 - 5 * 3.5, fabrica.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("sem massa suficiente a demanda é cancelada e nada é gasto")
    void faltaMassa() {
        fabrica.registrarDemanda(amanteigada, 1000);
        double massaAntes = massaAtual();
        RecursoInsuficienteException erro =
                assertThrows(RecursoInsuficienteException.class, () -> fabrica.executarProximaProducao());
        assertNotNull(erro.getMessage());
        assertEquals(StatusDemanda.CANCELADA, demandaDe(amanteigada).getStatus());
        assertEquals(massaAntes, massaAtual(), 1e-9);
        assertEquals(10000.0, fabrica.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("budget insuficiente cancela a demanda e não debita nada")
    void faltaBudget() {
        GerenciadorProducao apocaliptica = PlantaDeTeste.novoGerenciador(Cenario.APOCALIPTICO);
        String nome = PlantaDeTeste.nomeDaBolacha(apocaliptica, 2);
        apocaliptica.registrarDemanda(nome, 300);
        assertThrows(OrcamentoInsuficienteException.class, () -> apocaliptica.executarProximaProducao());
        assertEquals(400.0, apocaliptica.getBudget(), 1e-9);
        assertEquals(5000.0, apocaliptica.getInsumos().get(0).getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("máquina quebrada trava a produção, mas a demanda continua na fila")
    void maquinaQuebrada() {
        fabrica.registrarDemanda(cracker, 5);
        fabrica.getMaquinas().get(1).restaurarEstado(3, 0, 0, 100, 100, Arrays.asList(100.0, 100.0, 100.0));
        double massaAntes = massaAtual();
        assertThrows(MaquinaIndisponivelException.class, () -> fabrica.executarProximaProducao());
        assertEquals(StatusDemanda.PENDENTE, demandaDe(cracker).getStatus());
        assertEquals(massaAntes, massaAtual(), 1e-9);
        assertEquals(10000.0, fabrica.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("sem demanda elegível a estratégia não acha nada e a produção é recusada")
    void semDemanda() {
        assertThrows(DemandaInvalidaException.class, () -> fabrica.executarProximaProducao());
    }

    @Test
    @DisplayName("fabricar demanda que já foi concluída é recusado")
    void fabricarConcluida() {
        fabrica.registrarDemanda(cracker, 2);
        fabrica.fabricarDemanda(fabrica.getCatalogo().get(2));
        assertThrows(DemandaInvalidaException.class, () -> fabrica.fabricarDemanda(fabrica.getCatalogo().get(2)));
    }

    @Test
    @DisplayName("comprar matéria-prima soma ao estoque e debita o preço")
    void comprar() {
        MateriaPrima massa = fabrica.getInsumos().get(0);
        fabrica.comprarMateriaPrima(massa, 100);
        assertEquals(5100.0, massa.getQuantidade(), 1e-9);
        assertEquals(10000.0 - 30.0, fabrica.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("compra acima do budget é recusada sem gastar nem repor nada")
    void comprarDemais() {
        MateriaPrima massa = fabrica.getInsumos().get(0);
        assertThrows(OrcamentoInsuficienteException.class, () -> fabrica.comprarMateriaPrima(massa, 1_000_000));
        assertThrows(IllegalArgumentException.class, () -> fabrica.comprarMateriaPrima(massa, 0));
        assertEquals(5000.0, massa.getQuantidade(), 1e-9);
        assertEquals(10000.0, fabrica.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("trocar a estratégia em tempo de execução; estratégia nula é ignorada")
    void estrategia() {
        assertEquals("Fila do Forno", fabrica.getEstrategiaAtual().getNomeEstrategia());
        fabrica.setEstrategia(new EstrategiaCestaCheia());
        assertEquals("Cesta Cheia", fabrica.getEstrategiaAtual().getNomeEstrategia());
        fabrica.setEstrategia(null);
        assertEquals("Cesta Cheia", fabrica.getEstrategiaAtual().getNomeEstrategia());
    }

    @Test
    @DisplayName("reparar cobra pelo desgaste, devolve 100% e máquina nova não paga")
    void reparar() {
        Maquina forno = fabrica.getMaquinas().get(1);
        forno.restaurarEstado(40, 0, 0, 40, 40, Arrays.asList(40.0, 40.0, 40.0));
        double custo = fabrica.calcularCustoReparo(forno);
        assertEquals(60.0 * 3.0, custo, 1e-9);
        fabrica.repararMaquina(forno);
        assertEquals(100.0, forno.getSaudeGeral(), 1e-9);
        assertEquals(10000.0 - custo, fabrica.getBudget(), 1e-9);
        fabrica.repararMaquina(forno);
        assertEquals(10000.0 - custo, fabrica.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("sem budget o reparo é recusado e a máquina continua como estava")
    void repararSemBudget() {
        GerenciadorProducao pobre = PlantaDeTeste.novoGerenciador(Cenario.APOCALIPTICO);
        // Gasta quase todo o caixa (R$ 360 dos R$ 400) para o reparo nao caber.
        pobre.comprarMateriaPrima(pobre.getInsumos().get(0), 1200);
        Maquina forno = pobre.getMaquinas().get(1);
        forno.restaurarEstado(0, 0, 0, 0, 0, Arrays.asList(0.0, 0.0, 0.0));
        assertThrows(OrcamentoInsuficienteException.class, () -> pobre.repararMaquina(forno));
        assertEquals(0.0, forno.getSaude(), 1e-9);
        assertEquals(40.0, pobre.getBudget(), 1e-9);
    }

    @Test
    @DisplayName("a lista de máquinas do gerenciador é imutável (proteção da coleção)")
    void maquinasProtegidas() {
        List<Maquina> maquinas = fabrica.getMaquinas();
        assertThrows(UnsupportedOperationException.class, maquinas::clear);
        assertEquals(3, fabrica.getMaquinas().size());
    }

    @Test
    @DisplayName("restaurar estado com demanda de bolacha desconhecida falha e preserva a planta")
    void restaurarInvalidoPreserva() {
        fabrica.registrarDemanda(cracker, 7);
        EstadoPlanta bom = fabrica.exportarEstado();
        Demanda intrusa = new Demanda("Pão de queijo", 3);
        EstadoPlanta ruim = new EstadoPlanta(bom.getCenario(), 1.0, 0, bom.getMassa(),
                bom.getIngredientes(), bom.getMaquinas(), List.of(intrusa), bom.getArmazem());

        assertThrows(br.unicamp.ic.mc322.exception.DadosInvalidosException.class,
                () -> fabrica.restaurarEstado(ruim));

        assertEquals(10000.0, fabrica.getBudget(), 1e-9);
        assertEquals(1, fabrica.getDemandas().size());
        assertEquals(7, demandaDe(cracker).getQuantidadeProdutos());
    }

    @Test
    @DisplayName("restaurar estado sem máquinas ou com demanda duplicada é recusado")
    void restaurarOutrasRecusas() {
        EstadoPlanta base = fabrica.exportarEstado();
        EstadoPlanta semMaquinas = new EstadoPlanta(base.getCenario(), 10.0, 0, base.getMassa(),
                base.getIngredientes(), List.of(), List.of(), List.of());
        assertThrows(br.unicamp.ic.mc322.exception.DadosInvalidosException.class,
                () -> fabrica.restaurarEstado(semMaquinas));

        EstadoPlanta duplicada = new EstadoPlanta(base.getCenario(), 10.0, 0, base.getMassa(),
                base.getIngredientes(), base.getMaquinas(),
                List.of(new Demanda(cracker, 1), new Demanda(cracker, 2)), List.of());
        assertThrows(br.unicamp.ic.mc322.exception.DadosInvalidosException.class,
                () -> fabrica.restaurarEstado(duplicada));
        assertFalse(fabrica.getBudget() == 10.0);
    }
}
