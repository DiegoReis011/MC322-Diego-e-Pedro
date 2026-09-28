import java.util.List;

public interface EstrategiaProducao {

    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    String getNomeEstrategia();

    default boolean ehElegivel(Demanda demanda, double orcamentoDisponivel) {
        return demanda.isPendente();
    }

    // percorre a lista e fica com a elegivel de maior quantidade.
    // se empatar, vence a que chegou primeiro.
    default Demanda escolherMaiorQuantidade(List<Demanda> demandas, double orcamentoDisponivel) {
        Demanda escolhida = null;
        for (Demanda demanda : demandas) {
            if (ehElegivel(demanda, orcamentoDisponivel)
                    && (escolhida == null
                        || demanda.getQuantidadeProdutos() > escolhida.getQuantidadeProdutos())) {
                escolhida = demanda;
            }
        }
        return escolhida;
    }
}
