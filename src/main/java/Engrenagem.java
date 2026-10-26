/**
 * Engrenagem da máquina (relação 1:N: cada máquina tem várias). Cada engrenagem
 * quebrada tira um pedaço da capacidade da máquina, usando
 * {@link #getEficiencia()}.
 *
 * @author Diego Reis
 */
public class Engrenagem extends ParteDeMaquina {

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

    // Valida antes do super(): o construtor da mae precisa do nome pronto.
    private static int validar(int numero) {
        if (numero <= 0) {
            throw new IllegalArgumentException("O número da engrenagem precisa ser maior que zero.");
        }
        return numero;
    }
}
