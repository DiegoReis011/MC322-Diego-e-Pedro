/**
 * Engrenagem da máquina (relação 1:N: cada máquina tem várias). Cada engrenagem
 * gasta faz a máquina errar um pouco mais: com a engrenagem quebrada, a chance
 * de falha da máquina sobe 5 pontos percentuais, então três engrenagens
 * quebradas somam 15.
 *
 * @author Diego Reis
 */
public class Engrenagem extends ParteDeMaquina {
    // Quanto a chance de falha da maquina sobe por esta engrenagem totalmente
    // gasta (5 pontos percentuais).
    private static final double ACRESCIMO_FALHA_MAXIMO = 0.05;

    /**
     * Cria uma engrenagem com saúde cheia.
     *
     * @param numero posição da engrenagem na máquina, a partir de 1, só para
     *               identificá-la nos relatórios
     * @throws IllegalArgumentException se o número não for maior que zero
     */
    public Engrenagem(int numero) {
        super("Engrenagem " + validar(numero), "OK", "Gasta", "Quebrada");
    }

    /**
     * Calcula quanto o desgaste desta engrenagem soma à chance de falha da
     * máquina.
     *
     * @return acréscimo de 0.0 (engrenagem nova) a 0.05 (engrenagem quebrada)
     */
    public double getAcrescimoFalha() {
        return ACRESCIMO_FALHA_MAXIMO * (1.0 - getEficiencia());
    }

    // Valida antes do super(): o construtor da mae precisa do nome pronto.
    private static int validar(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("O número da engrenagem precisa ser maior que zero.");
        }
        return numero;
    }
}
