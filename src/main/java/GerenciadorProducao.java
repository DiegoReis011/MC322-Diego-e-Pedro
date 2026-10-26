import java.util.ArrayList;
import java.util.List;

/**
 * O cérebro da fábrica: guarda o budget, a despensa, as demandas, a linha de
 * máquinas, o armazém e a estratégia de produção atual.
 *
 * <p>É o Contexto do padrão Strategy: só conhece a interface
 * {@link EstrategiaProducao}, então a estratégia pode ser trocada a qualquer
 * momento sem mexer nesta classe.</p>
 *
 * <p>Regra de consistência: toda operação que gasta alguma coisa (massa,
 * ingrediente ou budget) confere tudo antes de gastar. Se alguma conferência
 * falhar, ela lança uma {@link PlantaException} e nada do estoque nem do budget
 * é alterado. A única mudança que acompanha a falha é a prevista no enunciado:
 * falta de insumo ou de budget cancela a demanda.</p>
 *
 * <p>O gerenciador não lê o teclado. Quem fala com o operador e trata os erros
 * é o {@link Painel}, chamado pelo {@link Main}.</p>
 *
 * @author Pedro Magalhães
 */
public class GerenciadorProducao {
    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    // O agrupador das maquinas. O gerenciador nunca mexe nas partes delas.
    private LinhaDeProducao linha;

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

    // Quem mostra o andamento da fornada e os relatorios continua sendo o painel.
    private Painel painel;

    /**
     * Monta a fábrica vazia, com o budget inicial do cenário escolhido.
     *
     * @param materiaPrima      a massa da casa, comum a todas as bolachas
     * @param cenario           cenário de operação (define budget, desgaste e falhas)
     * @param estrategiaInicial estratégia usada até o operador trocar
     * @param painel            painel que mostra o andamento das operações
     */
    public GerenciadorProducao(MateriaPrima materiaPrima, Cenario cenario,
            EstrategiaProducao estrategiaInicial, Painel painel) {
        this.materiaPrima = materiaPrima;
        this.cenario = cenario;
        this.budget = cenario.getBudgetInicial();
        this.estrategiaAtual = estrategiaInicial;
        this.painel = painel;
        this.demandas = new ArrayList<>();
        this.produtosFabricados = new ArrayList<>();
        this.linha = new LinhaDeProducao("da Casa");
        this.catalogo = new ArrayList<>();
        this.ingredientes = new ArrayList<>();
        this.contadorLotes = 0;
    }

    /**
     * Põe uma máquina no fim da linha, já ajustada ao cenário. A ordem de
     * chamada é a ordem em que a bolacha passa pelas máquinas.
     *
     * @param maquina máquina a instalar
     */
    public void adicionarMaquina(Maquina maquina) {
        maquina.aplicarCenario(cenario);
        linha.adicionarMaquina(maquina);
    }

    /**
     * Põe um ingrediente de sabor na despensa. O nome dele precisa ser igual
     * ao sabor das bolachas que o consomem.
     *
     * @param ingrediente ingrediente a guardar
     */
    public void adicionarIngrediente(MateriaPrima ingrediente) {
        ingredientes.add(ingrediente);
    }

    /**
     * Cadastra uma bolacha que a fábrica sabe fazer. Ela serve de molde para
     * as cópias fabricadas em cada lote.
     *
     * @param molde bolacha modelo
     */
    public void adicionarAoCatalogo(Produto molde) {
        catalogo.add(molde);
    }

    /**
     * Informa as bolachas que a fábrica sabe fazer.
     *
     * @return os moldes do catálogo, na ordem de cadastro
     */
    public ArrayList<Produto> getCatalogo() {
        return catalogo;
    }

    // ---------------------------------------------------------------
    // Estrategia
    // ---------------------------------------------------------------

    /**
     * Troca a estratégia de produção em tempo de execução. Uma estratégia
     * nula é ignorada e a atual continua valendo.
     *
     * @param novaEstrategia estratégia que passa a escolher as demandas
     */
    public void setEstrategia(EstrategiaProducao novaEstrategia) {
        if (novaEstrategia != null) {
            estrategiaAtual = novaEstrategia;
        }
    }

    /**
     * Informa a estratégia que escolhe a próxima demanda.
     *
     * @return estratégia atual
     */
    public EstrategiaProducao getEstrategiaAtual() {
        return estrategiaAtual;
    }

