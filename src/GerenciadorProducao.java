import java.util.ArrayList;

public class GerenciadorProducao {
    private ArrayList<Demanda> demandas;
    private ArrayList<Produto> produtosFabricados;
    private ArrayList<Maquina> maquinas;

    // A massa e a materia-prima da linha, comum a toda bolacha. Os
    // ingredientes entram por cima dela: o sabor do produto decide qual.
    private MateriaPrima materiaPrima;
    private ArrayList<MateriaPrima> ingredientes;

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

    public double getBudget() {
        return budget;
    }

    public MateriaPrima getMateriaPrima() {
        return materiaPrima;
    }
}
