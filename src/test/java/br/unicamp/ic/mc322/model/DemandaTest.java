package br.unicamp.ic.mc322.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;

/** Regras de negócio da {@link Demanda}: ciclo de vida pelo enum e validações. */
class DemandaTest {
    private Demanda demanda;

    @BeforeEach
    void preparar() {
        demanda = new Demanda("Bolacha cracker de coco", 10);
    }

    @Test
    @DisplayName("demanda nova nasce pendente, com a quantidade pedida")
    void nasceNaFila() {
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(10, demanda.getQuantidadeProdutos());
        assertTrue(demanda.isPendente());
    }

    @Test
    @DisplayName("quantidade zero ou negativa é recusada")
    void quantidadeInvalida() {
        assertThrows(DemandaInvalidaException.class, () -> new Demanda("x", 0));
        assertThrows(DemandaInvalidaException.class, () -> new Demanda("x", -3));
    }

    @Test
    @DisplayName("demanda sem nome de bolacha é recusada")
    void semNome() {
        assertThrows(DemandaInvalidaException.class, () -> new Demanda("  ", 5));
        assertThrows(DemandaInvalidaException.class, () -> new Demanda(null, 5));
    }

    @Test
    @DisplayName("fluxo normal: pendente, em produção, concluída")
    void fluxoCompleto() {
        demanda.iniciarProducao();
        assertEquals(StatusDemanda.EM_PRODUCAO, demanda.getStatus());
        demanda.atender();
        assertEquals(StatusDemanda.CONCLUIDA, demanda.getStatus());
        assertFalse(demanda.isPendente());
    }

    @Test
    @DisplayName("concluir demanda que não começou é recusado e o estado fica igual")
    void concluirSemIniciar() {
        DemandaInvalidaException erro = assertThrows(DemandaInvalidaException.class, demanda::atender);
        assertTrue(erro.getMessage().contains("concluir"));
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
    }

    @Test
    @DisplayName("demanda cancelada não pode ser concluída nem cancelada de novo")
    void transicoesDepoisDeCancelar() {
        demanda.cancelar();
        assertEquals(StatusDemanda.CANCELADA, demanda.getStatus());
        assertThrows(DemandaInvalidaException.class, demanda::atender);
        assertThrows(DemandaInvalidaException.class, demanda::cancelar);
        assertEquals(StatusDemanda.CANCELADA, demanda.getStatus());
    }

    @Test
    @DisplayName("novo pedido reabre demanda concluída")
    void reabrir() {
        demanda.iniciarProducao();
        demanda.atender();
        demanda.atualizarQuantidade(25);
        assertEquals(StatusDemanda.PENDENTE, demanda.getStatus());
        assertEquals(25, demanda.getQuantidadeProdutos());
    }

    @Test
    @DisplayName("demanda em produção não aceita mudança de quantidade")
    void atualizarEmProducao() {
        demanda.iniciarProducao();
        assertThrows(DemandaInvalidaException.class, () -> demanda.atualizarQuantidade(3));
        assertEquals(10, demanda.getQuantidadeProdutos());
        assertEquals(StatusDemanda.EM_PRODUCAO, demanda.getStatus());
    }

    @Test
    @DisplayName("massa necessária e custo estimado seguem a quantidade")
    void calculos() {
        demanda.setCustoUnitarioEstimado(3.5);
        assertEquals(60.0, demanda.calcularMateriaPrimaNecessaria(6.0), 1e-9);
        assertEquals(35.0, demanda.calcularCustoEstimado(), 1e-9);
        assertTrue(demanda.isViavel(35.0));
        assertFalse(demanda.isViavel(34.99));
    }

    @Test
    @DisplayName("custo unitário negativo é erro de programação")
    void custoNegativo() {
        assertThrows(IllegalArgumentException.class, () -> demanda.setCustoUnitarioEstimado(-1));
    }
}
