public enum EstadoMaquina {
    OPERANDO("Rodando redondo"),
    PRECISA_MANUTENCAO("Pedindo manutenção"),
    QUEBRADA("Quebrada, parou a linha");

    private final String descricao;

    EstadoMaquina(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
