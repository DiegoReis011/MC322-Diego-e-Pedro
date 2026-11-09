package br.unicamp.ic.mc322.exception;

/**
 * Lançada ao carregar quando o arquivo de persistência não existe. Na primeira
 * execução isso é normal, e quem chama decide como seguir (a aplicação abre uma
 * fábrica nova).
 *
 * @author Diego Reis
 */
public class ArquivoInexistenteException extends PersistenciaException {

    /**
     * Cria o erro com o caminho que não foi encontrado.
     *
     * @param caminho caminho do arquivo procurado
     */
    public ArquivoInexistenteException(String caminho) {
        super("O arquivo " + caminho + " não existe. Salve a planta antes de carregar.");
    }
}
