public class Estampadeira extends Maquina {

    // Quanto o corte torto piora a chance da bolacha ser reprovada depois.
    private static final double AGRAVO = 0.10;

    public Estampadeira() {
        super("Estampadeira", 500, 0.20, 1.0);
    }

    @Override
    public boolean processar(Produto produto) {
        if (!estaLigada()) {
            return false;
        }
        // Aqui o sorteio nao quebra a maquina: decide se o corte saiu torto.
        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(AGRAVO);
        }
        produto.setStatus("Cortado");
        return true;
    }

    @Override
    public String getTipo() {
        return "Processamento";
    }
}
