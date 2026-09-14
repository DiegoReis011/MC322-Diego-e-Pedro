public class MateriaPrima {
    private String id;
    private String nome;
    private double quantidade;
    private String unidade;
    private double quantidadeMinima;
    private double custoPorUnidade;

    public MateriaPrima(String id, String nome, double quantidade, String unidade, double quantidadeMinima, double custoPorUnidade) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
        this.custoPorUnidade = custoPorUnidade;
    }

    // Devolve false em vez de lançar excecao: quem chama decide o que dizer ao operador.
    public boolean consumir(double quantidadeDemandada) {
        if (!verificarDisponibilidade(quantidadeDemandada)) {
            return false;
        }
        quantidade -= quantidadeDemandada;
        return true;
    }

    public void adicionarEstoque(double quantidadeAdicionada) {
        if (quantidadeAdicionada > 0) {
            quantidade += quantidadeAdicionada;
        }
    }

    public boolean verificarDisponibilidade(double demanda) {
        return demanda > 0 && quantidade >= demanda;
    }

    public boolean estaAbaixoDoMinimo() {
        return quantidade < quantidadeMinima;
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getUnidade() {
        return unidade;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public double getCustoPorUnidade() {
    return custoPorUnidade;
}
}