    /**
     * Pede à estratégia atual a próxima demanda e fabrica essa demanda. A
     * estratégia só aponta; quem gasta e fabrica é o gerenciador.
     *
     * @throws DemandaInvalidaException      se a estratégia não achar nenhuma
     *                                       demanda elegível
     * @throws MaquinaIndisponivelException  se alguma máquina da linha estiver
     *                                       quebrada (a demanda continua na fila)
     * @throws RecursoInsuficienteException  se faltar massa ou ingrediente (a
     *                                       demanda é cancelada)
     * @throws OrcamentoInsuficienteException se o budget não cobrir as máquinas
     *                                       (a demanda é cancelada)
     */
    public void executarProximaProducao() {
        Demanda escolhida = estrategiaAtual.selecionarDemanda(demandas, budget);
        if (escolhida == null) {
            throw new DemandaInvalidaException("A estratégia " + estrategiaAtual.getNomeEstrategia()
                    + " não achou nenhuma demanda elegível. Anote ou reabra uma demanda,"
                    + " ou troque de estratégia.");
        }
        painel.etapa("A estratégia " + estrategiaAtual.getNomeEstrategia() + " escolheu "
                + escolhida.getQuantidadeProdutos() + " x " + escolhida.getTipoProduto() + ".");
        fabricar(buscarMolde(escolhida.getTipoProduto()), escolhida);
    }

    // ---------------------------------------------------------------
    // Demandas
    // ---------------------------------------------------------------

