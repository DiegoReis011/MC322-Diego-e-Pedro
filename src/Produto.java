public abstract class Produto {
    private String id;
    private String nome;
    private String status;
    private double quantidadeMateriaPrimaPorUnidade;
    private double qualidade;
    private double probabilidadeFalhaAcumulada;
    private String sabor;
    private static int totalProdutosFabricados = 0;

    public Produto(String id, String nome, double quantidadeMateriaPrimaPorUnidade,
            double qualidade, String sabor) {
        this.id = id;
        this.nome = nome;
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;
        this.sabor = sabor;
        this.status = "Aguardando processamento";
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public double getProbabilidadeFalhaAcumulada() {
        return probabilidadeFalhaAcumulada;
    }

    public static int getTotalProdutosFabricados() {
        return totalProdutosFabricados;
    }
}
