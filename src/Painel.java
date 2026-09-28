import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class Painel {
    private static final String RISCO = "==============================================================";
    private static final String TRACO = "--------------------------------------------------------------";
    private static final String NOME_FABRICA = "NOTBISCOITO";
    private static final String SLOGAN = "\"aqui temos bolacha de verdade\"";
    private static final String DUPLA = "Pedro Magalhães e Diego Reis";

    // Cores ANSI. Terminal que nao entende mostra o texto sem cor, nada quebra.
    private static final String RESET = "\u001B[0m";
    private static final String NEGRITO = "\u001B[1m";
    private static final String VERMELHO = "\u001B[31m";
    private static final String VERDE = "\u001B[32m";
    private static final String AMARELO = "\u001B[33m";
    private static final String CIANO = "\u001B[36m";

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
        System.out.println(VERMELHO + "[x] " + explicacao + RESET);
        if (tentativa == 2) {
            System.out.println("    Segunda vez. É número.");
        } else if (tentativa == 3) {
            System.out.println("    Terceira. A massa tá esfriando.");
        } else if (tentativa >= 4) {
            System.out.println("    Você tá fazendo de propósito, né?");
        }
    }

    // ---------------------------------------------------------------
    // Abertura
    // ---------------------------------------------------------------

    public void exibirIntroducao() {
        System.out.println(CIANO + RISCO);
        System.out.println(NEGRITO + "                        " + NOME_FABRICA + RESET);
        System.out.println(CIANO + "               " + SLOGAN);
        System.out.println(RISCO + RESET);
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
        System.out.println("A linha tem estampadeira, forno e inspeção. Cada");
        System.out.println("fornada gasta um pouco de cada máquina, e máquina");
        System.out.println("gasta erra mais: corta torto, queima a bolacha, julga");
        System.out.println("errado. Abaixo de 10% de saúde ela para de vez, e só");
        System.out.println("a manutenção põe de volta na linha.");
        System.out.println();
        System.out.println("Feito por: " + DUPLA);
        System.out.println(CIANO + RISCO + RESET);
    }

    public Cenario escolherCenario() {
        Cenario[] cenarios = Cenario.values();
        System.out.println();
        System.out.println(NEGRITO + "Como está a fábrica hoje?" + RESET);
        for (int i = 0; i < cenarios.length; i++) {
            System.out.println("  " + (i + 1) + " - " + cenarios[i].getNome()
                    + String.format(" (R$ %.2f)", cenarios[i].getBudgetInicial()));
            System.out.println("      " + cenarios[i].getDescricao());
        }
        Cenario escolhido = cenarios[lerInteiro("Cenário", 1, cenarios.length) - 1];
        System.out.println();
        System.out.println("Cenário " + escolhido.getNome() + ". Você começa com R$ "
                + String.format("%.2f", escolhido.getBudgetInicial())
                + ". Massa e ingrediente saem do seu bolso, e cada máquina cobra pra rodar.");
        return escolhido;
    }

    public void exibirCatalogo(ArrayList<Produto> bolachas) {
        System.out.println();
        System.out.println("Bolachas da casa:");
        for (int i = 0; i < bolachas.size(); i++) {
            Produto bolacha = bolachas.get(i);
            System.out.println("  " + (i + 1) + " - " + bolacha.getNome()
                    + "  (" + bolacha.getQuantidadeMateriaPrimaPorUnidade()
                    + " g de massa + " + bolacha.getSabor() + ")");
        }
    }

    // ---------------------------------------------------------------
    // Menus
    // ---------------------------------------------------------------

    public void exibirMenuPrincipal(String estrategia, Cenario cenario, double budget) {
        System.out.println();
        System.out.println(CIANO + RISCO + RESET);
        System.out.println(linhaDaCaixa(NEGRITO + centralizar("[" + NOME_FABRICA + "]", 58) + RESET, 58));
        System.out.println(linhaDaCaixa("ESTRATÉGIA ATUAL: [" + estrategia + "]", 58));
        System.out.println(linhaDaCaixa("CENÁRIO ATIVO:    [" + cenario.getNome() + "]", 58));
        System.out.println(linhaDaCaixa("BUDGET ATUAL:     R$ " + String.format("%.2f", budget), 58));
        System.out.println(CIANO + RISCO + RESET);
        System.out.println("  1 - Demandas");
        System.out.println("  2 - Fabricação");
        System.out.println("  3 - Consultar armazém e despensa");
        System.out.println("  4 - Comprar matéria-prima");
        System.out.println("  5 - Trocar estratégia de produção");
        System.out.println("  6 - Auditoria e manutenção");
        System.out.println(TRACO);
        System.out.println("  0 - Fechar a fábrica");
    }

    // Mostra o submenu e devolve a opcao escolhida; 0 e sempre voltar.
    public int escolherNoSubmenu(String titulo, String[] opcoes) {
        System.out.println();
        System.out.println(TRACO);
        System.out.println(NEGRITO + "[" + titulo + "]" + RESET);
        System.out.println(TRACO);
        for (int i = 0; i < opcoes.length; i++) {
            System.out.println("  " + (i + 1) + " - " + opcoes[i]);
        }
        System.out.println("  0 - Voltar");
        return lerInteiro("Opção", 0, opcoes.length);
    }

    public Produto escolherBolacha(ArrayList<Produto> bolachas) {
        String[] nomes = new String[bolachas.size()];
        for (int i = 0; i < nomes.length; i++) {
            nomes[i] = bolachas.get(i).getNome();
        }
        int escolha = escolherNoSubmenu("QUAL BOLACHA", nomes);
        if (escolha == 0) {
            return null;
        }
        return bolachas.get(escolha - 1);
    }

    // A caixa do cabecalho tem largura fixa; o texto de dentro e completado com espacos.
    // As cores nao ocupam espaco na tela, entao nao contam na largura.
    private String linhaDaCaixa(String conteudo, int largura) {
        int visivel = conteudo.replaceAll("\u001B\\[[0-9;]*m", "").length();
        StringBuilder linha = new StringBuilder(CIANO + "| " + RESET + conteudo);
        for (int i = visivel; i < largura; i++) {
            linha.append(' ');
        }
        return linha.append(CIANO + " |" + RESET).toString();
    }

    private String centralizar(String texto, int largura) {
        int margem = Math.max(0, (largura - texto.length()) / 2);
        return " ".repeat(margem) + texto;
    }

    // ---------------------------------------------------------------
    // Consultas
    // ---------------------------------------------------------------

    public void exibirDemandas(ArrayList<Demanda> demandas, double budget) {
        System.out.println();
        if (demandas.isEmpty()) {
            System.out.println("Nenhuma demanda anotada ainda.");
            return;
        }
        System.out.println(NEGRITO + "Demandas (na ordem em que chegaram):" + RESET);
        System.out.println(String.format("  %-34s %6s  %-26s %12s  %s",
                "Bolacha", "Qtd", "Status", "Custo est.", "Cabe?"));
        for (int i = 0; i < demandas.size(); i++) {
            Demanda demanda = demandas.get(i);
            System.out.println(String.format("  %-34s %6d  %-26s %12s  %s",
                    demanda.getTipoProduto(), demanda.getQuantidadeProdutos(),
                    demanda.getStatus().getDescricao(),
                    String.format("R$ %.2f", demanda.calcularCustoEstimado()),
                    !demanda.isPendente() ? "-" : demanda.isViavel(budget) ? "sim" : "não"));
        }
    }

    public void exibirEstoque(ArrayList<MateriaPrima> insumos) {
        System.out.println();
        System.out.println(NEGRITO + "Na despensa (matéria-prima):" + RESET);
        for (int i = 0; i < insumos.size(); i++) {
            MateriaPrima insumo = insumos.get(i);
            String linha = "  " + (i + 1) + " - " + insumo.getId() + "  " + insumo.getNome()
                    + ": " + insumo.getQuantidade() + " " + insumo.getUnidade();
            if (insumo.estaAbaixoDoMinimo()) {
                linha = linha + AMARELO + "   <<< tá no fim, repõe antes que vire biscoito" + RESET;
            }
            System.out.println(linha);
        }
    }

    public void exibirBudget(double budget) {
        System.out.println();
        System.out.println("BUDGET ATUAL: R$ " + String.format("%.2f", budget));
    }

    // Uma linha por lote: cada fornada e de um tipo so, entao o lote ja agrupa.
    public void exibirArmazem(ArrayList<Produto> armazem) {
        System.out.println();
        if (armazem.isEmpty()) {
            System.out.println("Armazém vazio. Nenhuma bolacha aprovada ainda.");
            return;
        }
        Map<Integer, ArrayList<Produto>> lotes = new LinkedHashMap<>();
        for (Produto bolacha : armazem) {
            lotes.computeIfAbsent(bolacha.getLote(), lote -> new ArrayList<>()).add(bolacha);
        }

        System.out.println(NEGRITO + "Armazém (produtos acabados): " + armazem.size() + " bolachas" + RESET);
        System.out.println(String.format("  %-5s %-34s %5s %10s %12s %9s",
                "Lote", "Bolacha", "Qtd", "Qualidade", "Risco médio", "Em risco"));
        for (Map.Entry<Integer, ArrayList<Produto>> lote : lotes.entrySet()) {
            ArrayList<Produto> bolachas = lote.getValue();
            double riscoTotal = 0;
            int emRisco = 0;
            for (Produto bolacha : bolachas) {
                riscoTotal += bolacha.getProbabilidadeFalhaAcumulada();
                if (bolacha.precisaManutencao()) {
                    emRisco++;
                }
            }
            String linha = String.format("  %-5d %-34s %5d %10.2f %11.1f%% %9d",
                    lote.getKey(), bolachas.get(0).getNome(), bolachas.size(),
                    bolachas.get(0).getQualidade(), riscoTotal / bolachas.size() * 100, emRisco);
            System.out.println(emRisco > 0 ? AMARELO + linha + RESET : linha);
        }
    }

    public void exibirOficina(ArrayList<Maquina> maquinas, double[] custosReparo) {
        System.out.println();
        System.out.println(NEGRITO + "Oficina:" + RESET);
        for (int i = 0; i < maquinas.size(); i++) {
            Maquina maquina = maquinas.get(i);
            System.out.println(String.format("  %d - %-20s saúde %5.1f%%  %-24s reparo R$ %.2f",
                    i + 1, maquina.getNome(), maquina.getSaude(),
                    maquina.getEstado().getDescricao(), custosReparo[i]));
        }
        System.out.println("  0 - Voltar");
    }

    // ---------------------------------------------------------------
    // Auditoria
    // ---------------------------------------------------------------

    public void abrirAuditoria(String titulo) {
        System.out.println();
        System.out.println(CIANO + RISCO + RESET);
        System.out.println(NEGRITO + "  " + titulo + RESET);
        System.out.println(CIANO + RISCO + RESET);
    }

    public void linhaAuditoria(String diagnostico, boolean precisaIntervencao) {
        if (precisaIntervencao) {
            System.out.println(AMARELO + "  [!] " + diagnostico + RESET);
        } else {
            System.out.println(VERDE + "  [ok] " + RESET + diagnostico);
        }
    }

    public void fecharAuditoria(int total, int exibidos, int emAlerta) {
        if (total == 0) {
            System.out.println("  Nada para auditar ainda.");
        } else if (total > exibidos) {
            System.out.println("  ... e mais " + (total - exibidos) + " itens.");
        }
        System.out.println(TRACO);
        System.out.println("  " + total + " itens auditados, " + emAlerta + " pedem intervenção.");
    }

    // ---------------------------------------------------------------
    // Mensagens
    // ---------------------------------------------------------------

    public void etapa(String mensagem) {
        System.out.println(VERDE + "[ok] " + RESET + mensagem);
    }

    public void aviso(String mensagem) {
        System.out.println(AMARELO + "[!] " + mensagem + RESET);
    }

    public void alerta(String mensagem) {
        System.out.println(VERMELHO + NEGRITO + "[!!] " + mensagem + RESET);
    }

    public void recusa(String motivo) {
        System.out.println(VERMELHO + "[x] " + motivo + RESET);
        System.out.println("    Fornada cancelada, nada foi gasto.");
    }

    public void despedida() {
        System.out.println();
        System.out.println("Fábrica fechada. Até amanhã.");
    }
}
