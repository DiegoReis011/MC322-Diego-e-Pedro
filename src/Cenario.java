// Cada cenario e um pacote fechado de parametros. O gerenciador recebe um e
// repassa para as maquinas; ninguem mais precisa saber qual foi escolhido.
public enum Cenario {
    IDEAL("Ideal", "Fábrica recém-inaugurada, máquinas zeradas e caixa cheio.",
            10000.0, 100.0, 0.5, 1.5),
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

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getBudgetInicial() {
        return budgetInicial;
    }

    public double getSaudeInicialMaquinas() {
        return saudeInicialMaquinas;
    }

    public double getFatorFalha() {
        return fatorFalha;
    }

    public double getDesgasteMaximo() {
        return desgasteMaximo;
    }
}
