import java.util.List;

public class EstrategiaCestaCheia implements EstrategiaProducao {

    // alem de pendente, a demanda precisa caber no orcamento.
    @Override
    public boolean ehElegivel(Demanda demanda, double orcamentoDisponivel) {
        return demanda.isPendente() && demanda.isViavel(orcamentoDisponivel);
    }

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        return escolherMaiorQuantidade(demandas, orcamentoDisponivel);
    }

    @Override
    public String getNomeEstrategia() {
        return "Cesta Cheia";
    }
}
