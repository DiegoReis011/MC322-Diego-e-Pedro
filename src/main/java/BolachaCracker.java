/**
 * Bolacha cracker: a mais simples da casa, com qualidade 0.5, 6 g de massa por
 * unidade e a produção mais rápida. Por ter critérios mais flexíveis, é a que
 * menos reprova na inspeção.
 *
 * @author Diego Reis
 */
public class BolachaCracker extends Produto {

    /**
     * Cria uma cracker do sabor pedido.
     *
     * @param id    código da bolacha
     * @param sabor sabor, que é o nome do ingrediente gasto
     */
    public BolachaCracker(String id, String sabor) {
        super(id, "Bolacha cracker de " + sabor, 6.0, 0.5, sabor);
    }

    /** {@inheritDoc} A cracker passa a ser marcada como processada. */
    @Override
    public void processar() {
        setStatus(StatusBolacha.PROCESSADA);
    }

    /** {@inheritDoc} A cracker leva 5, o menor tempo dos três tipos. */
    @Override
    public double calcularTempoProducao() {
        return 5.0;
    }

    /** {@inheritDoc} */
    @Override
    public String getTipo() {
        return "Bolacha Cracker";
    }

    /** {@inheritDoc} */
    @Override
    public Produto criarCopia(String novoId) {
        return new BolachaCracker(novoId, getSabor());
    }
}
