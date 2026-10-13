/**
 * Raiz de todos os erros de regra de negócio da fábrica NotBiscoito.
 *
 * <p>Capturar esta classe pega qualquer problema da planta (falta de massa,
 * budget curto, demanda inválida, máquina quebrada) sem pegar falhas de
 * programação como {@link NullPointerException}.</p>
 *
 * @author Diego Reis
 */
public class PlantaException extends RuntimeException {

    /**
     * Cria o erro com uma mensagem pronta para ser mostrada ao operador.
     *
     * @param mensagem explicação do que aconteceu
     */
    public PlantaException(String mensagem) {
        super(mensagem);
    }
}
