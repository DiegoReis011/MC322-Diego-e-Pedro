import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        Painel painel = new Painel(teclado);

        // A massa e comum a todas: o tipo da bolacha decide quanta massa vai.
        MateriaPrima massa = new MateriaPrima("MP001", "massa da casa", 5000, "g", 200, 0.30);

        // Por enquanto fixo no Ideal e na Fila do Forno; a escolha entra no menu depois.
        GerenciadorProducao gerenciador = new GerenciadorProducao(massa, Cenario.IDEAL,
                new EstrategiaFilaDoForno(), painel);

        // O nome tem que ser igual ao sabor da bolacha: e por ele que a
        // linha acha o insumo.
        gerenciador.adicionarIngrediente(ingrediente("IN001", "chocolate", 1.20));
        gerenciador.adicionarIngrediente(ingrediente("IN002", "coco", 0.80));
        gerenciador.adicionarIngrediente(ingrediente("IN003", "aveia", 0.50));

        // A ordem aqui e a ordem em que a bolacha passa pela linha.
        gerenciador.adicionarMaquina(new Estampadeira());
        gerenciador.adicionarMaquina(new Forno());
        gerenciador.adicionarMaquina(new EstacaoInspecao());

        Produto[] bolachas = {
            new BolachaAmanteigada("B001", "chocolate"),
            new BolachaTradicional("B002", "aveia"),
            new BolachaCracker("B003", "coco")
        };

        for (Produto bolacha : bolachas) {
            gerenciador.adicionarAoCatalogo(bolacha);
        }

        painel.exibirIntroducao(Cenario.IDEAL.getBudgetInicial());
        painel.exibirCatalogo(bolachas);

        int opcao = -1;
        while (opcao != 0) {
            gerenciador.exibirBudget();
            painel.exibirMenu(bolachas);
            opcao = painel.lerInteiro("ESCOLHA", 0, 9);

            if (opcao >= 1 && opcao <= 3) {
                atualizarDemanda(painel, gerenciador, bolachas[opcao - 1]);
            } else if (opcao >= 4 && opcao <= 6) {
                gerenciador.fabricarDemanda(bolachas[opcao - 4]);
            } else if (opcao == 7) {
                gerenciador.exibirArmazem();
            } else if (opcao == 8) {
                painel.exibirEstoque(gerenciador.getInsumos());
            } else if (opcao == 9) {
                comprarInsumo(painel, gerenciador);
            }
        }

        painel.despedida();
        teclado.close();
    }

    // Ingrediente de sabor so difere no nome e no preco.
    private static MateriaPrima ingrediente(String id, String nome, double custoPorGrama) {
        return new MateriaPrima(id, nome, 1000, "g", 100, custoPorGrama);
    }

    private static void atualizarDemanda(Painel painel, GerenciadorProducao gerenciador,
            Produto bolacha) {
        int quantidade = painel.lerInteiro("Quantas " + bolacha.getNome(), 1, 1000);
        gerenciador.registrarDemanda(bolacha.getNome(), quantidade);
        painel.etapa("Demanda anotada: " + quantidade + " x " + bolacha.getNome() + ".");
    }

    private static void comprarInsumo(Painel painel, GerenciadorProducao gerenciador) {
        ArrayList<MateriaPrima> insumos = gerenciador.getInsumos();
        painel.exibirEstoque(insumos);

        int escolha = painel.lerInteiro("Qual insumo vai comprar", 1, insumos.size());
        MateriaPrima insumo = insumos.get(escolha - 1);

        double quantidade = painel.lerDouble("Quanto de " + insumo.getNome() + " em gramas", 1);
        double custo = gerenciador.calcularCustoCompra(insumo, quantidade);

        if (gerenciador.comprarMateriaPrima(insumo, quantidade)) {
            painel.etapa("Comprou " + quantidade + " g de " + insumo.getNome()
                    + " por R$ " + String.format("%.2f", custo) + ".");
        } else {
            painel.recusa("A compra custa R$ " + String.format("%.2f", custo)
                    + " e o budget nao cobre.");
        }
    }
}
