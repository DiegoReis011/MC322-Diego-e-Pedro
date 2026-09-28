import java.util.ArrayList;
import java.util.List;

public class GerenciadorProducao {
    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private ArrayList<Maquina> maquinas;

    // Um molde de cada bolacha da casa. E por ele que a demanda escolhida pela
    // estrategia volta a ser uma bolacha de verdade.
    private ArrayList<Produto> catalogo;

    // A massa e a materia-prima da linha, comum a toda bolacha. Os
    // ingredientes entram por cima dela: o sabor do produto decide qual.
    private MateriaPrima materiaPrima;
    private ArrayList<MateriaPrima> ingredientes;

    // Fracao da massa que vira ingrediente de sabor.
    private static final double PROPORCAO_INGREDIENTE = 0.20;
    // Quanto a manutencao cobra por ponto de saude recuperado.
    private static final double CUSTO_REPARO_POR_PONTO = 3.0;
    // Linhas por relatorio de auditoria, pra lista de bolachas nao rolar a tela inteira.
    private static final int LIMITE_AUDITORIA = 20;

    private double budget;
    private Cenario cenario;
    private int contadorLotes;

    // So a interface: o gerenciador nao sabe qual estrategia concreta esta usando.
    private EstrategiaProducao estrategiaAtual;

    // O gerenciador nao imprime nada por conta propria. Quem fala com o
    // operador continua sendo o painel.
    private Painel painel;

    public GerenciadorProducao(MateriaPrima materiaPrima, Cenario cenario,
            EstrategiaProducao estrategiaInicial, Painel painel) {
        this.materiaPrima = materiaPrima;
        this.cenario = cenario;
        this.budget = cenario.getBudgetInicial();
        this.estrategiaAtual = estrategiaInicial;
        this.painel = painel;
        this.demandas = new ArrayList<>();
        this.produtosFabricados = new ArrayList<>();
        this.maquinas = new ArrayList<>();
        this.catalogo = new ArrayList<>();
        this.ingredientes = new ArrayList<>();
        this.contadorLotes = 0;
    }

    public void adicionarMaquina(Maquina maquina) {
        maquina.aplicarCenario(cenario);
        maquinas.add(maquina);
    }

    public void adicionarIngrediente(MateriaPrima ingrediente) {
        ingredientes.add(ingrediente);
    }

    public void adicionarAoCatalogo(Produto molde) {
        catalogo.add(molde);
    }

    public ArrayList<Produto> getCatalogo() {
        return catalogo;
    }

    // ---------------------------------------------------------------
    // Estrategia
    // ---------------------------------------------------------------

    public void setEstrategia(EstrategiaProducao novaEstrategia) {
        if (novaEstrategia != null) {
            estrategiaAtual = novaEstrategia;
        }
    }

    public EstrategiaProducao getEstrategiaAtual() {
        return estrategiaAtual;
    }

    // A estrategia so aponta a demanda; quem gasta e fabrica continua sendo o gerenciador.
    public boolean executarProximaProducao() {
        Demanda escolhida = estrategiaAtual.selecionarDemanda(demandas, budget);
        if (escolhida == null) {
            painel.aviso("A estratégia " + estrategiaAtual.getNomeEstrategia()
                    + " não achou nenhuma demanda elegível.");
            return false;
        }
        painel.etapa("A estratégia " + estrategiaAtual.getNomeEstrategia() + " escolheu "
                + escolhida.getQuantidadeProdutos() + " x " + escolhida.getTipoProduto() + ".");
        return fabricar(buscarMolde(escolhida.getTipoProduto()), escolhida);
    }

    // ---------------------------------------------------------------
    // Demandas
    // ---------------------------------------------------------------

    // Registrar de novo mexe na demanda que ja existe: uma por bolacha.
    public void registrarDemanda(String tipoProduto, int quantidade) {
        Demanda existente = buscarDemanda(tipoProduto);
        if (existente == null) {
            Demanda nova = new Demanda(tipoProduto, quantidade);
            // Insumo ja foi pago na compra: a fornada so cobra as maquinas.
            nova.setCustoUnitarioEstimado(calcularCustoProducao(1));
            demandas.add(nova);
        } else {
            existente.atualizarQuantidade(quantidade);
        }
    }

