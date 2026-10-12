import java.util.List;

/**
 * Lote de Festa: a maior encomenda passa na frente, porque é a que alimenta
 * mais gente. Não olha o budget, então pode escolher uma demanda que o caixa
 * não cobre (o gerenciador é quem recusa na hora de fabricar).
 *
 * @author Diego Reis
 */
public class EstrategiaLoteFesta implements EstrategiaProducao {

    /** Cria a estratégia. Ela não guarda estado, então não precisa de parâmetros. */
    public EstrategiaLoteFesta() {
    }

    /** {@inheritDoc} Aqui, a pendente de maior quantidade. */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        return escolherMaiorQuantidade(demandas, orcamentoDisponivel);
    }

    /** {@inheritDoc} */
    @Override
    public String getNomeEstrategia() {
        return "Lote de Festa";
    }
}
