import java.util.List;

public class EstrategiaFilaDoForno implements EstrategiaProducao {

    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        for (Demanda demanda : demandas) {
            if (ehElegivel(demanda, orcamentoDisponivel)) {
                return demanda;
            }
        }
        return null;
    }

    @Override
    public String getNomeEstrategia() {
        return "Fila do Forno";
    }
}
