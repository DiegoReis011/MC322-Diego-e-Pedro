public class BolachaAmanteigada extends Produto {

    public BolachaAmanteigada(String id, String nome) {
        super(id, nome, 15.0, 0.9);
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
}
