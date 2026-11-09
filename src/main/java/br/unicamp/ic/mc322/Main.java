package br.unicamp.ic.mc322;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import br.unicamp.ic.mc322.exception.DemandaInvalidaException;
import br.unicamp.ic.mc322.exception.MaquinaIndisponivelException;
import br.unicamp.ic.mc322.exception.OrcamentoInsuficienteException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.RecursoInsuficienteException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.persistence.PersistenciaPlanta;
import br.unicamp.ic.mc322.service.FabricaNotBiscoito;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaCestaCheia;
import br.unicamp.ic.mc322.strategy.EstrategiaFilaDoForno;
import br.unicamp.ic.mc322.strategy.EstrategiaLoteFesta;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;
import br.unicamp.ic.mc322.ui.Painel;

/**
 * Ponto de entrada da NotBiscoito: monta a fábrica e roda o menu.
 *
 * <p>Ao abrir, se existir uma planta salva ({@value #ARQUIVO_PLANTA}), oferece
 * carregá-la; sem arquivo (primeira execução) a fábrica nasce com os dados
 * padrão. Os itens 7 e 8 do menu salvam e carregam a planta em XML.</p>
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
    private static final String ARQUIVO_PLANTA = "dados/planta.xml";

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
            // O unico lugar que conhece as estrategias concretas. O gerenciador
            // so recebe a interface.
            EstrategiaProducao[] estrategias = {
                new EstrategiaFilaDoForno(),
                new EstrategiaLoteFesta(),
                new EstrategiaCestaCheia()
            };
            PersistenciaPlanta persistencia = new PersistenciaPlanta(Path.of(ARQUIVO_PLANTA));

            GerenciadorProducao gerenciador = abrirFabrica(painel, persistencia, estrategias[0]);
            painel.exibirCatalogo(gerenciador.getCatalogo());
            operar(painel, gerenciador, estrategias, persistencia);

            // Fechamento do caixa antes de apagar as luzes.
            gerenciador.exibirBudget();
        } catch (NoSuchElementException erro) {
            // Sem teclado nao da pra perguntar nada: fecha a fabrica em vez de estourar.
            painel.aviso("O teclado foi fechado, então não dá pra continuar. Fechando a fábrica.");
        }
        painel.despedida();
        teclado.close();
    }

    // Sem arquivo (primeira execucao) a fabrica nasce com os dados padrao. Com
    // arquivo, o operador escolhe; se o carregamento falhar, a fabrica nova
    // abre do mesmo jeito e o arquivo antigo fica intocado.
    private static GerenciadorProducao abrirFabrica(Painel painel, PersistenciaPlanta persistencia,
            EstrategiaProducao estrategiaInicial) {
        if (persistencia.existe()) {
            painel.aviso("Encontrei uma planta salva em " + persistencia.getArquivo() + ".");
            int escolha = painel.lerInteiro("1 = continuar a planta salva, 2 = fábrica nova", 1, 2);
            if (escolha == 1) {
                try {
                    // O cenario inicial e so um ponto de partida: o estado salvo o substitui.
                    GerenciadorProducao salva = FabricaNotBiscoito.criarGerenciador(painel,
                            Cenario.IDEAL, estrategiaInicial);
                    salva.restaurarEstado(persistencia.carregar());
                    painel.etapa("Planta carregada. A fábrica continua de onde parou.");
                    return salva;
                } catch (PersistenciaException erro) {
                    painel.exibirErro("Não foi possível carregar a planta salva:", erro.getMessage(),
                            "O arquivo não foi alterado. A fábrica abre nova, com os dados padrão.");
                }
            }
        }
        Cenario cenario = painel.escolherCenario();
        return FabricaNotBiscoito.criarGerenciador(painel, cenario, estrategiaInicial);
    }

    // Opcao fora da faixa ou texto no lugar de numero o painel ja resolve:
    // ele so devolve opcao valida.
    private static void operar(Painel painel, GerenciadorProducao gerenciador,
            EstrategiaProducao[] estrategias, PersistenciaPlanta persistencia) {
        int opcao = -1;
        while (opcao != 0) {
            painel.exibirMenuPrincipal(gerenciador.getEstrategiaAtual().getNomeEstrategia(),
                    gerenciador.getCenario(), gerenciador.getBudget());
            opcao = painel.lerInteiro("ESCOLHA", 0, 8);

            switch (opcao) {
                case 1 -> menuDemandas(painel, gerenciador);
                case 2 -> menuFabricacao(painel, gerenciador);
                case 3 -> menuConsultar(painel, gerenciador);
                case 4 -> comprarInsumo(painel, gerenciador);
                case 5 -> menuEstrategia(painel, gerenciador, estrategias);
                case 6 -> menuAuditoria(painel, gerenciador);
                case 7 -> salvarPlanta(painel, gerenciador, persistencia);
                case 8 -> carregarPlanta(painel, gerenciador, persistencia);
                default -> { }
            }
        }
    }

    private static void salvarPlanta(Painel painel, GerenciadorProducao gerenciador,
            PersistenciaPlanta persistencia) {
        try {
            persistencia.salvar(gerenciador.exportarEstado());
            painel.etapa("Planta salva em " + persistencia.getArquivo() + ".");
        } catch (PersistenciaException erro) {
            painel.exibirErro("Não foi possível salvar a planta:", erro.getMessage(),
                    "A fábrica continua como estava e o save anterior, se havia, não foi alterado.");
        }
    }

    // O estado so e trocado depois que o arquivo inteiro foi lido e validado.
    private static void carregarPlanta(Painel painel, GerenciadorProducao gerenciador,
            PersistenciaPlanta persistencia) {
        try {
            gerenciador.restaurarEstado(persistencia.carregar());
            painel.etapa("Planta carregada de " + persistencia.getArquivo()
                    + ". O estado anterior foi substituído.");
        } catch (PersistenciaException erro) {
            painel.exibirErro("Não foi possível carregar a planta:", erro.getMessage(),
                    "A fábrica continua exatamente como estava.");
        }
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
