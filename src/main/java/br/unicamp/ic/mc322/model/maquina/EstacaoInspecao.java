package br.unicamp.ic.mc322.model.maquina;

import java.util.Random;

import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.model.produto.StatusBolacha;

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
        this(500, 0.10, 0.50, 3);
    }

    /**
     * Cria a estação com a configuração dada. Serve para reconstruir uma
     * estação salva em arquivo.
     *
     * @param capacidadeMaxima      bolachas por fornada
     * @param probabilidadeFalha    chance de falha com saúde cheia, de 0 a 1
     * @param custoOperacional      reais por bolacha
     * @param quantidadeEngrenagens engrenagens da máquina (pelo menos 1)
     * @throws IllegalArgumentException se a quantidade de engrenagens for menor que 1
     */
    public EstacaoInspecao(double capacidadeMaxima, double probabilidadeFalha,
            double custoOperacional, int quantidadeEngrenagens) {
        super("Estação de Inspeção", capacidadeMaxima, probabilidadeFalha, custoOperacional,
                quantidadeEngrenagens);
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
