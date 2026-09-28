import java.util.ArrayList;

public class GerenciadorProducao {
    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private ArrayList<Maquina> maquinas;

    // A massa e a materia-prima da linha, comum a toda bolacha. Os
    // ingredientes entram por cima dela: o sabor do produto decide qual.
    private MateriaPrima materiaPrima;
    private ArrayList<MateriaPrima> ingredientes;

    // Fracao da massa que vira ingrediente de sabor.
    private static final double PROPORCAO_INGREDIENTE = 0.20;

    private double budget;

    // O gerenciador nao imprime nada por conta propria. Quem fala com o
    // operador continua sendo o painel.
    private Painel painel;

    public GerenciadorProducao(MateriaPrima materiaPrima, double budget, Painel painel) {
        this.materiaPrima = materiaPrima;
        this.budget = budget;
        this.painel = painel;
        this.demandas = new ArrayList<>();
        this.produtosFabricados = new ArrayList<>();
        this.maquinas = new ArrayList<>();
        this.ingredientes = new ArrayList<>();
    }

    public void adicionarMaquina(Maquina maquina) {
        maquinas.add(maquina);
    }

    public void adicionarIngrediente(MateriaPrima ingrediente) {
        ingredientes.add(ingrediente);
    }

    // Registrar de novo mexe na demanda que ja existe: uma por bolacha.
    public void registrarDemanda(String tipoProduto, int quantidade) {
        Demanda existente = buscarDemanda(tipoProduto);
        if (existente == null) {
            demandas.add(new Demanda(tipoProduto, quantidade));
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

    public Demanda getDemanda(String tipoProduto) {
        return buscarDemanda(tipoProduto);
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

    public boolean fabricarDemanda(Produto molde) {
        Demanda demanda = buscarDemanda(molde.getNome());
        if (demanda == null) {
            painel.recusa("Nao tem demanda registrada de " + molde.getNome() + ".");
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
            painel.recusa("A despensa nao tem " + molde.getSabor() + ".");
            return false;
        }
        if (!materiaPrima.verificarDisponibilidade(massaNecessaria)) {
            painel.recusa("Faltou massa: precisa de " + massaNecessaria + " g e so tem "
                    + materiaPrima.getQuantidade() + " g.");
            return false;
        }
        if (!ingrediente.verificarDisponibilidade(ingredienteNecessario)) {
            painel.recusa("Faltou " + ingrediente.getNome() + ": precisa de "
                    + ingredienteNecessario + " g e so tem "
                    + ingrediente.getQuantidade() + " g.");
            return false;
        }
        if (custo > budget) {
            painel.recusa("As maquinas cobram R$ " + String.format("%.2f", custo)
                    + " e o budget tem R$ " + String.format("%.2f", budget) + ".");
            return false;
        }

        materiaPrima.consumir(massaNecessaria);
        ingrediente.consumir(ingredienteNecessario);
        budget -= custo;

        painel.etapa(quantidade + " x " + molde.getNome() + " na linha.");
        painel.etapa("Gastou " + massaNecessaria + " g de massa e "
                + ingredienteNecessario + " g de " + ingrediente.getNome() + ".");

        ligarLinha();
        int aprovadas = 0;
        for (int i = 0; i < quantidade; i++) {
            Produto bolacha = molde.criarCopia(molde.getId() + "-" + (i + 1));
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

        demanda.atender();
        painel.etapa(aprovadas + " aprovadas, " + (quantidade - aprovadas) + " no refugo.");
        return true;
    }

    public void exibirArmazem() {
        painel.exibirArmazem(produtosFabricados);
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

    public double getBudget() {
        return budget;
    }

    public MateriaPrima getMateriaPrima() {
        return materiaPrima;
    }
}
