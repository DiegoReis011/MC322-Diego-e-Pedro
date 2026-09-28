import java.util.Random;

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

    public abstract boolean processar(Produto produto);

    public abstract String getTipo();

    // O cenario chega pelo gerenciador quando a maquina entra na linha.
    public void aplicarCenario(Cenario cenario) {
        this.fatorFalha = cenario.getFatorFalha();
        this.desgasteMaximo = cenario.getDesgasteMaximo();
        this.saude = limitar(cenario.getSaudeInicialMaquinas(), 0, SAUDE_MAXIMA);
    }

    public void ligar() {
        ligada = true;
    }

    public void desligar() {
        ligada = false;
    }

    public boolean estaLigada() {
        return ligada;
    }

    // Ligada nao basta: maquina quebrada nao roda nem ligada.
    public boolean podeOperar() {
        return ligada && !estaQuebrada();
    }

    public boolean estaQuebrada() {
        return saude < LIMIAR_QUEBRA;
    }

    // Chamado uma vez por fornada: cada ciclo tira um pouco de saude.
    public void registrarCiclo() {
        saude = limitar(saude - random.nextDouble() * desgasteMaximo, 0, SAUDE_MAXIMA);
        ciclos++;
    }

    public void reparar() {
        saude = SAUDE_MAXIMA;
    }

    // Saude cheia falha na taxa base do cenario; com metade da saude, o dobro.
    public double calcularProbabilidadeFalha() {
        if (saude <= 0) {
            return 1.0;
        }
        double chance = probabilidadeFalhaBase * fatorFalha * (SAUDE_MAXIMA / saude);
        return limitar(chance, 0, 1);
    }

    public EstadoMaquina getEstado() {
        if (estaQuebrada()) {
            return EstadoMaquina.QUEBRADA;
        }
        if (precisaManutencao()) {
            return EstadoMaquina.PRECISA_MANUTENCAO;
        }
        return EstadoMaquina.OPERANDO;
    }

    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("%-20s | saúde %5.1f%% | chance de falha %5.1f%% | %d falhas em %d fornadas | %s",
                nome, saude, calcularProbabilidadeFalha() * 100, historicoFalhas, ciclos,
                getEstado().getDescricao());
    }

    @Override
    public boolean precisaManutencao() {
        return saude < LIMIAR_MANUTENCAO;
    }

    public String getNome() {
        return nome;
    }

    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public double getCustoOperacional() {
        return custoOperacional;
    }

    public double getSaude() {
        return saude;
    }

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
