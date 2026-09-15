import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        Painel painel = new Painel(teclado);

        MateriaPrima massaChocolate = new MateriaPrima("MP001", "massa de chocolate", 5000, "g", 200);
        MateriaPrima massaAveia = new MateriaPrima("MP002", "massa de aveia", 5000, "g", 200);
        MateriaPrima massaCoco = new MateriaPrima("MP003", "massa de coco", 5000, "g", 200);
        MateriaPrima[] massas = { massaChocolate, massaAveia, massaCoco };

        // Os dois vetores andam juntos: bolachas[i] e feita de massas[i].
        Produto[] bolachas = {
            new Produto("B001", "Bolacha de chocolate", 80, massaChocolate),
            new Produto("B002", "Bolacha de aveia", 70, massaAveia),
            new Produto("B003", "Bolacha de coco", 65, massaCoco)
        };


        painel.exibirIntroducao();

        int opcao = 0;
        while (opcao != 4) {
            painel.exibirCatalogo(bolachas);
            painel.exibirMenu();
            opcao = painel.lerInteiro("O que vai ser", 1, 4);

            if (opcao == 1) {
                // A linha de producao da tarefa 1 saiu daqui. Quem vai assar
                // e o GerenciadorProducao, que ainda nao existe.
                painel.etapa("Linha de produção em obras.");
            } else if (opcao == 2) {
                painel.exibirEstoque(massas);
            } else if (opcao == 3) {
                reporEstoque(painel, massas);
            }
        }

        painel.despedida();
        teclado.close();
    }

    private static void reporEstoque(Painel painel, MateriaPrima[] massas) {
        painel.exibirEstoque(massas);
        int escolha = painel.lerInteiro("Qual massa vai repor", 1, massas.length);
        MateriaPrima massa = massas[escolha - 1];

        double quantidade = painel.lerDouble("Quanto de " + massa.getNome() + " em gramas", 1);
        massa.adicionarEstoque(quantidade);

        painel.etapa(quantidade + " g de " + massa.getNome() + " na despensa. Agora tem "
                + massa.getQuantidade() + " " + massa.getUnidade() + ".");
    }
}
