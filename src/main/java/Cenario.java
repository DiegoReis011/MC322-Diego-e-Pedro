/**
 * Cenários de operação escolhidos no início da execução. Cada um é um pacote
 * fechado de parâmetros: o gerenciador recebe um e repassa para as máquinas,
 * e ninguém mais precisa saber qual foi escolhido.
 *
 * @author Pedro Magalhães
 */
public enum Cenario {
    /** Budget farto, máquinas novas, pouca falha e pouco desgaste. */
    IDEAL("Ideal", "Fábrica recém-inaugurada, máquinas zeradas e caixa cheio.",
            10000.0, 100.0, 0.5, 1.5),
    /** Budget só pro básico, máquinas gastas, muita falha e desgaste acelerado. */
    APOCALIPTICO("Apocalíptico", "Máquinas de segunda mão, caixa no vermelho e o forno fazendo barulho.",
            400.0, 70.0, 1.5, 10.0);

    private final String nome;
    private final String descricao;
    private final double budgetInicial;
    private final double saudeInicialMaquinas;
    // Multiplica a chance de falha base de cada maquina.
    private final double fatorFalha;
    // Teto do desgaste sorteado a cada fornada.
    private final double desgasteMaximo;

    Cenario(String nome, String descricao, double budgetInicial, double saudeInicialMaquinas,
            double fatorFalha, double desgasteMaximo) {
        this.nome = nome;
        this.descricao = descricao;
        this.budgetInicial = budgetInicial;
        this.saudeInicialMaquinas = saudeInicialMaquinas;
        this.fatorFalha = fatorFalha;
        this.desgasteMaximo = desgasteMaximo;
    }

    /**
     * Nome do cenário para o menu.
     *
     * @return nome do cenário
     */
    public String getNome() {
        return nome;
    }

    /**
     * Frase que descreve o cenário na escolha inicial.
     *
     * @return descrição do cenário
     */
    public String getDescricao() {
        return descricao;
    }

    /**
     * Dinheiro em caixa na abertura da fábrica.
     *
     * @return budget inicial, em reais
     */
    public double getBudgetInicial() {
        return budgetInicial;
    }

    /**
     * Saúde com que as máquinas entram na linha.
     *
     * @return saúde inicial, de 0 a 100
     */
    public double getSaudeInicialMaquinas() {
        return saudeInicialMaquinas;
    }

    /**
     * Multiplicador da chance de falha base de cada máquina.
     *
     * @return fator de falha (abaixo de 1 falha menos, acima de 1 falha mais)
     */
    public double getFatorFalha() {
        return fatorFalha;
    }

    /**
     * Maior desgaste que uma fornada pode tirar da saúde de cada máquina.
     *
     * @return teto do desgaste por fornada, em pontos de saúde
     */
    public double getDesgasteMaximo() {
        return desgasteMaximo;
    }
}
