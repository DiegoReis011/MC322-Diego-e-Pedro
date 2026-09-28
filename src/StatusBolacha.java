public enum StatusBolacha {
    AGUARDANDO("Aguardando processamento"),
    PROCESSADA("Processada"),
    CORTADA("Cortada"),
    ASSADA("Assada"),
    APROVADA("Aprovada"),
    REPROVADA("Reprovada");

    private final String descricao;

    StatusBolacha(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
