public class BolachaAmanteigada extends Produto {

    public BolachaAmanteigada(String id, String sabor) {
        super(id, "Bolacha amanteigada de " + sabor, 15.0, 0.9, sabor);
    }

    @Override
    public void processar() {
        setStatus("Processado");
    }

    @Override
    public double calcularTempoProducao() {
        return 12.0;
    }

    @Override
    public String getTipo() {
        return "Bolacha Amanteigada";
    }

    @Override
    public Produto criarCopia(String novoId) {
        return new BolachaAmanteigada(novoId, getSabor());
    }
}
