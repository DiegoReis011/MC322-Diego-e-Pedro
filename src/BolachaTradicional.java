public class BolachaTradicional extends Produto {

    public BolachaTradicional(String id, String nome) {
        super(id, nome, 10.0, 0.7);
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
        return "Bolacha tradicional";
    }
}
