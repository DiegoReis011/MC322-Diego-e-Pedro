package br.unicamp.ic.mc322.strategy;

import java.util.List;

import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.maquina.Forno;

/**
 * Fila do Forno: quem pediu primeiro assa primeiro (FIFO). Escolhe a primeira
 * demanda pendente na ordem de cadastro.
 *
 * @author Diego Reis
 */
public class EstrategiaFilaDoForno implements EstrategiaProducao {

    /** Cria a estratégia. Ela não guarda estado, então não precisa de parâmetros. */
    public EstrategiaFilaDoForno() {
    }

    /** {@inheritDoc} Aqui, a primeira pendente da lista. */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        for (Demanda demanda : demandas) {
            if (ehElegivel(demanda, orcamentoDisponivel)) {
                return demanda;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getNomeEstrategia() {
        return "Fila do Forno";
    }
}
