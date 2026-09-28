public class BolachaCracker extends Produto {

    public BolachaCracker(String id, String sabor) {
        super(id, "Bolacha cracker de " + sabor, 6.0, 0.5, sabor);
    }

    @Override
    public void processar() {
        setStatus(StatusBolacha.PROCESSADA);
    }

    @Override
    public double calcularTempoProducao() {
        return 5.0;
    }

    @Override
    public String getTipo() {
        return "Bolacha Cracker";
    }

    @Override
    public Produto criarCopia(String novoId) {
        return new BolachaCracker(novoId, getSabor());
    }
}
