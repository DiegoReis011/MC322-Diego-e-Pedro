import java.util.Random;

/**
 * Equipamento da linha de produção. Cada subclasse decide o que faz com a
 * bolacha em {@link #processar(Produto)}; esta classe cuida do que é comum a
 * todas: ligar e desligar, desgaste, falhas e diagnóstico.
 *
 * <p>Regras de desgaste:</p>
 * <ul>
 *   <li>a saúde vai de 0 a 100 e cai um valor aleatório a cada fornada
 *       ({@link #registrarCiclo()}), até o teto de desgaste do cenário;</li>
 *   <li>a chance de falha é inversamente proporcional à saúde: com metade da
 *       saúde, a máquina falha o dobro;</li>
 *   <li>abaixo de 30 de saúde ela pede manutenção, e abaixo de 10 quebra e não
 *       opera mais até ser reparada.</li>
 * </ul>
 *
 * @author Pedro Magalhães
 */
public abstract class Maquina implements Auditavel {
    private static final double SAUDE_MAXIMA = 100.0;
    // Abaixo disso a maquina pede manutencao.
    private static final double LIMIAR_MANUTENCAO = 30.0;
    // Abaixo disso a maquina para de vez ate ser reparada.
    private static final double LIMIAR_QUEBRA = 10.0;

    private Random random;
    private String nome;
    private boolean ligada;
    private double capacidadeMaxima;
    private double probabilidadeFalhaBase;
    private double custoOperacional;

    private double saude;
    private double fatorFalha;
    private double desgasteMaximo;
    private int ciclos;
    private int historicoFalhas;

    /**
     * Cria a máquina desligada, com saúde cheia e sem cenário aplicado.
     *
     * @param nome              nome mostrado nos relatórios
     * @param capacidadeMaxima  quantas bolachas ela aguenta por fornada
     * @param probabilidadeFalha chance de falha com saúde cheia, de 0 a 1
     *                          (valores fora da faixa são ajustados)
     * @param custoOperacional  quanto ela cobra por bolacha processada, em reais
     */
    public Maquina(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacional) {
        this.random = new Random();
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ligada = false;
        this.probabilidadeFalhaBase = limitar(probabilidadeFalha, 0, 1);
        this.custoOperacional = custoOperacional;
        this.saude = SAUDE_MAXIMA;
        this.fatorFalha = 1.0;
        this.desgasteMaximo = 3.0;
        this.ciclos = 0;
        this.historicoFalhas = 0;
    }

    /**
     * Faz o trabalho desta máquina na bolacha e atualiza o status dela.
     *
     * @param produto bolacha que está passando pela máquina
     * @return {@code true} se a máquina operou, {@code false} se ela estava
     *         desligada ou quebrada e a bolacha passou sem ser tocada
     */
    public abstract boolean processar(Produto produto);

    /**
     * Categoria da máquina para os relatórios.
     *
     * @return tipo da máquina, por exemplo "Processamento"
     */
    public abstract String getTipo();

    /**
     * Ajusta a máquina ao cenário: multiplicador de falha, teto de desgaste e
     * saúde inicial. O gerenciador chama isto quando a máquina entra na linha.
     *
     * @param cenario cenário de operação escolhido no início
     */
    public void aplicarCenario(Cenario cenario) {
        this.fatorFalha = cenario.getFatorFalha();
        this.desgasteMaximo = cenario.getDesgasteMaximo();
        this.saude = limitar(cenario.getSaudeInicialMaquinas(), 0, SAUDE_MAXIMA);
    }

    /** Liga a máquina. Ligada não basta para operar: ela também não pode estar quebrada. */
    public void ligar() {
        ligada = true;
    }

    /** Desliga a máquina. */
    public void desligar() {
        ligada = false;
    }

    /**
     * Diz se a máquina está ligada.
     *
     * @return {@code true} se estiver ligada
     */
    public boolean estaLigada() {
        return ligada;
    }

    /**
     * Diz se a máquina pode trabalhar agora. Máquina quebrada não roda nem
     * ligada.
     *
     * @return {@code true} se estiver ligada e não estiver quebrada
     */
    public boolean podeOperar() {
        return ligada && !estaQuebrada();
    }

    /**
     * Diz se a máquina quebrou.
     *
     * @return {@code true} se a saúde estiver abaixo de 10
     */
    public boolean estaQuebrada() {
        return saude < LIMIAR_QUEBRA;
    }

    /**
     * Registra uma fornada: tira da saúde um valor aleatório entre 0 e o teto
     * de desgaste do cenário. Chamado uma vez por fornada.
     */
    public void registrarCiclo() {
        saude = limitar(saude - random.nextDouble() * desgasteMaximo, 0, SAUDE_MAXIMA);
        ciclos++;
    }

    /** Devolve a máquina a 100% de saúde. Quem cobra o reparo é o gerenciador. */
    public void reparar() {
        saude = SAUDE_MAXIMA;
    }

    /**
     * Calcula a chance de falha agora. Com saúde cheia é a taxa base vezes o
     * fator do cenário; com metade da saúde, o dobro.
     *
     * @return chance de falha, de 0 a 1 (1 se a saúde chegou a zero)
     */
    public double calcularProbabilidadeFalha() {
        if (saude <= 0) {
            return 1.0;
        }
        double chance = probabilidadeFalhaBase * fatorFalha * (SAUDE_MAXIMA / saude);
        return limitar(chance, 0, 1);
    }

    /**
     * Traduz a saúde num estado do enum.
     *
     * @return {@link EstadoMaquina#QUEBRADA}, {@link EstadoMaquina#PRECISA_MANUTENCAO}
     *         ou {@link EstadoMaquina#OPERANDO}
     */
    public EstadoMaquina getEstado() {
        if (estaQuebrada()) {
            return EstadoMaquina.QUEBRADA;
        }
        if (precisaManutencao()) {
            return EstadoMaquina.PRECISA_MANUTENCAO;
        }
        return EstadoMaquina.OPERANDO;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Para máquina: saúde, chance de falha, falhas por fornada e estado.</p>
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("%-20s | saúde %5.1f%% | chance de falha %5.1f%% | %d falhas em %d fornadas | %s",
                nome, saude, calcularProbabilidadeFalha() * 100, historicoFalhas, ciclos,
                getEstado().getDescricao());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Para máquina: saúde abaixo de 30.</p>
     */
    @Override
    public boolean precisaManutencao() {
        return saude < LIMIAR_MANUTENCAO;
    }

    /**
     * Informa o nome da máquina.
     *
     * @return nome da máquina
     */
    public String getNome() {
        return nome;
    }

    /**
     * Informa quantas bolachas ela aguenta por fornada.
     *
     * @return capacidade por fornada
     */
    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    /**
     * Informa quanto a máquina cobra para processar uma bolacha.
     *
     * @return custo por bolacha, em reais
     */
    public double getCustoOperacional() {
        return custoOperacional;
    }

    /**
     * Informa a saúde atual.
     *
     * @return saúde, de 0 a 100
     */
    public double getSaude() {
        return saude;
    }

    /**
     * Sorteia se a máquina falhou nesta bolacha, usando a chance atual, e
     * conta a falha no histórico. O que a falha significa (corte torto,
     * bolacha queimada, julgamento errado) é a subclasse que decide.
     *
     * @return {@code true} se falhou
     */
    protected boolean verificarFalha() {
        boolean falhou = random.nextDouble() < calcularProbabilidadeFalha();
        if (falhou) {
            historicoFalhas++;
        }
        return falhou;
    }

    private static double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
