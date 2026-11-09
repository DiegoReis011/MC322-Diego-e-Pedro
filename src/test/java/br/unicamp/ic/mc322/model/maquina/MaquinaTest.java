package br.unicamp.ic.mc322.model.maquina;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.produto.BolachaCracker;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.model.produto.StatusBolacha;

/** Composição da máquina (Tarefa 5): partes criadas por ela, desgaste, reparo e diagnóstico. */
class MaquinaTest {
    private Forno forno;

    @BeforeEach
    void preparar() {
        forno = new Forno();
        forno.aplicarCenario(Cenario.IDEAL);
    }

    @Test
    @DisplayName("a máquina nasce com motor, painel e três engrenagens, tudo com saúde cheia")
    void nasceComPartes() {
        assertEquals(3, forno.getQuantidadeEngrenagens());
        assertEquals(100.0, forno.getSaudeGeral(), 1e-9);
        assertEquals(100.0, forno.getSaudeMotor(), 1e-9);
        assertEquals(100.0, forno.getSaudePainel(), 1e-9);
        assertEquals(3, forno.getSaudesEngrenagens().size());
    }

    @Test
    @DisplayName("uma máquina sem engrenagem é recusada")
    void semEngrenagem() {
        assertThrows(IllegalArgumentException.class, () -> new Forno(500, 0.3, 2.0, 0));
    }

    @Test
    @DisplayName("o diagnóstico em cascata lista a máquina e cada parte, indentadas")
    void diagnosticoEmCascata() {
        String[] linhas = forno.gerarRelatorioDiagnostico().split("\n");
        assertEquals(1 + 2 + 3, linhas.length);
        assertTrue(linhas[0].startsWith("Forno"));
        assertTrue(linhas[1].startsWith("  - Motor: OK"));
        assertTrue(linhas[2].startsWith("  - Painel de Controle: Calibrado"));
        assertTrue(linhas[5].startsWith("  - Engrenagem 3"));
    }

    @Test
    @DisplayName("cada fornada desgasta a máquina e as suas partes")
    void desgaste() {
        forno.aplicarCenario(Cenario.APOCALIPTICO);
        double antes = forno.getSaudeGeral();
        for (int i = 0; i < 20; i++) {
            forno.registrarCiclo();
        }
        assertTrue(forno.getSaudeGeral() < antes);
        assertTrue(forno.getSaudeMotor() < 100.0);
        assertEquals(20, forno.getCiclos());
    }

    @Test
    @DisplayName("reparar devolve a máquina e todas as partes a 100%")
    void reparar() {
        forno.restaurarEstado(40, 5, 1, 30, 20, Arrays.asList(10.0, 50.0, 60.0));
        forno.reparar();
        assertEquals(100.0, forno.getSaudeGeral(), 1e-9);
        assertEquals(100.0, forno.getSaudeMotor(), 1e-9);
        for (double saude : forno.getSaudesEngrenagens()) {
            assertEquals(100.0, saude, 1e-9);
        }
    }

    @Test
    @DisplayName("saúde abaixo de 10 quebra a máquina, mesmo ligada, e ela não opera")
    void quebraPorSaude() {
        forno.restaurarEstado(5, 0, 0, 100, 100, Arrays.asList(100.0, 100.0, 100.0));
        forno.ligar();
        assertTrue(forno.estaQuebrada());
        assertEquals(EstadoMaquina.QUEBRADA, forno.getEstado());
        assertFalse(forno.podeOperar());
        Produto bolacha = new BolachaCracker("C", "coco");
        assertFalse(forno.processar(bolacha));
        assertEquals(StatusBolacha.AGUARDANDO, bolacha.getStatus());
    }

    @Test
    @DisplayName("motor quebrado para a máquina inteira, mesmo com a saúde dela alta")
    void quebraPeloMotor() {
        forno.restaurarEstado(100, 0, 0, 5, 100, Arrays.asList(100.0, 100.0, 100.0));
        assertTrue(forno.estaQuebrada());
    }

    @Test
    @DisplayName("peça abaixo de 30 pede manutenção sem quebrar a máquina")
    void pedeManutencao() {
        forno.restaurarEstado(100, 0, 0, 100, 100, Arrays.asList(100.0, 25.0, 100.0));
        assertTrue(forno.precisaManutencao());
        assertFalse(forno.estaQuebrada());
        assertEquals(EstadoMaquina.PRECISA_MANUTENCAO, forno.getEstado());
    }

    @Test
    @DisplayName("motor gasto encarece a operação em até 50%")
    void custoComMotorGasto() {
        assertEquals(2.0, forno.getCustoOperacional(), 1e-9);
        forno.restaurarEstado(100, 0, 0, 0, 100, Arrays.asList(100.0, 100.0, 100.0));
        assertEquals(3.0, forno.getCustoOperacional(), 1e-9);
        assertEquals(2.0, forno.getCustoOperacionalBase(), 1e-9);
    }

    @Test
    @DisplayName("painel e engrenagens gastos aumentam a chance de falha")
    void falhaComPartesGastas() {
        double nova = forno.calcularProbabilidadeFalha();
        forno.restaurarEstado(100, 0, 0, 100, 0, Arrays.asList(0.0, 0.0, 0.0));
        assertEquals(nova + 0.15 + 3 * 0.05, forno.calcularProbabilidadeFalha(), 1e-9);
    }

    @Test
    @DisplayName("a lista de saúdes das engrenagens não deixa mexer nas peças")
    void listaImutavel() {
        List<Double> saudes = forno.getSaudesEngrenagens();
        assertThrows(UnsupportedOperationException.class, () -> saudes.add(10.0));
    }

    @Test
    @DisplayName("restaurar estado inválido lança exceção e não muda nada")
    void restaurarInvalido() {
        forno.restaurarEstado(80, 3, 1, 70, 60, Arrays.asList(50.0, 50.0, 50.0));
        assertThrows(IllegalArgumentException.class,
                () -> forno.restaurarEstado(150, 0, 0, 100, 100, Arrays.asList(100.0, 100.0, 100.0)));
        assertThrows(IllegalArgumentException.class,
                () -> forno.restaurarEstado(90, 0, 0, 100, 100, Arrays.asList(100.0, 100.0)));
        assertThrows(IllegalArgumentException.class,
                () -> forno.restaurarEstado(90, -1, 0, 100, 100, Arrays.asList(100.0, 100.0, 100.0)));
        assertEquals(80.0, forno.getSaude(), 1e-9);
        assertEquals(3, forno.getCiclos());
        assertEquals(70.0, forno.getSaudeMotor(), 1e-9);
    }

    @Test
    @DisplayName("máquina desligada não opera e deixa a bolacha como estava")
    void desligada() {
        Produto bolacha = new BolachaCracker("C", "coco");
        assertFalse(forno.processar(bolacha));
        forno.ligar();
        assertTrue(forno.processar(bolacha));
        assertEquals(StatusBolacha.ASSADA, bolacha.getStatus());
    }

    @Test
    @DisplayName("a estação de inspeção sempre dá um veredito")
    void inspecao() {
        EstacaoInspecao estacao = new EstacaoInspecao();
        estacao.ligar();
        Produto bolacha = new BolachaCracker("C", "coco");
        assertTrue(estacao.processar(bolacha));
        assertTrue(bolacha.getStatus() == StatusBolacha.APROVADA
                || bolacha.getStatus() == StatusBolacha.REPROVADA);
        assertEquals(1, estacao.getTotalInspecionados());
    }
}
