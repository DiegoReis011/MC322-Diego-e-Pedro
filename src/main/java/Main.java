import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Ponto de entrada da NotBiscoito: monta a fábrica e roda o menu.
 *
 * <p>É aqui que os erros de regra de negócio são tratados. Cada operação do
 * menu captura as exceções que ela pode receber, mostra a tela de erro com o
 * motivo e com o que aconteceu com a fábrica, e devolve o operador ao menu.
 * Nenhum erro de operação fecha o programa. A única coisa que fecha antes do
 * 0 é o teclado ser fechado, porque aí não tem mais como perguntar nada.</p>
 *
 * @author Pedro Magalhães
 */
public class Main {

    private static final String NAO_PRODUZIU = "Não foi possível iniciar a produção:";

    // Main so tem o ponto de entrada; ninguem cria um Main.
    private Main() {
    }

    /**
     * Abre a fábrica: escolhe o cenário, monta a linha e roda o menu até o
     * operador escolher 0 ou o teclado ser fechado.
     *
     * @param args não usado
     */
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        Painel painel = new Painel(teclado);

        painel.exibirIntroducao();
        try {
            Cenario cenario = painel.escolherCenario();

            // O unico lugar que conhece as estrategias concretas. O gerenciador
            // so recebe a interface.
            EstrategiaProducao[] estrategias = {
                new EstrategiaFilaDoForno(),
                new EstrategiaLoteFesta(),
                new EstrategiaCestaCheia()
            };

            GerenciadorProducao gerenciador = montarFabrica(painel, cenario, estrategias[0]);
            painel.exibirCatalogo(gerenciador.getCatalogo());
            operar(painel, gerenciador, estrategias);

            // Fechamento do caixa antes de apagar as luzes.
            gerenciador.exibirBudget();
        } catch (NoSuchElementException erro) {
            // Sem teclado nao da pra perguntar nada: fecha a fabrica em vez de estourar.
            painel.aviso("O teclado foi fechado, então não dá pra continuar. Fechando a fábrica.");
        }
        painel.despedida();
        teclado.close();
    }

    private static GerenciadorProducao montarFabrica(Painel painel, Cenario cenario,
            EstrategiaProducao estrategiaInicial) {
        // A massa e comum a todas: o tipo da bolacha decide quanta massa vai.
        MateriaPrima massa = new MateriaPrima("MP001", "massa da casa", 5000, "g", 200, 0.30);

        GerenciadorProducao gerenciador =
                new GerenciadorProducao(massa, cenario, estrategiaInicial, painel);

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

        return gerenciador;
    }

    // Opcao fora da faixa ou texto no lugar de numero o painel ja resolve:
    // ele so devolve opcao valida.
    private static void operar(Painel painel, GerenciadorProducao gerenciador,
            EstrategiaProducao[] estrategias) {
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
        try {
            gerenciador.registrarDemanda(bolacha.getNome(), quantidade);
            painel.etapa("Demanda anotada: " + quantidade + " x " + bolacha.getNome() + ".");
        } catch (DemandaInvalidaException erro) {
            painel.exibirErro("Não foi possível anotar a demanda:", erro.getMessage(),
                    "As demandas continuam como estavam.");
        }
    }

    // Cada erro deixa a fabrica num estado diferente, entao cada um tem a sua
    // explicacao do que ficou.
    private static void menuFabricacao(Painel painel, GerenciadorProducao gerenciador) {
        int opcao = painel.escolherNoSubmenu("FABRICAÇÃO", new String[] {
            "Processar próxima demanda (estratégia " + gerenciador.getEstrategiaAtual().getNomeEstrategia() + ")",
            "Fabricar bolacha específica"
        });
        Produto bolacha = null;
        if (opcao == 2) {
            bolacha = painel.escolherBolacha(gerenciador.getCatalogo());
            if (bolacha == null) {
                return;
            }
        } else if (opcao == 0) {
            return;
        }

        try {
            if (opcao == 1) {
                gerenciador.executarProximaProducao();
            } else {
                gerenciador.fabricarDemanda(bolacha);
            }
        } catch (RecursoInsuficienteException erro) {
            painel.exibirErro(NAO_PRODUZIU, erro.getMessage(),
                    "Nenhuma massa, ingrediente ou budget foi gasto. A demanda foi cancelada:"
                    + " compre o que falta e atualize a demanda para reabrir.");
        } catch (OrcamentoInsuficienteException erro) {
            painel.exibirErro(NAO_PRODUZIU, erro.getMessage(),
                    "Nenhuma massa, ingrediente ou budget foi gasto. A demanda foi cancelada:"
                    + " atualize a demanda com uma quantidade que caiba no budget.");
        } catch (MaquinaIndisponivelException erro) {
            painel.exibirErro(NAO_PRODUZIU, erro.getMessage(),
                    "Nada foi gasto e a demanda continua na fila. Depois do reparo, é só fabricar de novo.");
        } catch (DemandaInvalidaException erro) {
            painel.exibirErro(NAO_PRODUZIU, erro.getMessage(),
                    "Nada foi gasto e nenhuma demanda mudou.");
        }
    }

    private static void menuConsultar(Painel painel, GerenciadorProducao gerenciador) {
        int opcao = painel.escolherNoSubmenu("CONSULTAR", new String[] {
            "Ver armazém (bolachas prontas)",
            "Ver despensa (matéria-prima)",
            "Ver a linha de produção"
        });
        if (opcao == 1) {
            gerenciador.exibirArmazem();
        } else if (opcao == 2) {
            painel.exibirEstoque(gerenciador.getInsumos());
        } else if (opcao == 3) {
            gerenciador.exibirLinha();
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

        try {
            gerenciador.comprarMateriaPrima(insumo, quantidade);
            painel.etapa("Comprou " + quantidade + " g de " + insumo.getNome()
                    + " por R$ " + String.format("%.2f", custo) + ".");
        } catch (OrcamentoInsuficienteException erro) {
            painel.exibirErro("Não foi possível comprar " + insumo.getNome() + ":", erro.getMessage(),
                    "Nada foi gasto e a despensa continua igual. Tente uma quantidade menor.");
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
            "Estrutura da linha (máquinas e peças)",
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
        List<Maquina> maquinas = gerenciador.getMaquinas();
        double[] custos = new double[maquinas.size()];
        for (int i = 0; i < custos.length; i++) {
            custos[i] = gerenciador.calcularCustoReparo(maquinas.get(i));
        }
        painel.exibirOficina(maquinas, custos);
        int escolha = painel.lerInteiro("Qual máquina reparar", 0, maquinas.size());
        if (escolha == 0) {
            return;
        }
        Maquina maquina = maquinas.get(escolha - 1);
        try {
            gerenciador.repararMaquina(maquina);
        } catch (OrcamentoInsuficienteException erro) {
            painel.exibirErro("Não foi possível reparar " + maquina.getNome() + ":", erro.getMessage(),
                    "Nada foi cobrado e a máquina continua como estava.");
        }
    }
}
