import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        Painel painel = new Painel(teclado);

        // A massa e comum a todas: o tipo da bolacha decide quanta massa vai.
        MateriaPrima massa = new MateriaPrima("MP001", "massa da casa", 5000, "g", 200, 0.30);

        // O sabor decide qual desses ingredientes a fornada gasta.
        MateriaPrima cacau = new MateriaPrima("IN001", "cacau", 1000, "g", 100, 1.20);
        MateriaPrima coco = new MateriaPrima("IN002", "coco ralado", 1000, "g", 100, 0.80);
        MateriaPrima aveia = new MateriaPrima("IN003", "aveia", 1000, "g", 100, 0.50);
        MateriaPrima[] estoque = { massa, cacau, coco, aveia };

        Produto[] bolachas = {
            new BolachaAmanteigada("B001", "chocolate"),
            new BolachaTradicional("B002", "aveia"),
            new BolachaCracker("B003", "coco")
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
                painel.exibirEstoque(estoque);
            } else if (opcao == 3) {
                reporEstoque(painel, estoque);
            }
        }

        painel.despedida();
        teclado.close();
    }

    private static void reporEstoque(Painel painel, MateriaPrima[] estoque) {
        painel.exibirEstoque(estoque);
        int escolha = painel.lerInteiro("Qual insumo vai repor", 1, estoque.length);
        MateriaPrima insumo = estoque[escolha - 1];

        double quantidade = painel.lerDouble("Quanto de " + insumo.getNome() + " em gramas", 1);
        insumo.adicionarEstoque(quantidade);

        painel.etapa(quantidade + " g de " + insumo.getNome() + " na despensa. Agora tem "
                + insumo.getQuantidade() + " " + insumo.getUnidade() + ".");
    }
}
