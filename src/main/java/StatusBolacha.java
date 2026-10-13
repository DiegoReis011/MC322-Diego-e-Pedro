/**
 * Etapas por que uma bolacha passa na linha, da massa crua até o veredito da
 * inspeção. Cada máquina avança o status quando processa a bolacha.
 *
 * @author Pedro Magalhães
 */
public enum StatusBolacha {
    /** Bolacha criada, ainda não entrou na linha. */
    AGUARDANDO("Aguardando processamento"),
    /** Massa preparada, pronta para o corte. */
    PROCESSADA("Processada"),
    /** Cortada pela estampadeira. */
    CORTADA("Cortada"),
    /** Assada pelo forno. */
    ASSADA("Assada"),
    /** Passou na inspeção e vai para o armazém. */
    APROVADA("Aprovada"),
    /** Barrada na inspeção e vai para o refugo. */
    REPROVADA("Reprovada");

    private final String descricao;

    StatusBolacha(String descricao) {
        this.descricao = descricao;
    }

    /**
     * Texto do status para os relatórios.
     *
     * @return descrição legível do status
     */
    public String getDescricao() {
        return descricao;
    }
}
