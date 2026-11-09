package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;

/** Regras do estoque de {@link MateriaPrima}: consumir, repor e conferir. */
class MateriaPrimaTest {
    private MateriaPrima massa;

    @BeforeEach
    void preparar() {
        massa = new MateriaPrima("MP001", "massa da casa", 100, "g", 20, 0.30);
    }

    @Test
    @DisplayName("consumir tira do estoque")
    void consumir() {
        massa.consumir(30);
        assertEquals(70.0, massa.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("consumir mais do que há lança exceção e não retira nada")
    void consumirDemais() {
        RecursoInsuficienteException erro =
                assertThrows(RecursoInsuficienteException.class, () -> massa.consumir(100.5));
        assertTrue(erro.getMessage().contains("massa da casa"));
        assertEquals(100.0, massa.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("quantidade zero ou negativa é recusada ao consumir e ao repor")
    void quantidadesInvalidas() {
        assertThrows(IllegalArgumentException.class, () -> massa.consumir(0));
        assertThrows(IllegalArgumentException.class, () -> massa.adicionarEstoque(-5));
        assertEquals(100.0, massa.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("repor soma ao estoque")
    void repor() {
        massa.adicionarEstoque(50);
        assertEquals(150.0, massa.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("verificar disponibilidade não altera o estoque")
    void verificarDisponibilidade() {
        assertTrue(massa.verificarDisponibilidade(100));
        assertFalse(massa.verificarDisponibilidade(100.1));
        assertFalse(massa.verificarDisponibilidade(0));
        assertEquals(100.0, massa.getQuantidade(), 1e-9);
    }

    @Test
    @DisplayName("abaixo do mínimo a despensa avisa")
    void abaixoDoMinimo() {
        assertFalse(massa.estaAbaixoDoMinimo());
        massa.consumir(81);
        assertTrue(massa.estaAbaixoDoMinimo());
    }
}
