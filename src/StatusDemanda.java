public enum StatusDemanda {
    PENDENTE("Na fila, esperando a vez"),
    EM_PRODUCAO("Massa no forno"),
    CONCLUIDA("Fornada pronta"),
    CANCELADA("Cancelada");

    private final String descricao;

    StatusDemanda(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
