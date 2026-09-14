public class BolachaCracker extends Produto {

    public BolachaCracker(String id, String nome) {
        super(id, nome, 6.0, 0.5);
    }

    @Override
    public void processar() {
        setStatus("Processado");
    }

    @Override
    public double calcularTempoProducao() {
        return 5.0;
    }

    @Override
    public String getTipo() {
        return "Bolacha Cracker";
    }
}
