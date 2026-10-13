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
        super("Forno", 500, 0.30, 2.0);
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
