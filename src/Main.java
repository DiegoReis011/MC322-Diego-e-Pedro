import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        Painel painel = new Painel(teclado);

        painel.exibirIntroducao();
        Cenario cenario = painel.escolherCenario();

        // O unico lugar que conhece as estrategias concretas. O gerenciador
        // so recebe a interface.
        EstrategiaProducao[] estrategias = {
            new EstrategiaFilaDoForno(),
            new EstrategiaLoteFesta(),
            new EstrategiaCestaCheia()
        };

        // A massa e comum a todas: o tipo da bolacha decide quanta massa vai.
        MateriaPrima massa = new MateriaPrima("MP001", "massa da casa", 5000, "g", 200, 0.30);

        GerenciadorProducao gerenciador =
                new GerenciadorProducao(massa, cenario, estrategias[0], painel);

        // O nome tem que ser igual ao sabor da bolacha: e por ele que a
        // linha acha o insumo.
        gerenciador.adicionarIngrediente(ingrediente("IN001", "chocolate", 1.20));
        gerenciador.adicionarIngrediente(ingrediente("IN002", "coco", 0.80));
        gerenciador.adicionarIngrediente(ingrediente("IN003", "aveia", 0.50));

        // A ordem aqui e a ordem em que a bolacha passa pela linha.
        gerenciador.adicionarMaquina(new Estampadeira());
        gerenciador.adicionarMaquina(new Forno());
        gerenciador.adicionarMaquina(new EstacaoInspecao());

        gerenciador.adicionarAoCatalogo(new BolachaAmanteigada("B001", "chocolate"));
        gerenciador.adicionarAoCatalogo(new BolachaTradicional("B002", "aveia"));
        gerenciador.adicionarAoCatalogo(new BolachaCracker("B003", "coco"));

        painel.exibirCatalogo(gerenciador.getCatalogo());

        int opcao = -1;
        while (opcao != 0) {
            painel.exibirMenuPrincipal(gerenciador.getEstrategiaAtual().getNomeEstrategia(),
                    gerenciador.getCenario(), gerenciador.getBudget());
            opcao = painel.lerInteiro("ESCOLHA", 0, 6);

            switch (opcao) {
                case 1 -> menuDemandas(painel, gerenciador);
                case 2 -> menuFabricacao(painel, gerenciador);
                case 3 -> menuConsultar(painel, gerenciador);
                case 4 -> comprarInsumo(painel, gerenciador);
                case 5 -> menuEstrategia(painel, gerenciador, estrategias);
                case 6 -> menuAuditoria(painel, gerenciador);
                default -> { }
            }
        }

        // Fechamento do caixa antes de apagar as luzes.
        gerenciador.exibirBudget();
        painel.despedida();
        teclado.close();
    }

    // Ingrediente de sabor so difere no nome e no preco.
    private static MateriaPrima ingrediente(String id, String nome, double custoPorGrama) {
        return new MateriaPrima(id, nome, 1000, "g", 100, custoPorGrama);
    }

    private static void menuDemandas(Painel painel, GerenciadorProducao gerenciador) {
        int opcao = painel.escolherNoSubmenu("DEMANDAS", new String[] {
            "Atualizar demanda",
            "Listar demandas"
        });
        if (opcao == 1) {
            atualizarDemanda(painel, gerenciador);
        } else if (opcao == 2) {
            painel.exibirDemandas(gerenciador.getDemandas(), gerenciador.getBudget());
        }
    }

    private static void atualizarDemanda(Painel painel, GerenciadorProducao gerenciador) {
        Produto bolacha = painel.escolherBolacha(gerenciador.getCatalogo());
        if (bolacha == null) {
            return;
        }
        int quantidade = painel.lerInteiro("Quantas " + bolacha.getNome(), 1, 1000);
        gerenciador.registrarDemanda(bolacha.getNome(), quantidade);
        painel.etapa("Demanda anotada: " + quantidade + " x " + bolacha.getNome() + ".");
    }

    private static void menuFabricacao(Painel painel, GerenciadorProducao gerenciador) {
        int opcao = painel.escolherNoSubmenu("FABRICAÇÃO", new String[] {
            "Processar próxima demanda (estratégia " + gerenciador.getEstrategiaAtual().getNomeEstrategia() + ")",
            "Fabricar bolacha específica"
        });
        if (opcao == 1) {
            gerenciador.executarProximaProducao();
        } else if (opcao == 2) {
            Produto bolacha = painel.escolherBolacha(gerenciador.getCatalogo());
            if (bolacha != null) {
                gerenciador.fabricarDemanda(bolacha);
            }
        }
    }

    private static void menuConsultar(Painel painel, GerenciadorProducao gerenciador) {
        int opcao = painel.escolherNoSubmenu("CONSULTAR", new String[] {
            "Ver armazém (bolachas prontas)",
            "Ver despensa (matéria-prima)"
        });
        if (opcao == 1) {
            gerenciador.exibirArmazem();
        } else if (opcao == 2) {
            painel.exibirEstoque(gerenciador.getInsumos());
        }
    }

    private static void comprarInsumo(Painel painel, GerenciadorProducao gerenciador) {
        ArrayList<MateriaPrima> insumos = gerenciador.getInsumos();
        painel.exibirEstoque(insumos);

        int escolha = painel.lerInteiro("Qual insumo vai comprar (0 volta)", 0, insumos.size());
        if (escolha == 0) {
            return;
        }
        MateriaPrima insumo = insumos.get(escolha - 1);

        double quantidade = painel.lerDouble("Quanto de " + insumo.getNome() + " em gramas", 1);
        double custo = gerenciador.calcularCustoCompra(insumo, quantidade);

        if (gerenciador.comprarMateriaPrima(insumo, quantidade)) {
            painel.etapa("Comprou " + quantidade + " g de " + insumo.getNome()
                    + " por R$ " + String.format("%.2f", custo) + ".");
        } else {
            painel.aviso("A compra custa R$ " + String.format("%.2f", custo)
                    + " e o budget não cobre.");
        }
    }

    private static void menuEstrategia(Painel painel, GerenciadorProducao gerenciador,
            EstrategiaProducao[] estrategias) {
        String[] nomes = new String[estrategias.length];
        for (int i = 0; i < estrategias.length; i++) {
            nomes[i] = estrategias[i].getNomeEstrategia();
        }
        int opcao = painel.escolherNoSubmenu("ESTRATÉGIA DE PRODUÇÃO", nomes);
        if (opcao > 0) {
            gerenciador.setEstrategia(estrategias[opcao - 1]);
            painel.etapa("A linha agora segue a estratégia " + nomes[opcao - 1] + ".");
        }
    }

    private static void menuAuditoria(Painel painel, GerenciadorProducao gerenciador) {
        int opcao = painel.escolherNoSubmenu("AUDITORIA E MANUTENÇÃO", new String[] {
            "Relatório geral",
            "Detalhar máquinas",
            "Detalhar bolachas",
            "Chamar a manutenção"
        });
        switch (opcao) {
            case 1 -> gerenciador.gerarAuditoriaGeral();
            case 2 -> gerenciador.auditarMaquinas();
            case 3 -> gerenciador.auditarProdutos();
            case 4 -> chamarManutencao(painel, gerenciador);
            default -> { }
        }
    }

    private static void chamarManutencao(Painel painel, GerenciadorProducao gerenciador) {
        ArrayList<Maquina> maquinas = gerenciador.getMaquinas();
        double[] custos = new double[maquinas.size()];
        for (int i = 0; i < custos.length; i++) {
            custos[i] = gerenciador.calcularCustoReparo(maquinas.get(i));
        }
        painel.exibirOficina(maquinas, custos);
        int escolha = painel.lerInteiro("Qual máquina reparar", 0, maquinas.size());
        if (escolha > 0) {
            gerenciador.repararMaquina(maquinas.get(escolha - 1));
        }
    }
}
