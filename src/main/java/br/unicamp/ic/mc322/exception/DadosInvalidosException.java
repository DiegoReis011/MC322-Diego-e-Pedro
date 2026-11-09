package br.unicamp.ic.mc322.exception;

/**
 * Lançada quando o arquivo é XML válido, mas os dados contrariam as regras da
 * fábrica: saúde fora de 0 a 100, estoque negativo, status desconhecido,
 * demanda de uma bolacha que a casa não faz e assim por diante.
 *
 * @author Diego Reis
 */
public class DadosInvalidosException extends PersistenciaException {

    /**
     * Cria o erro com a explicação do dado inválido.
     *
     * @param mensagem explicação do que há de errado no arquivo
     */
    public DadosInvalidosException(String mensagem) {
        super(mensagem);
    }

    /**
     * Cria o erro guardando a falha original que o causou.
     *
     * @param mensagem explicação do que há de errado no arquivo
     * @param causa    falha original
     */
    public DadosInvalidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
