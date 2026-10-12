/**
 * Lançada quando uma demanda não existe ou quando a operação pedida não é
 * compatível com o estado atual dela (por exemplo, concluir uma demanda
 * cancelada).
 *
 * @author Diego Reis
 */
public class DemandaInvalidaException extends PlantaException {

    /**
     * @param mensagem explicação do que há de errado com a demanda
     */
    public DemandaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
