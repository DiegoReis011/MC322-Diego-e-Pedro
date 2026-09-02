public class Produto {
    private String id;
    private String nome;
    private String status;
    private double quantidadeMateriaPrimaNecessaria;
    private MateriaPrima materiaPrima;

    public Produto(String id, String nome, double quantidadeMateriaPrimaNecessaria, MateriaPrima materiaPrima) {
        this.id = id;
        this.nome = nome;
        this.quantidadeMateriaPrimaNecessaria = quantidadeMateriaPrimaNecessaria;
        this.materiaPrima = materiaPrima;
        this.status = "Aguardando processamento";
    }

    public void processar() {
        status = "Processado";
    }

    public void aprovar() {
        status = "Inspecionado";
    }

    public void definirDemandaMateriaPrima(double novaDemanda) {
        if (novaDemanda > 0) {
            quantidadeMateriaPrimaNecessaria = novaDemanda;
        }
    }

    public double getDemandaMateriaPrima() {
        return quantidadeMateriaPrimaNecessaria;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getStatus() {
        return status;
    }

    // Guardar a massa aqui e o que deixa a gente saber de onde veio cada bolacha.
    public MateriaPrima getMateriaPrima() {
        return materiaPrima;
    }
}
