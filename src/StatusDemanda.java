/**
 * Estados possíveis de uma {@link Demanda}. Usar um enum no lugar de texto
 * solto impede erros de digitação e permite comparar com {@code ==}.
 *
 * <p>Quem controla as transições entre os estados é a própria
 * {@link Demanda}.</p>
 *
 * @author Diego Reis
 */
public enum StatusDemanda {
    /** Demanda cadastrada, na fila e ainda não iniciada. */
    PENDENTE("Na fila, esperando a vez"),
    /** Demanda escolhida e em fabricação. */
    EM_PRODUCAO("Massa no forno"),
    /** Todas as bolachas da demanda foram fabricadas. */
    CONCLUIDA("Fornada pronta"),
    /** Demanda cancelada, por exemplo por falta de budget ou de insumo. */
    CANCELADA("Cancelada");

    private final String descricao;

    StatusDemanda(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Texto do estado para o menu e para os relatórios.
     *
     * @return descrição legível do estado
     */
    public String getDescricao() {
        return descricao;
    }
}
