import java.util.Random;

/**
 * Última máquina da linha: aprova ou reprova a bolacha. Só bolacha aprovada
 * vai para o armazém.
 *
 * <p>A chance de reprovar é a qualidade vezes o rigor (0,30), somada ao risco
 * que a bolacha acumulou na estampadeira e no forno. Bolacha de qualidade alta
 * é julgada com mais rigor. Quando a estação falha, ela erra o julgamento:
 * aprova o que devia reprovar, ou o contrário.</p>
 *
 * @author Pedro Magalhães
 */
public class EstacaoInspecao extends Maquina {

    // Quanto a qualidade pesa no criterio da inspecao.
    private static final double RIGOR = 0.30;

    private Random sorteio;
    private int produtosInspecionados;

    /** Cria a estação com capacidade 500, 10% de falha base e R$ 0,50 por bolacha. */
    public EstacaoInspecao() {
        super("Estação de Inspeção", 500, 0.10, 0.50);
        this.sorteio = new Random();
        this.produtosInspecionados = 0;
    }

    /**
     * {@inheritDoc}
     *
     * <p>O veredito fica no status do produto: {@link StatusBolacha#APROVADA}
     * ou {@link StatusBolacha#REPROVADA}. O retorno diz só se a inspeção rodou,
     * não se a bolacha passou.</p>
     */
    @Override
    public boolean processar(Produto produto) {
        if (!podeOperar()) {
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

    /** {@inheritDoc} */
    @Override
    public String getTipo() {
        return "Inspeção";
    }

    /**
     * Informa quantas bolachas a estação já julgou.
     *
     * @return total de bolachas inspecionadas
     */
    public int getTotalInspecionados() {
        return produtosInspecionados;
    }
}
