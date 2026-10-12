/**
 * Bolacha tradicional: o meio-termo da casa, com qualidade 0.7 e 10 g de massa
 * por unidade.
 *
 * @author Diego Reis
 */
public class BolachaTradicional extends Produto {

    /**
     * Cria uma tradicional do sabor pedido.
     *
     * @param id    código da bolacha
     * @param sabor sabor, que é o nome do ingrediente gasto
     */
    public BolachaTradicional(String id, String sabor) {
        super(id, "Bolacha tradicional de " + sabor, 10.0, 0.7, sabor);
    }

    /** {@inheritDoc} A tradicional passa a ser marcada como processada. */
    @Override
    public void processar() {
        setStatus(StatusBolacha.PROCESSADA);
    }

    /** {@inheritDoc} A tradicional leva 8, o tempo intermediário. */
    @Override
    public double calcularTempoProducao() {
        return 8.0;
    }

    /** {@inheritDoc} */
    @Override
    public String getTipo() {
        return "Bolacha Tradicional";
    }

    /** {@inheritDoc} */
    @Override
    public Produto criarCopia(String novoId) {
        return new BolachaTradicional(novoId, getSabor());
    }
}
