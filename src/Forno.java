public class Forno extends Maquina {

    // Base do agravo. Bolacha boa leva mais manteiga e queima antes, entao
    // o estrago e proporcional a qualidade dela.
    private static final double AGRAVO_BASE = 0.15;

    public Forno() {
        super("Forno", 500, 0.30, 2.0);
    }

    @Override
    public boolean processar(Produto produto) {
        if (!estaLigada()) {
            return false;
        }
        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(AGRAVO_BASE * produto.getQualidade());
        }
        produto.setStatus(StatusBolacha.ASSADA);
        return true;
    }

    @Override
    public String getTipo() {
        return "Processamento";
    }
}
