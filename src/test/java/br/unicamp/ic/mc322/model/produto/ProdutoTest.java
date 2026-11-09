package br.unicamp.ic.mc322.model.produto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Comportamento das bolachas: valores por tipo, risco acumulado e cópias. */
class ProdutoTest {

    @Test
    @DisplayName("cada tipo tem a sua qualidade e a sua massa por unidade")
    void valoresPorTipo() {
        Produto amanteigada = new BolachaAmanteigada("A", "chocolate");
        Produto cracker = new BolachaCracker("C", "coco");
        assertEquals(0.9, amanteigada.getQualidade(), 1e-9);
        assertEquals(0.5, cracker.getQualidade(), 1e-9);
        assertTrue(amanteigada.getQuantidadeMateriaPrimaPorUnidade()
                > cracker.getQuantidadeMateriaPrimaPorUnidade());
        assertEquals("chocolate", amanteigada.getSabor());
    }

    @Test
    @DisplayName("bolacha nova aguarda processamento e não tem risco")
    void estadoInicial() {
        Produto bolacha = new BolachaTradicional("T", "aveia");
        assertEquals(StatusBolacha.AGUARDANDO, bolacha.getStatus());
        assertEquals(0.0, bolacha.getProbabilidadeFalhaAcumulada(), 1e-9);
        assertFalse(bolacha.precisaManutencao());
    }

    @Test
    @DisplayName("o risco acumulado soma, nunca passa de 100% e acende a auditoria")
    void riscoAcumulado() {
        Produto bolacha = new BolachaCracker("C", "coco");
        bolacha.aumentarProbabilidadeFalha(0.10);
        bolacha.aumentarProbabilidadeFalha(0.10);
        assertEquals(0.20, bolacha.getProbabilidadeFalhaAcumulada(), 1e-9);
        assertTrue(bolacha.precisaManutencao());
        bolacha.aumentarProbabilidadeFalha(5.0);
        assertEquals(1.0, bolacha.getProbabilidadeFalhaAcumulada(), 1e-9);
    }

    @Test
    @DisplayName("criarCopia devolve outra bolacha do mesmo tipo e sabor, sem risco")
    void copia() {
        Produto molde = new BolachaAmanteigada("B001", "chocolate");
        molde.aumentarProbabilidadeFalha(0.5);
        Produto copia = molde.criarCopia("B001-L1-1");
        assertNotSame(molde, copia);
        assertEquals(molde.getClass(), copia.getClass());
        assertEquals("chocolate", copia.getSabor());
        assertEquals("B001-L1-1", copia.getId());
        assertEquals(0.0, copia.getProbabilidadeFalhaAcumulada(), 1e-9);
    }
}