    public boolean atualizarDemanda(String tipoProduto, int quantidade) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null) {
            return false;
        }
        demanda.atualizarQuantidade(quantidade);
        return true;
    }

    public ArrayList<Demanda> getDemandas() {
        return demandas;
    }

    private Demanda buscarDemanda(String tipoProduto) {
        for (int i = 0; i < demandas.size(); i++) {
            Demanda demanda = demandas.get(i);
            if (demanda.getTipoProduto().equals(tipoProduto)) {
                return demanda;
            }
        }
        return null;
    }

    private Produto buscarMolde(String nome) {
        for (int i = 0; i < catalogo.size(); i++) {
            if (catalogo.get(i).getNome().equals(nome)) {
                return catalogo.get(i);
            }
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Materia-prima
    // ---------------------------------------------------------------

    public boolean comprarMateriaPrima(MateriaPrima insumo, double quantidade) {
        if (quantidade <= 0) {
            return false;
        }
        double custo = insumo.getCustoPorUnidade() * quantidade;
        if (custo > budget) {
            return false;
        }
        budget -= custo;
        insumo.adicionarEstoque(quantidade);
        return true;
    }

    public double calcularCustoCompra(MateriaPrima insumo, double quantidade) {
        return insumo.getCustoPorUnidade() * quantidade;
    }

    // Na ordem em que o menu de compra numera os insumos.
    public ArrayList<MateriaPrima> getInsumos() {
        ArrayList<MateriaPrima> todos = new ArrayList<>();
        todos.add(materiaPrima);
        for (int i = 0; i < ingredientes.size(); i++) {
            todos.add(ingredientes.get(i));
        }
        return todos;
    }

    public void exibirBudget() {
        painel.exibirBudget(budget);
    }

    // ---------------------------------------------------------------
    // Fabricacao
    // ---------------------------------------------------------------

    // Fabricacao manual: o operador escolhe a bolacha, sem passar pela estrategia.
    public boolean fabricarDemanda(Produto molde) {
        Demanda demanda = buscarDemanda(molde.getNome());
        if (demanda == null) {
            painel.recusa("Não tem demanda registrada de " + molde.getNome() + ".");
            return false;
        }
        if (!demanda.isPendente()) {
            painel.recusa("A demanda de " + molde.getNome() + " não está na fila ("
                    + demanda.getStatus().getDescricao() + "). Atualize a demanda para reabrir.");
            return false;
        }
        return fabricar(molde, demanda);
    }

    private boolean fabricar(Produto molde, Demanda demanda) {
        Maquina quebrada = buscarMaquinaQuebrada();
        if (quebrada != null) {
            // Maquina quebrada nao e falta de budget nem de insumo: a demanda segue na fila.
            painel.recusa("A linha parou: " + quebrada.getNome() + " quebrou. Chame a manutenção na Auditoria.");
            return false;
        }

        int quantidade = demanda.getQuantidadeProdutos();
        double massaNecessaria = demanda.calcularMateriaPrimaNecessaria(
                molde.getQuantidadeMateriaPrimaPorUnidade());
        double ingredienteNecessario = massaNecessaria * PROPORCAO_INGREDIENTE;
        MateriaPrima ingrediente = buscarIngrediente(molde.getSabor());
        double custo = calcularCustoProducao(quantidade);

        // Confere tudo antes de gastar qualquer coisa: fornada recusada nao
        // pode deixar massa gasta nem budget debitado.
        if (ingrediente == null) {
            return cancelarPorFalta(demanda, "A despensa não tem " + molde.getSabor() + ".");
        }
        if (!materiaPrima.verificarDisponibilidade(massaNecessaria)) {
            return cancelarPorFalta(demanda, "Faltou massa: precisa de " + massaNecessaria
                    + " g e só tem " + materiaPrima.getQuantidade() + " g.");
        }
        if (!ingrediente.verificarDisponibilidade(ingredienteNecessario)) {
            return cancelarPorFalta(demanda, "Faltou " + ingrediente.getNome() + ": precisa de "
                    + ingredienteNecessario + " g e só tem " + ingrediente.getQuantidade() + " g.");
        }
        if (custo > budget) {
            return cancelarPorFalta(demanda, "As máquinas cobram R$ " + String.format("%.2f", custo)
                    + " e o budget tem R$ " + String.format("%.2f", budget) + ".");
        }

        demanda.iniciarProducao();
        materiaPrima.consumir(massaNecessaria);
        ingrediente.consumir(ingredienteNecessario);
        budget -= custo;
        int lote = ++contadorLotes;

        painel.etapa("Lote " + lote + ": " + quantidade + " x " + molde.getNome() + " na linha.");
        painel.etapa("Gastou " + massaNecessaria + " g de massa e "
                + ingredienteNecessario + " g de " + ingrediente.getNome() + ".");

        ligarLinha();
        int aprovadas = 0;
        for (int i = 0; i < quantidade; i++) {
            Produto bolacha = molde.criarCopia(molde.getId() + "-L" + lote + "-" + (i + 1));
            bolacha.setLote(lote);
            bolacha.processar();
            for (int m = 0; m < maquinas.size(); m++) {
                maquinas.get(m).processar(bolacha);
            }
            if (bolacha.getStatus() == StatusBolacha.APROVADA) {
                produtosFabricados.add(bolacha);
                aprovadas++;
            }
        }
        desligarLinha();
        desgastarLinha();

        demanda.atender();
        painel.etapa(aprovadas + " aprovadas, " + (quantidade - aprovadas) + " no refugo.");
        return true;
    }

    // Falta de budget ou insumo cancela a demanda, como o enunciado define.
    // Sem isso a Fila do Forno escolheria a mesma demanda impossivel pra sempre.
    private boolean cancelarPorFalta(Demanda demanda, String motivo) {
        demanda.cancelar();
        painel.recusa(motivo);
        painel.aviso("Demanda de " + demanda.getTipoProduto() + " agora está: "
                + demanda.getStatus().getDescricao() + ". Atualize a demanda para reabrir.");
        return false;
    }

    // Uma fornada = um ciclo de uso pra cada maquina da linha.
    private void desgastarLinha() {
        for (int i = 0; i < maquinas.size(); i++) {
            Maquina maquina = maquinas.get(i);
            maquina.registrarCiclo();
            if (maquina.estaQuebrada()) {
                painel.alerta(maquina.getNome() + " soltou fumaça e parou. A linha só volta depois da manutenção.");
            } else if (maquina.precisaManutencao()) {
                painel.alerta(maquina.getNome() + " está rangendo: saúde em "
                        + String.format("%.1f", maquina.getSaude()) + "%.");
            }
        }
    }

    private double calcularCustoProducao(int quantidade) {
        double porBolacha = 0;
        for (int i = 0; i < maquinas.size(); i++) {
            porBolacha += maquinas.get(i).getCustoOperacional();
        }
        return porBolacha * quantidade;
    }

    // O sabor da bolacha e o nome do ingrediente que ela consome.
    private MateriaPrima buscarIngrediente(String sabor) {
        for (int i = 0; i < ingredientes.size(); i++) {
            if (ingredientes.get(i).getNome().equals(sabor)) {
                return ingredientes.get(i);
            }
        }
        return null;
    }

    private Maquina buscarMaquinaQuebrada() {
        for (int i = 0; i < maquinas.size(); i++) {
            if (maquinas.get(i).estaQuebrada()) {
                return maquinas.get(i);
            }
        }
        return null;
    }

    private void ligarLinha() {
        for (int i = 0; i < maquinas.size(); i++) {
            maquinas.get(i).ligar();
        }
    }

    private void desligarLinha() {
        for (int i = 0; i < maquinas.size(); i++) {
            maquinas.get(i).desligar();
        }
    }

    // ---------------------------------------------------------------
    // Manutencao
    // ---------------------------------------------------------------

    public ArrayList<Maquina> getMaquinas() {
        return maquinas;
    }

    public double calcularCustoReparo(Maquina maquina) {
        return (100.0 - maquina.getSaude()) * CUSTO_REPARO_POR_PONTO;
    }

    public boolean repararMaquina(Maquina maquina) {
        double custo = calcularCustoReparo(maquina);
        if (custo <= 0) {
            painel.aviso(maquina.getNome() + " já está nova em folha.");
            return false;
        }
        if (custo > budget) {
            painel.aviso("O reparo de " + maquina.getNome() + " custa R$ "
                    + String.format("%.2f", custo) + " e o budget não cobre.");
            return false;
        }
        budget -= custo;
        maquina.reparar();
        painel.etapa("Manutenção feita: " + maquina.getNome() + " de volta a 100% por R$ " + String.format("%.2f", custo) + ".");
        return true;
    }

    // ---------------------------------------------------------------
    // Auditoria e armazem
    // ---------------------------------------------------------------

    public void gerarAuditoriaGeral() {
        List<Auditavel> planta = new ArrayList<>();
        planta.addAll(maquinas);
        planta.addAll(produtosFabricados);
        auditar("RELATÓRIO GERAL DA PLANTA", planta);
    }

    public void auditarMaquinas() {
        auditar("DIAGNÓSTICO DAS MÁQUINAS", new ArrayList<Auditavel>(maquinas));
    }

    public void auditarProdutos() {
        auditar("DIAGNÓSTICO DAS BOLACHAS NO ARMAZÉM", new ArrayList<Auditavel>(produtosFabricados));
    }

    // Maquina e bolacha nao tem nada em comum na hierarquia; aqui as duas
    // sao so Auditavel, e cada uma responde do seu jeito.
    private void auditar(String titulo, List<Auditavel> itens) {
        painel.abrirAuditoria(titulo);
        int emAlerta = 0;
        int exibidos = 0;
        for (Auditavel item : itens) {
            boolean alerta = item.precisaManutencao();
            if (alerta) {
                emAlerta++;
            }
            if (exibidos < LIMITE_AUDITORIA) {
                painel.linhaAuditoria(item.gerarRelatorioDiagnostico(), alerta);
                exibidos++;
            }
        }
        painel.fecharAuditoria(itens.size(), exibidos, emAlerta);
    }

    public void exibirArmazem() {
        painel.exibirArmazem(produtosFabricados);
    }

    public double getBudget() {
        return budget;
    }

    public Cenario getCenario() {
        return cenario;
    }
}
