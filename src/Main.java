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

        Maquina estampadeira = new Maquina("Estampadeira", 500);
        Esteira esteira = new Esteira(600);
        EstacaoInspecao inspecao = new EstacaoInspecao();

        painel.exibirIntroducao();

        int opcao = 0;
        while (opcao != 4) {
            painel.exibirCatalogo(bolachas);
            painel.exibirMenu();
            opcao = painel.lerInteiro("O que vai ser", 1, 4);

            if (opcao == 1) {
                produzir(painel, bolachas, estampadeira, esteira, inspecao);
            } else if (opcao == 2) {
                painel.exibirEstoque(massas);
            } else if (opcao == 3) {
                reporEstoque(painel, massas);
            }
        }

        painel.despedida();
        teclado.close();
    }

    // A massa vem da propria bolacha, entao nao precisa passar o vetor de massas.
    private static void produzir(Painel painel, Produto[] bolachas, Maquina estampadeira,
            Esteira esteira, EstacaoInspecao inspecao) {
        int escolha = painel.lerInteiro("Qual bolacha", 1, bolachas.length);
        Produto bolacha = bolachas[escolha - 1];
        MateriaPrima massa = bolacha.getMateriaPrima();

        double demanda = painel.lerDouble("Quanto de " + massa.getNome() + " em gramas", 1);

        // Confere tudo antes de ligar qualquer coisa: fornada recusada nao pode
        // deixar equipamento ligado nem massa gasta.
        // A massa vem primeiro: sem material, nem faz sentido perguntar se cabe
        // na esteira.
        if (!massa.verificarDisponibilidade(demanda)) {
            painel.recusa("Só tem " + massa.getQuantidade() + " g de " + massa.getNome()
                    + " na despensa.");
            return;
        }
        if (!esteira.verificarCapacidade(demanda)) {
            painel.recusa("A esteira só carrega " + esteira.getCapacidadeMaxima() + " g por vez.");
            return;
        }
        if (demanda > estampadeira.getCapacidadeMaxima()) {
            painel.recusa("A " + estampadeira.getNome() + " só assa "
                    + estampadeira.getCapacidadeMaxima() + " g por fornada.");
            return;
        }

        bolacha.definirDemandaMateriaPrima(demanda);

        esteira.ligar();
        painel.etapa("Esteira ligada.");
        estampadeira.ligar();
        painel.etapa(estampadeira.getNome() + " ligada.");
        inspecao.ativar();
        painel.etapa("Inspeção ativada.");

        if (!esteira.adicionarItem(massa, demanda)) {
            painel.recusa("A esteira não aceitou a massa.");
            return;
        }
        painel.etapa(demanda + " g de " + massa.getNome() + " na esteira.");
        esteira.removerItem();
        painel.etapa("Massa entregue na " + estampadeira.getNome() + ".");

        if (!estampadeira.processar(massa, bolacha, demanda)) {
            painel.recusa("A " + estampadeira.getNome() + " não conseguiu assar.");
            return;
        }
        painel.etapa(estampadeira.getNome() + " assando " + demanda + " g...");
        painel.etapa(bolacha.getNome() + " pronta (" + bolacha.getId() + ").");

        if (!esteira.adicionarItem(bolacha, demanda)) {
            painel.recusa("A esteira não aceitou a bolacha.");
            return;
        }
        painel.etapa(bolacha.getId() + " na esteira, indo pra inspeção.");
        esteira.removerItem();

        if (!inspecao.inspecionar(bolacha)) {
            painel.recusa("A inspeção estava desativada.");
            return;
        }
        painel.etapa(bolacha.getId() + " aprovada.");

        painel.conclusao(bolacha, massa);
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
