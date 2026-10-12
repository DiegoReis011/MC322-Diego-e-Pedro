/**
 * Pedido de fabricação de um tipo de bolacha, com o ciclo de vida controlado
 * pelo enum {@link StatusDemanda}.
 *
 * <p>Transições permitidas:</p>
 * <ul>
 *   <li>{@code PENDENTE} para {@code EM_PRODUCAO}: {@link #iniciarProducao()}</li>
 *   <li>{@code EM_PRODUCAO} para {@code CONCLUIDA}: {@link #atender()}</li>
 *   <li>{@code PENDENTE} ou {@code EM_PRODUCAO} para {@code CANCELADA}: {@link #cancelar()}</li>
 *   <li>{@code CONCLUIDA} ou {@code CANCELADA} para {@code PENDENTE}: um novo
 *       pedido via {@link #atualizarQuantidade(int)} reabre a demanda</li>
 * </ul>
 *
 * <p>Toda transição inválida lança {@link DemandaInvalidaException} e deixa a
 * demanda exatamente como estava.</p>
 *
 * @author Diego Reis
 */
public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;

    // Quanto o budget paga para fabricar UMA unidade. Quem registra a demanda
    // preenche; a estrategia de maximo de produtos usa pra checar o budget.
    private double custoUnitarioEstimado;

    /**
     * Cria uma demanda nova, já na fila ({@code PENDENTE}).
     *
     * @param tipoProduto        nome da bolacha pedida
     * @param quantidadeProdutos quantas bolachas foram pedidas
     * @throws DemandaInvalidaException se o nome estiver vazio ou a quantidade
     *                                  não for maior que zero
     */
    public Demanda(String tipoProduto, int quantidadeProdutos) {
        if (tipoProduto == null || tipoProduto.isBlank()) {
            throw new DemandaInvalidaException("A demanda precisa dizer qual bolacha foi pedida.");
        }
        if (quantidadeProdutos <= 0) {
            throw new DemandaInvalidaException("A quantidade pedida precisa ser maior que zero.");
        }
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        this.status = StatusDemanda.PENDENTE;
        this.custoUnitarioEstimado = 0.0;
    }

    /**
     * Troca a quantidade pedida. Um pedido novo reabre a demanda: se ela já
     * estava concluída ou cancelada, volta para {@code PENDENTE}.
     *
     * @param novaQuantidade nova quantidade de bolachas
     * @throws DemandaInvalidaException se a quantidade não for maior que zero
     *                                  ou se a demanda estiver em produção
     */
    public void atualizarQuantidade(int novaQuantidade) {
        if (novaQuantidade <= 0) {
            throw new DemandaInvalidaException("A quantidade pedida precisa ser maior que zero.");
        }
        if (status == StatusDemanda.EM_PRODUCAO) {
            throw new DemandaInvalidaException("A demanda de " + tipoProduto
                    + " está em produção e não pode ser alterada agora.");
        }
        quantidadeProdutos = novaQuantidade;
        status = StatusDemanda.PENDENTE;
    }

    /**
     * Calcula quanta massa a demanda inteira vai gastar.
     *
     * @param quantidadeMateriaPrimaPorUnidade massa gasta por bolacha, em gramas
     * @return massa total necessária, em gramas
     */
    public double calcularMateriaPrimaNecessaria(double quantidadeMateriaPrimaPorUnidade) {
        return quantidadeProdutos * quantidadeMateriaPrimaPorUnidade;
    }

    /**
     * Marca a demanda como em produção. Só vale para demandas {@code PENDENTE}.
     *
     * @throws DemandaInvalidaException se a demanda não estiver pendente
     */
    public void iniciarProducao() {
        if (status != StatusDemanda.PENDENTE) {
            throw transicaoInvalida("iniciar produção");
        }
        status = StatusDemanda.EM_PRODUCAO;
    }

    /**
     * Marca a demanda como concluída. Só vale para demandas que estão em
     * produção, então concluir uma demanda cancelada (ou que nem começou) é
     * recusado.
     *
     * @throws DemandaInvalidaException se a demanda não estiver em produção
     */
    public void atender() {
        if (status != StatusDemanda.EM_PRODUCAO) {
            throw transicaoInvalida("concluir");
        }
        status = StatusDemanda.CONCLUIDA;
    }

    /**
     * Cancela a demanda, por exemplo por falta de budget ou de insumo. Só vale
     * para demandas pendentes ou em produção.
     *
     * @throws DemandaInvalidaException se a demanda já estiver concluída ou
     *                                  cancelada
     */
    public void cancelar() {
        if (status != StatusDemanda.PENDENTE && status != StatusDemanda.EM_PRODUCAO) {
            throw transicaoInvalida("cancelar");
        }
        status = StatusDemanda.CANCELADA;
    }

    // Monta o erro padrao de transicao invalida (DRY entre as tres transicoes).
    private DemandaInvalidaException transicaoInvalida(String operacao) {
        return new DemandaInvalidaException(String.format(
                "Não dá pra %s a demanda de %s: ela está \"%s\".",
                operacao, tipoProduto, status.getDescricao()));
    }

    /**
     * Estima o custo de fabricar a demanda inteira, só com o custo das máquinas.
     *
     * @return quantidade pedida vezes o custo unitário estimado, em reais
     */
    public double calcularCustoEstimado() {
        return quantidadeProdutos * custoUnitarioEstimado;
    }

    /**
     * Diz se o budget cobre a demanda inteira.
     *
     * @param orcamentoDisponivel budget atual, em reais
     * @return {@code true} se o custo estimado cabe no orçamento
     */
    public boolean isViavel(double orcamentoDisponivel) {
        return calcularCustoEstimado() <= orcamentoDisponivel;
    }

    /**
     * Define quanto custa fabricar uma unidade. Quem registra a demanda (o
     * gerenciador) é quem sabe esse valor.
     *
     * @param custoUnitarioEstimado custo por bolacha, em reais
     * @throws IllegalArgumentException se o custo for negativo (erro de
     *                                  programação, não de operação)
     */
    public void setCustoUnitarioEstimado(double custoUnitarioEstimado) {
        if (custoUnitarioEstimado < 0) {
            throw new IllegalArgumentException("O custo unitário não pode ser negativo.");
        }
        this.custoUnitarioEstimado = custoUnitarioEstimado;
    }

    /**
     * Informa o custo estimado de uma bolacha.
     *
     * @return custo estimado por bolacha, em reais
     */
    public double getCustoUnitarioEstimado() {
        return custoUnitarioEstimado;
    }

    /**
     * Informa qual bolacha foi pedida.
     *
     * @return nome da bolacha pedida
     */
    public String getTipoProduto() {
        return tipoProduto;
    }

    /**
     * Informa quantas bolachas foram pedidas.
     *
     * @return quantidade de bolachas pedida
     */
    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    /**
     * Informa em que etapa a demanda está.
     *
     * @return estado atual da demanda
     */
    public StatusDemanda getStatus() {
        return status;
    }

    /**
     * Filtro comum das estratégias de produção: só demanda pendente disputa a
     * vez.
     *
     * @return {@code true} se o estado for {@code PENDENTE}
     */
    public boolean isPendente() {
        return status == StatusDemanda.PENDENTE;
    }
}
