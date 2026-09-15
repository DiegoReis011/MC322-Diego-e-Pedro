public class BolachaTradicional extends Produto {

    public BolachaTradicional(String id, String sabor) {
        super(id, "Bolacha tradicional de " + sabor, 10.0, 0.7, sabor);
    }

    @Override
    public void processar() {
        setStatus("Processado");
    }

    @Override
    public double calcularTempoProducao() {
        return 8.0;
    }

    @Override
    public String getTipo() {
        return "Bolacha Tradicional";
    }

    @Override
    public Produto criarCopia(String novoId) {
        return new BolachaTradicional(novoId, getSabor());
    }
}
