import java.util.Random;

public class EstacaoInspecao extends Maquina {

    // Quanto a qualidade pesa no criterio da inspecao.
    private static final double RIGOR = 0.30;

    private Random sorteio;
    private int produtosInspecionados;

    public EstacaoInspecao() {
        super("Estação de Inspeção", 500, 0.10, 0.50);
        this.sorteio = new Random();
        this.produtosInspecionados = 0;
    }

    // Devolve true se a inspecao rodou, nao se a bolacha passou. O veredito
    // fica no status do produto.
    @Override
    public boolean processar(Produto produto) {
        if (!estaLigada()) {
            return false;
        }

        boolean reprovado = sorteio.nextDouble() < calcularChanceRejeicao(produto);

        // Falhar aqui e errar o julgamento, nao travar.
        if (verificarFalha()) {
            reprovado = !reprovado;
        }

        produto.setStatus(reprovado ? StatusBolacha.REPROVADA : StatusBolacha.APROVADA);

        produtosInspecionados++;
        return true;
    }

    private double calcularChanceRejeicao(Produto produto) {
        double chance = produto.getQualidade() * RIGOR
                + produto.getProbabilidadeFalhaAcumulada();
        return Math.min(chance, 1.0);
    }

    @Override
    public String getTipo() {
        return "Inspeção";
    }

    public int getTotalInspecionados() {
        return produtosInspecionados;
    }
}
