package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando salvar ou carregar o estado da planta falha: erro de leitura
 * ou de escrita, arquivo vazio ou XML malformado.
 *
 * <p>Quando o problema é o conteúdo do arquivo (valores que contrariam as
 * regras da fábrica), a exceção é a subclasse {@link DadosInvalidosException}.</p>
 *
 * @author Diego Reis
 */
public class PersistenciaException extends PlantaException {

    /**
     * Cria o erro com a explicação do problema.
     *
     * @param mensagem explicação pronta para ser mostrada ao operador
     */
    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria o erro guardando a falha original que o causou.
     *
     * @param mensagem explicação pronta para ser mostrada ao operador
     * @param causa    falha original (por exemplo, uma {@link java.io.IOException})
     */
    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
