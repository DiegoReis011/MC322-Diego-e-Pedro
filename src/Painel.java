import java.util.Scanner;

public class Painel {
    private static final String RISCO = "========================================";
    private static final String NOME_FABRICA = "NOTBISCOITO";
    private static final String SLOGAN = "\"aqui temos bolacha de verdade\"";
    private static final String DUPLA = "Pedro Magalhães e Diego Reis";

    private Scanner entrada;

    public Painel(Scanner entrada) {
        this.entrada = entrada;
    }

    // Lê a linha inteira e converte na mão. Com nextInt() direto, digitar uma
    // letra trava o programa num loop infinito.
    public int lerInteiro(String pergunta, int minimo, int maximo) {
        while (true) {
            System.out.print(pergunta + " (" + minimo + "-" + maximo + "): ");
            String digitado = entrada.nextLine().trim();
            try {
                int valor = Integer.parseInt(digitado);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
                System.out.println("Só vale número de " + minimo + " a " + maximo + ".");
            } catch (NumberFormatException erro) {
                System.out.println("Isso não é número. Digite só números.");
            }
        }
    }

    // Troca virgula por ponto senao "12,5" quebra o parseDouble.
    public double lerDouble(String pergunta, double minimo) {
        while (true) {
            System.out.print(pergunta + " (a partir de " + minimo + "): ");
            String digitado = entrada.nextLine().trim().replace(",", ".");
            try {
                double valor = Double.parseDouble(digitado);
                if (valor >= minimo) {
                    return valor;
                }
                System.out.println("Tem que ser pelo menos " + minimo + ".");
            } catch (NumberFormatException erro) {
                System.out.println("Isso não é número. Digite só números.");
            }
        }
    }
}
