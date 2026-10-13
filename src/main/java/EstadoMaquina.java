/**
 * Estados possíveis de uma {@link Maquina}, calculados a partir da saúde dela
 * em {@link Maquina#getEstado()}.
 *
 * @author Pedro Magalhães
 */
public enum EstadoMaquina {
    /** Saúde a partir de 30: a máquina trabalha normalmente. */
    OPERANDO("Rodando redondo"),
    /** Saúde abaixo de 30: ainda trabalha, mas falha mais e pede reparo. */
    PRECISA_MANUTENCAO("Pedindo manutenção"),
    /** Saúde abaixo de 10: não trabalha até ser reparada e para a linha inteira. */
    QUEBRADA("Quebrada, parou a linha");

    private final String descricao;

    EstadoMaquina(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Texto do estado para a oficina e para os relatórios.
     *
     * @return descrição legível do estado
     */
    public String getDescricao() {
        return descricao;
    }
}
