import java.util.List;

/**
 * Cesta Cheia: escolhe a maior encomenda que o budget consegue pagar, para
 * encher a cesta de bolachas sem estourar o caixa. Se nenhuma couber, não
 * escolhe nenhuma.
 *
 * @author Diego Reis
 */
public class EstrategiaCestaCheia implements EstrategiaProducao {

    /** Cria a estratégia. Ela não guarda estado, então não precisa de parâmetros. */
    public EstrategiaCestaCheia() {
    }

    /**
     * {@inheritDoc}
     *
     * <p>Além de pendente, a demanda precisa caber no orçamento.</p>
     */
    @Override
    public boolean ehElegivel(Demanda demanda, double orcamentoDisponivel) {
        return demanda.isPendente() && demanda.isViavel(orcamentoDisponivel);
    }

    /** {@inheritDoc} Aqui, a maior pendente que cabe no budget. */
    @Override
    public Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel) {
        return escolherMaiorQuantidade(demandas, orcamentoDisponivel);
    }

    /** {@inheritDoc} */
    @Override
    public String getNomeEstrategia() {
        return "Cesta Cheia";
    }
}
