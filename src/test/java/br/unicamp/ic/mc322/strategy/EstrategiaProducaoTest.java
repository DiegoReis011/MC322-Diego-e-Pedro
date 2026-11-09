package br.unicamp.ic.mc322.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.model.Demanda;

/** Padrão Strategy: cada estratégia escolhe a próxima demanda do seu jeito. */
class EstrategiaProducaoTest {
    private Demanda pequena;
    private Demanda grande;
    private Demanda media;
    private List<Demanda> fila;

    @BeforeEach
    void preparar() {
        pequena = new Demanda("A", 5);
        grande = new Demanda("B", 50);
        media = new Demanda("C", 20);
        pequena.setCustoUnitarioEstimado(1.0);
        grande.setCustoUnitarioEstimado(1.0);
        media.setCustoUnitarioEstimado(1.0);
        fila = new ArrayList<>(List.of(pequena, grande, media));
    }

    @Test
    @DisplayName("Fila do Forno escolhe a primeira pendente")
    void filaDoForno() {
        EstrategiaProducao estrategia = new EstrategiaFilaDoForno();
        assertSame(pequena, estrategia.selecionarDemanda(fila, 1000));
        pequena.cancelar();
        assertSame(grande, estrategia.selecionarDemanda(fila, 1000));
        assertEquals("Fila do Forno", estrategia.getNomeEstrategia());
    }

    @Test
    @DisplayName("Lote de Festa escolhe a pendente de maior quantidade")
    void loteFesta() {
        EstrategiaProducao estrategia = new EstrategiaLoteFesta();
        assertSame(grande, estrategia.selecionarDemanda(fila, 1000));
    }

    @Test
    @DisplayName("Cesta Cheia escolhe a maior que cabe no budget")
    void cestaCheia() {
        EstrategiaProducao estrategia = new EstrategiaCestaCheia();
        assertSame(grande, estrategia.selecionarDemanda(fila, 1000));
        assertSame(media, estrategia.selecionarDemanda(fila, 25));
        assertSame(pequena, estrategia.selecionarDemanda(fila, 6));
    }

    @Test
    @DisplayName("sem demanda elegível a estratégia devolve null")
    void semElegivel() {
        EstrategiaProducao estrategia = new EstrategiaCestaCheia();
        assertNull(estrategia.selecionarDemanda(fila, 1));
        fila.forEach(Demanda::cancelar);
        assertNull(new EstrategiaFilaDoForno().selecionarDemanda(fila, 1000));
        assertNull(new EstrategiaLoteFesta().selecionarDemanda(new ArrayList<>(), 1000));
    }
}
