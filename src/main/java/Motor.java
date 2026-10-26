/**
 * Motor da máquina (relação 1:1). Um motor gasto trabalha forçado e gasta
 * mais energia, então encarece a operação. Um motor quebrado para a máquina
 * inteira.
 *
 * @author Diego Reis
 */
public class Motor extends ParteDeMaquina {
    // Quanto a operacao encarece com o motor totalmente gasto (50%).
    private static final double ACRESCIMO_CUSTO_MAXIMO = 0.5;

    /** Cria o motor com saúde cheia. */
    public Motor() {
        super("Motor", "OK", "Superaquecido", "Fundido");
    }

    /**
     * Calcula quanto o motor encarece a operação da máquina.
     *
     * @return multiplicador do custo: 1.0 com saúde cheia, até 1.5 com o motor
     *         totalmente gasto
     */
    public double getFatorCusto() {
        return 1.0 + ACRESCIMO_CUSTO_MAXIMO * (1.0 - getSaude() / 100.0);
    }
}
