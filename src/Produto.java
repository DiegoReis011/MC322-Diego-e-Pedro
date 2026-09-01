public class Produto {
    private String id;
    private String nome;
    private String status;
    private double demandaMateriaPrima;

    public Produto(String id, String nome, double demandaMateriaPrima) {
        this.id = id;
        this.nome = nome;
        this.demandaMateriaPrima = demandaMateriaPrima;
        this.status = "Aguardando processamento";
    }

    public void processar() {
        status = "Processado";
    }

    public void definirDemandaMateriaPrima(double novaDemanda) {
        if (novaDemanda > 0) {
            demandaMateriaPrima = novaDemanda;
        }
    }

    public double getDemandaMateriaPrima() {
        return demandaMateriaPrima;
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
}