    /**
     * Anota um pedido de bolacha. Cada bolacha tem uma demanda só: registrar
     * de novo troca a quantidade da que já existe e, se ela estava concluída
     * ou cancelada, reabre.
     *
     * @param tipoProduto nome da bolacha, igual ao de um molde do catálogo
     * @param quantidade  quantas bolachas foram pedidas
     * @throws DemandaInvalidaException se a bolacha não estiver no catálogo, se
     *                                  a quantidade não for maior que zero ou se
     *                                  a demanda estiver em produção
     */
    public void registrarDemanda(String tipoProduto, int quantidade) {
        if (buscarMolde(tipoProduto) == null) {
            throw new DemandaInvalidaException("A fábrica não faz " + tipoProduto
                    + ". Escolha uma bolacha do catálogo.");
        }
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

    /**
     * Troca a quantidade de uma demanda que já existe.
     *
     * @param tipoProduto nome da bolacha da demanda
     * @param quantidade  nova quantidade pedida
     * @throws DemandaInvalidaException se não houver demanda dessa bolacha, se
     *                                  a quantidade não for maior que zero ou se
     *                                  a demanda estiver em produção
     */
    public void atualizarDemanda(String tipoProduto, int quantidade) {
        buscarDemandaRegistrada(tipoProduto).atualizarQuantidade(quantidade);
    }

    /**
     * Informa as demandas anotadas.
     *
     * @return as demandas, na ordem em que chegaram
     */
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

    // Mesma busca, mas aqui nao achar e erro de quem chamou.
    private Demanda buscarDemandaRegistrada(String tipoProduto) {
        Demanda demanda = buscarDemanda(tipoProduto);
        if (demanda == null) {
            throw new DemandaInvalidaException("Não tem demanda registrada de " + tipoProduto
                    + ". Anote uma no menu Demandas.");
        }
        return demanda;
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

    /**
     * Compra matéria-prima com o budget. Confere o preço antes: se o budget
     * não cobre, nada é gasto e o estoque fica como estava.
     *
     * @param insumo     massa ou ingrediente a comprar
     * @param quantidade quanto comprar, na unidade do insumo
     * @throws IllegalArgumentException      se a quantidade não for maior que
     *                                       zero (o painel já impede isso)
     * @throws OrcamentoInsuficienteException se a compra custar mais que o budget
     */
    public void comprarMateriaPrima(MateriaPrima insumo, double quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade comprada precisa ser maior que zero.");
        }
        double custo = calcularCustoCompra(insumo, quantidade);
        if (custo > budget) {
            throw new OrcamentoInsuficienteException(custo, budget);
        }
        insumo.adicionarEstoque(quantidade);
        budget -= custo;
    }

    /**
     * Calcula o preço de uma compra, sem comprar nada.
     *
     * @param insumo     massa ou ingrediente
     * @param quantidade quanto comprar, na unidade do insumo
     * @return preço da compra, em reais
     */
    public double calcularCustoCompra(MateriaPrima insumo, double quantidade) {
        return insumo.getCustoPorUnidade() * quantidade;
    }

    /**
     * Junta a massa e os ingredientes numa lista só, na ordem em que o menu de
     * compra numera os insumos.
     *
     * @return a massa seguida dos ingredientes de sabor
     */
    public ArrayList<MateriaPrima> getInsumos() {
        ArrayList<MateriaPrima> todos = new ArrayList<>();
        todos.add(materiaPrima);
        for (int i = 0; i < ingredientes.size(); i++) {
            todos.add(ingredientes.get(i));
        }
        return todos;
    }

    /** Mostra o budget atual no painel. */
    public void exibirBudget() {
        painel.exibirBudget(budget);
    }

    // ---------------------------------------------------------------
    // Fabricacao
    // ---------------------------------------------------------------

    /**
     * Fabricação manual: o operador escolhe a bolacha, sem passar pela
     * estratégia. A demanda dela precisa existir e estar na fila.
     *
     * @param molde bolacha do catálogo a fabricar
     * @throws DemandaInvalidaException      se não houver demanda dessa bolacha
     *                                       ou se ela não estiver pendente
     * @throws MaquinaIndisponivelException  se alguma máquina da linha estiver
     *                                       quebrada (a demanda continua na fila)
     * @throws RecursoInsuficienteException  se faltar massa ou ingrediente (a
     *                                       demanda é cancelada)
     * @throws OrcamentoInsuficienteException se o budget não cobrir as máquinas
     *                                       (a demanda é cancelada)
     */
    public void fabricarDemanda(Produto molde) {
        Demanda demanda = buscarDemandaRegistrada(molde.getNome());
        if (!demanda.isPendente()) {
            throw new DemandaInvalidaException("A demanda de " + molde.getNome() + " não está na fila ("
                    + demanda.getStatus().getDescricao() + "). Atualize a demanda para reabrir.");
        }
        fabricar(molde, demanda);
    }

    // Confere tudo antes de gastar qualquer coisa: fornada recusada nao pode
    // deixar massa gasta, budget debitado nem demanda pela metade.
    private void fabricar(Produto molde, Demanda demanda) {
        Maquina quebrada = linha.buscarMaquinaQuebrada();
        if (quebrada != null) {
            // Maquina quebrada nao e falta de budget nem de insumo: a demanda segue na fila.
            throw new MaquinaIndisponivelException(quebrada.getNome(),
                    "ela quebrou. Chame a manutenção na Auditoria.");
        }

        int quantidade = demanda.getQuantidadeProdutos();
        double massaNecessaria = demanda.calcularMateriaPrimaNecessaria(
                molde.getQuantidadeMateriaPrimaPorUnidade());
        double ingredienteNecessario = massaNecessaria * PROPORCAO_INGREDIENTE;
        MateriaPrima ingrediente = buscarIngrediente(molde.getSabor());
        double custo = calcularCustoProducao(quantidade);

        if (ingrediente == null) {
            throw cancelarPorFalta(demanda, new RecursoInsuficienteException(
                    molde.getSabor(), ingredienteNecessario, 0, "g"));
        }
        if (!materiaPrima.verificarDisponibilidade(massaNecessaria)) {
            throw cancelarPorFalta(demanda, new RecursoInsuficienteException(materiaPrima.getNome(),
                    massaNecessaria, materiaPrima.getQuantidade(), materiaPrima.getUnidade()));
        }
        if (!ingrediente.verificarDisponibilidade(ingredienteNecessario)) {
            throw cancelarPorFalta(demanda, new RecursoInsuficienteException(ingrediente.getNome(),
                    ingredienteNecessario, ingrediente.getQuantidade(), ingrediente.getUnidade()));
        }
        if (custo > budget) {
            throw cancelarPorFalta(demanda, new OrcamentoInsuficienteException(custo, budget));
        }

        // Daqui pra baixo nada mais pode faltar: tudo foi conferido acima.
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
            linha.processar(bolacha);
            if (bolacha.getStatus() == StatusBolacha.APROVADA) {
                produtosFabricados.add(bolacha);
                aprovadas++;
            }
        }
        desligarLinha();
        desgastarLinha();

        demanda.atender();
        painel.etapa(aprovadas + " aprovadas, " + (quantidade - aprovadas) + " no refugo.");
    }

    // Falta de budget ou insumo cancela a demanda, como o enunciado define.
    // Sem isso a Fila do Forno escolheria a mesma demanda impossivel pra sempre.
    // Devolve o erro pra quem chamou fazer o throw.
    private PlantaException cancelarPorFalta(Demanda demanda, PlantaException motivo) {
        demanda.cancelar();
        return motivo;
    }

