import java.util.List;

/**
 * Padrão Strategy da fábrica: decide qual demanda vai para o forno em seguida.
 * O {@link GerenciadorProducao} só conhece esta interface, então dá para
 * trocar de estratégia a qualquer momento sem mexer nele.
 *
 * <p>As estratégias são puras: só leem a lista e escolhem. Quem muda status,
 * budget e estoque é o gerenciador.</p>
 *
 * @author Diego Reis
 */
public interface EstrategiaProducao {

    /**
     * Escolhe a próxima demanda a fabricar.
     *
     * @param demandas            demandas cadastradas, na ordem de chegada
     * @param orcamentoDisponivel budget atual, em reais
     * @return a demanda escolhida, ou {@code null} se nenhuma for elegível
     */
    Demanda selecionarDemanda(List<Demanda> demandas, double orcamentoDisponivel);

    /**
     * Nome da estratégia para o menu.
     *
     * @return nome amigável da estratégia
     */
    String getNomeEstrategia();

    /**
     * Filtro comum a todas as estratégias: por padrão só disputa a vez quem
     * está {@code PENDENTE}. Uma estratégia pode sobrescrever para exigir mais.
     *
     * @param demanda             demanda a avaliar
     * @param orcamentoDisponivel budget atual, em reais
     * @return {@code true} se a demanda pode ser escolhida
     */
    default boolean ehElegivel(Demanda demanda, double orcamentoDisponivel) {
        return demanda.isPendente();
    }

    /**
     * Percorre a lista e fica com a demanda elegível de maior quantidade. Se
     * houver empate, vence a que chegou primeiro.
     *
     * @param demandas            demandas cadastradas, na ordem de chegada
     * @param orcamentoDisponivel budget atual, em reais
     * @return a elegível de maior quantidade, ou {@code null} se não houver
     */
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
