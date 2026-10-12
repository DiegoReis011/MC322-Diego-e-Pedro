/**
 * Lançada quando falta matéria-prima (massa ou ingrediente) para uma operação.
 *
 * @author Diego Reis
 */
public class RecursoInsuficienteException extends PlantaException {

    /**
     * Cria o erro com o que faltou, quanto era preciso e quanto havia.
     *
     * @param recurso    nome do insumo que faltou
     * @param necessario quantidade que a operação pedia
     * @param disponivel quantidade que havia em estoque
     * @param unidade    unidade de medida, por exemplo "g"
     */
    public RecursoInsuficienteException(String recurso, double necessario,
            double disponivel, String unidade) {
        super(String.format("Faltou %s: precisa de %.1f %s e só tem %.1f %s. "
                + "Compre mais no menu de matéria-prima.",
                recurso, necessario, unidade, disponivel, unidade));
    }
}
