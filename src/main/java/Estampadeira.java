/**
 * Primeira máquina da linha: corta a massa no formato da bolacha.
 *
 * <p>Aqui a falha não para a máquina: decide se o corte saiu torto. Corte
 * torto aumenta a chance da bolacha ser reprovada depois, na inspeção.</p>
 *
 * @author Pedro Magalhães
 */
public class Estampadeira extends Maquina {

    // Quanto o corte torto piora a chance da bolacha ser reprovada depois.
    private static final double AGRAVO = 0.10;

    /** Cria a estampadeira com capacidade 500, 20% de falha base e R$ 1,00 por bolacha. */
    public Estampadeira() {
        super("Estampadeira", 500, 0.20, 1.0);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Marca a bolacha como {@link StatusBolacha#CORTADA}. Num corte torto,
     * aumenta o risco dela em 0,10.</p>
     */
    @Override
    public boolean processar(Produto produto) {
        if (!podeOperar()) {
            return false;
        }
        if (verificarFalha()) {
            produto.aumentarProbabilidadeFalha(AGRAVO);
        }
        produto.setStatus(StatusBolacha.CORTADA);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getTipo() {
        return "Processamento";
    }
}
