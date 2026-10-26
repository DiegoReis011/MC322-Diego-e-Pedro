/**
 * Painel de controle da máquina (relação 1:1). É ele que calibra o trabalho:
 * quanto mais descalibrado, mais a máquina erra.
 *
 * @author Diego Reis
 */
public class PainelDeControle extends ParteDeMaquina {
    // Quanto a chance de falha da maquina sobe com o painel totalmente
    // descalibrado (15 pontos percentuais).
    private static final double ACRESCIMO_FALHA_MAXIMO = 0.15;

    /** Cria o painel com saúde cheia, ou seja, calibrado. */
    public PainelDeControle() {
        super("Painel de Controle", "Calibrado", "Descalibrado", "Pifou");
    }

    /**
     * Calcula quanto a descalibragem soma à chance de falha da máquina.
     *
     * @return acréscimo de 0.0 (painel calibrado) a 0.15 (painel pifado), a
     *         somar na probabilidade de falha
     */
    public double getAcrescimoFalha() {
        return ACRESCIMO_FALHA_MAXIMO * (1.0 - getSaude() / 100.0);
    }
}
