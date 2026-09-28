public abstract class Produto implements Auditavel {
    private String id;
    private String nome;
    private StatusBolacha status;
    private double quantidadeMateriaPrimaPorUnidade;
    private double qualidade;
    private double probabilidadeFalhaAcumulada;
    private String sabor;
    // Numero da fornada que produziu a bolacha. 0 enquanto for so molde.
    private int lote;
    private static int totalProdutosFabricados = 0;
    // limita qnd a bolacha é de risco, só chega em 0.15 se ambas as maquinas de processamento errarem.
    private static final double LIMIAR_RISCO = 0.15;

    public Produto(String id, String nome, double quantidadeMateriaPrimaPorUnidade,
            double qualidade, String sabor) {
        this.id = id;
        this.nome = nome;
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;
        this.sabor = sabor;
        this.status = StatusBolacha.AGUARDANDO;
        this.probabilidadeFalhaAcumulada = 0.0;
        totalProdutosFabricados++;
    }

    public abstract void processar();
    public abstract double calcularTempoProducao();
    public abstract String getTipo();

    // Deixa o gerenciador pedir copias sem saber qual bolacha e.
    public abstract Produto criarCopia(String novoId);

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public StatusBolacha getStatus() {
        return status;
    }

    public void setStatus(StatusBolacha status) {
        this.status = status;
    }

    public double getQuantidadeMateriaPrimaPorUnidade() {
        return quantidadeMateriaPrimaPorUnidade;
    }

    public double getQualidade() {
        return qualidade;
    }

    // O sabor diz qual ingrediente a fabrica gasta. O tipo, que e a subclasse,
    // diz quanta massa.
    public String getSabor() {
        return sabor;
    }

    public void aumentarProbabilidadeFalha(double incremento) {
        probabilidadeFalhaAcumulada += incremento;
        if (probabilidadeFalhaAcumulada > 1.0) {
            probabilidadeFalhaAcumulada = 1.0;
        }
    }

    public int getLote() {
        return lote;
    }

    public void setLote(int lote) {
        this.lote = lote;
    }

    public double getProbabilidadeFalhaAcumulada() {
        return probabilidadeFalhaAcumulada;
    }

    public static int getTotalProdutosFabricados() {
        return totalProdutosFabricados;
    }
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("[%s] %s | qualidade %.2f | risco acumulado %.1f%% (%s) | status: %s",
                id, nome, qualidade, probabilidadeFalhaAcumulada * 100,
                classificarRisco(), status.getDescricao());
    }

    @Override
    public boolean precisaManutencao() {
        return probabilidadeFalhaAcumulada >= LIMIAR_RISCO;
    }
    private String classificarRisco() {
        if (probabilidadeFalhaAcumulada >= LIMIAR_RISCO) {
            return "alto";
        }
        if (probabilidadeFalhaAcumulada >= LIMIAR_RISCO / 2) {
            return "moderado";
        }
        return "baixo";
    }
}
