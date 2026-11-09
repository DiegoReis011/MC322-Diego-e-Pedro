package br.unicamp.ic.mc322.model.maquina;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Agrupador da fábrica: ordem, proteção da lista, custo e auditoria em cascata. */
class LinhaDeProducaoTest {
    private LinhaDeProducao linha;
    private Estampadeira estampadeira;
    private Forno forno;

    @BeforeEach
    void preparar() {
        linha = new LinhaDeProducao("de Teste");
        estampadeira = new Estampadeira();
        forno = new Forno();
        linha.adicionarMaquina(estampadeira);
        linha.adicionarMaquina(forno);
    }

    @Test
    @DisplayName("as máquinas ficam na ordem em que foram adicionadas")
    void ordem() {
        assertEquals(2, linha.getMaquinas().size());
        assertSame(estampadeira, linha.getMaquinas().get(0));
        assertSame(forno, linha.getMaquinas().get(1));
    }

    @Test
    @DisplayName("getMaquinas devolve lista imutável (proteção da coleção)")
    void listaImutavel() {
        List<Maquina> vista = linha.getMaquinas();
        assertThrows(UnsupportedOperationException.class, () -> vista.add(new Forno()));
        assertThrows(UnsupportedOperationException.class, () -> vista.remove(0));
        assertEquals(2, linha.getMaquinas().size());
    }

    @Test
    @DisplayName("máquina nula ou repetida é recusada")
    void recusas() {
        assertThrows(IllegalArgumentException.class, () -> linha.adicionarMaquina(null));
        assertThrows(IllegalArgumentException.class, () -> linha.adicionarMaquina(forno));
        assertEquals(2, linha.getMaquinas().size());
    }

    @Test
    @DisplayName("remover tira a máquina da linha, que continua existindo (agregação)")
    void remover() {
        assertTrue(linha.removerMaquina(forno));
        assertFalse(linha.removerMaquina(forno));
        assertEquals(1, linha.getMaquinas().size());
        assertEquals(100.0, forno.getSaudeGeral(), 1e-9);
    }

    @Test
    @DisplayName("o custo por bolacha soma o das máquinas")
    void custo() {
        assertEquals(3.0, linha.calcularCustoPorProduto(), 1e-9);
    }

    @Test
    @DisplayName("acha a primeira máquina quebrada, ou nenhuma")
    void quebrada() {
        assertNull(linha.buscarMaquinaQuebrada());
        forno.restaurarEstado(2, 0, 0, 100, 100, Arrays.asList(100.0, 100.0, 100.0));
        assertSame(forno, linha.buscarMaquinaQuebrada());
    }

    @Test
    @DisplayName("o relatório da linha desce em cascata: linha, máquinas e peças")
    void relatorioEmCascata() {
        String relatorio = linha.gerarRelatorioDiagnostico();
        assertTrue(relatorio.startsWith("Linha de Teste"));
        assertTrue(relatorio.contains("\n  Estampadeira"));
        assertTrue(relatorio.contains("\n  Forno"));
        assertTrue(relatorio.contains("\n    - Motor: OK"));
        assertTrue(relatorio.contains("\n    - Engrenagem 2: OK"));
    }

    @Test
    @DisplayName("a linha pede manutenção quando uma máquina pede")
    void manutencao() {
        assertFalse(linha.precisaManutencao());
        estampadeira.restaurarEstado(100, 0, 0, 100, 10, Arrays.asList(100.0, 100.0, 100.0));
        assertTrue(linha.precisaManutencao());
    }
}