    // Uma fornada = um ciclo de uso pra cada maquina da linha.
    private void desgastarLinha() {
        for (Maquina maquina : linha.getMaquinas()) {
            maquina.registrarCiclo();
            if (maquina.estaQuebrada()) {
                painel.alerta(maquina.getNome() + " soltou fumaça e parou. A linha só volta depois da manutenção.");
            } else if (maquina.precisaManutencao()) {
                painel.alerta(maquina.getNome() + " está rangendo: tem peça no fim. Saúde geral em "
                        + String.format("%.1f", maquina.getSaudeGeral()) + "%.");
            }
        }
    }

    private double calcularCustoProducao(int quantidade) {
        return linha.calcularCustoPorProduto() * quantidade;
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


    private void ligarLinha() {
        linha.ligar();
    }

    private void desligarLinha() {
        linha.desligar();
    }

    // ---------------------------------------------------------------
    // Manutencao
    // ---------------------------------------------------------------

    /**
     * Informa as máquinas da linha, só para leitura. A lista devolvida é
     * imutável: tentar alterá-la lança {@link UnsupportedOperationException}.
     *
     * @return as máquinas, na ordem em que a bolacha passa por elas
     */
    public List<Maquina> getMaquinas() {
        return linha.getMaquinas();
    }

    /** Mostra a linha de produção: as máquinas na ordem da bolacha, com tipo e estado. */
    public void exibirLinha() {
        painel.exibirLinha(linha.getNome(), linha.getMaquinas());
    }

    /**
     * Calcula quanto custa devolver a máquina e as suas peças a 100% de saúde.
     * A manutenção cobra por ponto de saúde geral que falta.
     *
     * @param maquina máquina a avaliar
     * @return preço do reparo, em reais (zero se a máquina já está nova)
     */
    public double calcularCustoReparo(Maquina maquina) {
        return (100.0 - maquina.getSaudeGeral()) * CUSTO_REPARO_POR_PONTO;
    }

    /**
     * Repara a máquina e cobra do budget. Máquina já nova não é reparada nem
     * cobrada; o painel só avisa.
     *
     * @param maquina máquina a reparar
     * @throws OrcamentoInsuficienteException se o reparo custar mais que o
     *                                       budget (nada é cobrado nem reparado)
     */
    public void repararMaquina(Maquina maquina) {
        double custo = calcularCustoReparo(maquina);
        if (custo <= 0) {
            painel.aviso(maquina.getNome() + " já está nova em folha.");
            return;
        }
        if (custo > budget) {
            throw new OrcamentoInsuficienteException(custo, budget);
        }
        budget -= custo;
        maquina.reparar();
        painel.etapa("Manutenção feita: " + maquina.getNome() + " de volta a 100% por R$ " + String.format("%.2f", custo) + ".");
    }

    // ---------------------------------------------------------------
    // Auditoria e armazem
    // ---------------------------------------------------------------

    /**
     * Relatório da planta inteira: a linha (com máquinas e peças) e as bolachas
     * juntas, tratadas só como {@link Auditavel}.
     */
    public void gerarAuditoriaGeral() {
        List<Auditavel> planta = new ArrayList<>();
        planta.add(linha);
        planta.addAll(produtosFabricados);
        auditar("RELATÓRIO GERAL DA PLANTA", planta);
    }

    /**
     * Relatório estrutural em cascata: a linha, cada máquina dela e as peças de
     * cada máquina, indentados por nível.
     */
    public void auditarMaquinas() {
        painel.abrirAuditoria("ESTRUTURA DA LINHA (linha > máquinas > peças)");
        painel.linhaAuditoria(linha.gerarRelatorioDiagnostico(), linha.precisaManutencao());
        int emAlerta = 0;
        for (Maquina maquina : linha.getMaquinas()) {
            if (maquina.precisaManutencao()) {
                emAlerta++;
            }
        }
        painel.fecharAuditoria(linha.getMaquinas().size(), linha.getMaquinas().size(), emAlerta);
    }

    /** Relatório só das bolachas aprovadas no armazém. */
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

    /** Mostra as bolachas aprovadas no armazém, agrupadas por lote. */
    public void exibirArmazem() {
        painel.exibirArmazem(produtosFabricados);
    }

    /**
     * Informa o dinheiro em caixa.
     *
     * @return budget atual, em reais
     */
    public double getBudget() {
        return budget;
    }

    /**
     * Informa o cenário escolhido no início.
     *
     * @return cenário de operação
     */
    public Cenario getCenario() {
        return cenario;
    }
}
