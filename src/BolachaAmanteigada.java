/**
 * Bolacha amanteigada: a de maior qualidade (0.9), a que mais gasta massa
 * (15 g) e a mais demorada de fazer. Por ser a mais exigente, é também a mais
 * rejeitada na inspeção.
 *
 * @author Diego Reis
 */
public class BolachaAmanteigada extends Produto {

    /**
     * Cria uma amanteigada do sabor pedido.
     *
     * @param id    código da bolacha
     * @param sabor sabor, que é o nome do ingrediente gasto
     */
    public BolachaAmanteigada(String id, String sabor) {
        super(id, "Bolacha amanteigada de " + sabor, 15.0, 0.9, sabor);
    }

    /** {@inheritDoc} A amanteigada passa a ser marcada como processada. */
    @Override
    public void processar() {
        setStatus(StatusBolacha.PROCESSADA);
    }

    /** {@inheritDoc} A amanteigada leva 12, o maior tempo dos três tipos. */
    @Override
    public double calcularTempoProducao() {
        return 12.0;
    }

    /** {@inheritDoc} */
    @Override
    public String getTipo() {
        return "Bolacha Amanteigada";
    }

    /** {@inheritDoc} */
    @Override
    public Produto criarCopia(String novoId) {
        return new BolachaAmanteigada(novoId, getSabor());
    }
}
