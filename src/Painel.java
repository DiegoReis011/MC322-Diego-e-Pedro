import java.util.Scanner;

public class Painel {
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
}
