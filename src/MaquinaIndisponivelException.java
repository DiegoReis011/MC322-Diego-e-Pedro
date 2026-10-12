/**
 * Lançada quando uma máquina não pode operar, por estar quebrada ou desligada.
 *
 * @author Diego Reis
 */
public class MaquinaIndisponivelException extends PlantaException {

    /**
     * @param maquina nome da máquina que não pode operar
     * @param motivo  por que ela não pode operar
     */
    public MaquinaIndisponivelException(String maquina, String motivo) {
        super("A máquina " + maquina + " não pode operar: " + motivo);
    }
}
