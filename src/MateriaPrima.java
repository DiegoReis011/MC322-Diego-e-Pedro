/**
 * Insumo em estoque na despensa da fábrica: a massa da casa ou um ingrediente
 * de sabor.
 *
 * <p>As operações que mexem no estoque validam antes de alterar. Se uma delas
 * falhar, a quantidade fica exatamente como estava.</p>
 *
 * @author Diego Reis
 */
public class MateriaPrima {
    private String id;
    private String nome;
    private double quantidade;
    private String unidade;
    private double quantidadeMinima;
    private double custoPorUnidade;

    /**
     * Cria um insumo com o estoque inicial.
     *
     * @param id               código do insumo
     * @param nome             nome do insumo (o ingrediente de sabor usa o mesmo nome do sabor da bolacha)
     * @param quantidade       estoque inicial
     * @param unidade          unidade de medida, por exemplo "g"
     * @param quantidadeMinima abaixo deste valor a despensa avisa que está no fim
     * @param custoPorUnidade  preço de compra por unidade, em reais
     */
    public MateriaPrima(String id, String nome, double quantidade, String unidade,
            double quantidadeMinima, double custoPorUnidade) {
        this.id = id;
        this.nome = nome;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.quantidadeMinima = quantidadeMinima;
        this.custoPorUnidade = custoPorUnidade;
    }

    /**
     * Tira uma quantidade do estoque. Se não houver o bastante, nada é
     * retirado.
     *
     * @param quantidadeDemandada quanto consumir
     * @throws RecursoInsuficienteException se o estoque for menor que o pedido
     * @throws IllegalArgumentException     se a quantidade não for maior que zero
     */
    public void consumir(double quantidadeDemandada) {
        if (quantidadeDemandada <= 0) {
            throw new IllegalArgumentException("A quantidade a consumir precisa ser maior que zero.");
        }
        if (quantidadeDemandada > quantidade) {
            throw new RecursoInsuficienteException(nome, quantidadeDemandada, quantidade, unidade);
        }
        quantidade -= quantidadeDemandada;
    }

    /**
     * Põe mais insumo no estoque.
     *
     * @param quantidadeAdicionada quanto entrou
     * @throws IllegalArgumentException se a quantidade não for maior que zero
     */
    public void adicionarEstoque(double quantidadeAdicionada) {
        if (quantidadeAdicionada <= 0) {
            throw new IllegalArgumentException("A quantidade a adicionar precisa ser maior que zero.");
        }
        quantidade += quantidadeAdicionada;
    }

    /**
     * Consulta se o estoque cobre um pedido, sem alterar nada. Serve para
     * conferir tudo antes de começar uma fornada.
     *
     * @param demanda quantidade que se pretende consumir
     * @return {@code true} se a quantidade é positiva e cabe no estoque
     */
    public boolean verificarDisponibilidade(double demanda) {
        return demanda > 0 && quantidade >= demanda;
    }

    /**
     * Diz se o estoque já está no fim.
     *
     * @return {@code true} se o estoque está abaixo do mínimo configurado
     */
    public boolean estaAbaixoDoMinimo() {
        return quantidade < quantidadeMinima;
    }

    /**
     * Informa o código do insumo.
     *
     * @return código do insumo
     */
    public String getId() {
        return id;
    }

    /**
     * Informa o nome do insumo.
     *
     * @return nome do insumo
     */
    public String getNome() {
        return nome;
    }

    /**
     * Informa a unidade de medida do estoque.
     *
     * @return unidade de medida do estoque
     */
    public String getUnidade() {
        return unidade;
    }

    /**
     * Informa quanto há em estoque agora.
     *
     * @return quantidade atual em estoque
     */
    public double getQuantidade() {
        return quantidade;
    }

    /**
     * Informa o preço de compra por unidade.
     *
     * @return preço de compra por unidade, em reais
     */
    public double getCustoPorUnidade() {
        return custoPorUnidade;
    }
}
