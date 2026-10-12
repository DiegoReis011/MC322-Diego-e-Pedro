/**
 * Lançada quando o budget não cobre o custo de uma compra ou de uma fornada.
 *
 * @author Diego Reis
 */
public class OrcamentoInsuficienteException extends PlantaException {

    /**
     * @param necessario custo da operação, em reais
     * @param disponivel budget que havia no caixa, em reais
     */
    public OrcamentoInsuficienteException(double necessario, double disponivel) {
        super(String.format("A operação custa R$ %.2f e o budget tem R$ %.2f.",
                necessario, disponivel));
    }
}
