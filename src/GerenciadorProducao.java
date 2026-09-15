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

    public double getBudget() {
        return budget;
    }

    public MateriaPrima getMateriaPrima() {
        return materiaPrima;
    }
}
