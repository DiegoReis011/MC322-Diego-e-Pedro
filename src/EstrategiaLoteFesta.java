import java.util.List;

public class EstrategiaLoteFesta implements EstrategiaProducao {

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        return escolherMaiorQuantidade(demandas, orcamentoDisponivel);
    }

    @Override
    public String getNomeEstrategia() {
        return "Lote de Festa";
    }
}
