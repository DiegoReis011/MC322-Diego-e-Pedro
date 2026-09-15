import java.util.ArrayList;
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

    public void exibirIntroducao(double budget) {
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
        System.out.println("A casa faz três tipos: amanteigada, tradicional e");
        System.out.println("cracker. O sabor vem do ingrediente que entra junto");
        System.out.println("com a massa, então a mesma linha faz nove bolachas");
        System.out.println("diferentes sem trocar de máquina.");
        System.out.println();
        System.out.println("A linha tem estampadeira, forno e inspeção. As duas");
        System.out.println("primeiras não quebram, mas estragam bolacha de vez");
        System.out.println("em quando. A inspeção reprova - e é mais dura com a");
        System.out.println("bolacha boa, que é o que casa séria faz.");
        System.out.println();
        System.out.println("Você começa com R$ " + String.format("%.2f", budget) + ". Massa e ingrediente");
        System.out.println("saem do seu bolso, e cada máquina cobra pra rodar.");
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
                    + "  (" + bolacha.getQuantidadeMateriaPrimaPorUnidade()
                    + " g de massa + " + bolacha.getSabor() + ")");
        }
    }

    public void exibirMenu(Produto[] bolachas) {
        System.out.println();
        System.out.println(RISCO);
        System.out.println("  ATUALIZAR DEMANDAS");
        for (int i = 0; i < bolachas.length; i++) {
            System.out.println("  " + (i + 1) + " - Atualizar demanda de " + bolachas[i].getNome());
        }
        System.out.println();
        System.out.println("  FABRICAR");
        for (int i = 0; i < bolachas.length; i++) {
            System.out.println("  " + (i + 4) + " - Fabricar " + bolachas[i].getNome());
        }
        System.out.println();
        System.out.println("  CONSULTAR");
        System.out.println("  7 - Ver armazém");
        System.out.println("  8 - Ver estoque da despensa");
        System.out.println();
        System.out.println("  COMPRAR");
        System.out.println("  9 - Comprar matéria-prima");
        System.out.println();
        System.out.println("  0 - Fechar a fábrica");
        System.out.println(RISCO);
    }

    public void exibirEstoque(ArrayList<MateriaPrima> insumos) {
        System.out.println();
        System.out.println("Na despensa:");
        for (int i = 0; i < insumos.size(); i++) {
            MateriaPrima insumo = insumos.get(i);
            String linha = "  " + (i + 1) + " - " + insumo.getId() + "  " + insumo.getNome()
                    + ": " + insumo.getQuantidade() + " " + insumo.getUnidade();
            if (insumo.estaAbaixoDoMinimo()) {
                linha = linha + "   <<< tá no fim, repõe antes que vire biscoito";
            }
            System.out.println(linha);
        }
    }

    public void exibirBudget(double budget) {
        System.out.println();
        System.out.println("BUDGET ATUAL: R$ " + String.format("%.2f", budget));
    }

    public void exibirArmazem(ArrayList<Produto> armazem) {
        System.out.println();
        if (armazem.isEmpty()) {
            System.out.println("Armazém vazio. Nenhuma bolacha aprovada ainda.");
            return;
        }
        System.out.println("Armazém (" + armazem.size() + " bolachas):");
        int limite = 15;
        for (int i = 0; i < armazem.size() && i < limite; i++) {
            Produto bolacha = armazem.get(i);
            System.out.println("  " + bolacha.getId() + " - " + bolacha.getNome());
        }
        if (armazem.size() > limite) {
            System.out.println("  ... e mais " + (armazem.size() - limite) + ".");
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
