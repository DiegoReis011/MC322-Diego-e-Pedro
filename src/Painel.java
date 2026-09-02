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
        int tentativas = 0;
        while (true) {
            System.out.print(pergunta + " (" + minimo + "-" + maximo + "): ");
            String digitado = entrada.nextLine().trim();
            tentativas++;
            try {
                int valor = Integer.parseInt(digitado);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
                reclamar(tentativas, "Só vale número de " + minimo + " a " + maximo + ".");
            } catch (NumberFormatException erro) {
                reclamar(tentativas, "Isso não é número. Digite só números.");
            }
        }
    }

    // Troca virgula por ponto senao "12,5" quebra o parseDouble.
    public double lerDouble(String pergunta, double minimo) {
        int tentativas = 0;
        while (true) {
            System.out.print(pergunta + " (a partir de " + minimo + "): ");
            String digitado = entrada.nextLine().trim().replace(",", ".");
            tentativas++;
            try {
                double valor = Double.parseDouble(digitado);
                if (valor >= minimo) {
                    return valor;
                }
                reclamar(tentativas, "Tem que ser pelo menos " + minimo + ".");
            } catch (NumberFormatException erro) {
                reclamar(tentativas, "Isso não é número. Digite só números.");
            }
        }
    }

    // O contador e local de cada pergunta, entao a implicancia zera quando o
    // operador acerta e a proxima pergunta comeca do zero.
    private void reclamar(int tentativa, String explicacao) {
        System.out.println("[x] " + explicacao);
        if (tentativa == 2) {
            System.out.println("    Segunda vez. É número.");
        } else if (tentativa == 3) {
            System.out.println("    Terceira. A massa tá esfriando.");
        } else if (tentativa >= 4) {
            System.out.println("    Você tá fazendo de propósito, né?");
        }
    }

    public void exibirIntroducao() {
        System.out.println(RISCO);
        System.out.println("             " + NOME_FABRICA);
        System.out.println("    " + SLOGAN);
        System.out.println(RISCO);
        System.out.println("Aqui é bolacha. Não é biscoito.");
        System.out.println("Não é cookie. Não é wafer.");
        System.out.println("É BOLACHA.");
        System.out.println();
        System.out.println("Quem chamar de biscoito paga a fornada.");
        System.out.println();
        System.out.println("Fabricamos: bolacha assada, de massa de verdade.");
        System.out.println("Matéria-prima: massa fresca batida na casa.");
        System.out.println();
        System.out.println("Feito por: " + DUPLA);
        System.out.println(RISCO);
    }

    public void exibirCatalogo(Produto[] bolachas) {
        System.out.println();
        System.out.println("Bolachas da casa:");
        for (int i = 0; i < bolachas.length; i++) {
            Produto bolacha = bolachas[i];
            System.out.println("  " + (i + 1) + " - " + bolacha.getNome()
                    + "  (come " + bolacha.getDemandaMateriaPrima() + " g de "
                    + bolacha.getMateriaPrima().getNome() + ")");
        }
    }

    public void exibirMenu() {
        System.out.println();
        System.out.println(RISCO);
        System.out.println("  1 - Assar uma fornada");
        System.out.println("  2 - Ver o estoque");
        System.out.println("  3 - Repor massa");
        System.out.println("  4 - Fechar a fábrica");
        System.out.println(RISCO);
    }

    public void exibirEstoque(MateriaPrima[] massas) {
        System.out.println();
        System.out.println("Massa na despensa:");
        for (int i = 0; i < massas.length; i++) {
            MateriaPrima massa = massas[i];
            String linha = "  " + (i + 1) + " - " + massa.getId() + "  " + massa.getNome()
                    + ": " + massa.getQuantidade() + " " + massa.getUnidade();
            if (massa.estaAbaixoDoMinimo()) {
                linha = linha + "   <<< tá no fim, repõe antes que vire biscoito";
            }
            System.out.println(linha);
        }
    }

    public void etapa(String mensagem) {
        System.out.println("[ok] " + mensagem);
    }

    public void recusa(String motivo) {
        System.out.println("[x] " + motivo);
        System.out.println("    Fornada cancelada, nada foi gasto.");
    }

    public void conclusao(Produto bolacha, MateriaPrima massa) {
        System.out.println();
        System.out.println(RISCO);
        System.out.println("  FORNADA PRONTA");
        System.out.println(RISCO);
        System.out.println(bolacha.getId() + " - " + bolacha.getNome()
                + " (" + bolacha.getStatus() + ")");
        System.out.println("Feita com " + massa.getNome() + " (" + massa.getId() + ")");
        System.out.println("Sobrou: " + massa.getQuantidade() + " " + massa.getUnidade());
    }

    public void despedida() {
        System.out.println();
        System.out.println("Fábrica fechada. Até amanhã.");
    }
}
