package br.unicamp.ic.mc322.model.maquina;

import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.model.produto.StatusBolacha;

/**
 * Segunda máquina da linha: assa a bolacha cortada.
 *
 * <p>Quando o forno falha, a bolacha sai queimada demais e fica mais perto de
 * ser reprovada na inspeção. Bolacha boa leva mais manteiga e queima antes,
 * então o estrago é proporcional à qualidade dela.</p>
 *
 * @author Pedro Magalhães
 */
public class Forno extends Maquina {

    // Base do agravo, multiplicada pela qualidade da bolacha.
    private static final double AGRAVO_BASE = 0.15;

    /** Cria o forno com capacidade 500, 30% de falha base e R$ 2,00 por bolacha. */
    public Forno() {
        this(500, 0.30, 2.0, 3);
    }

    /**
     * Cria o forno com a configuração dada. Serve para reconstruir um forno
     * salvo em arquivo.
     *
     * @param capacidadeMaxima      bolachas por fornada
     * @param probabilidadeFalha    chance de falha com saúde cheia, de 0 a 1
     * @param custoOperacional      reais por bolacha
     * @param quantidadeEngrenagens engrenagens da máquina (pelo menos 1)
     * @throws IllegalArgumentException se a quantidade de engrenagens for menor que 1
     */
    public Forno(double capacidadeMaxima, double probabilidadeFalha,
            double custoOperacional, int quantidadeEngrenagens) {
        super("Forno", capacidadeMaxima, probabilidadeFalha, custoOperacional,
                quantidadeEngrenagens);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Marca a bolacha como {@link StatusBolacha#ASSADA}. Numa falha, aumenta
     * o risco dela em 0,15 vezes a qualidade.</p>
     */
    @Override
    public boolean processar(Produto produto) {
        if (!podeOperar()) {
            return false;
        }
        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(AGRAVO_BASE * produto.getQualidade());
        }
        produto.setStatus(StatusBolacha.ASSADA);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getTipo() {
        return "Processamento";
    }
}
