public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private StatusDemanda status;

    // Quanto custa fabricar UMA unidade (maquinas + insumos). Quem registra a
    // demanda preenche; a estrategia de maximo de produtos usa pra checar o budget.
    private double custoUnitarioEstimado;

    public Demanda(String tipoProduto, int quantidadeProdutos) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        this.status = StatusDemanda.PENDENTE;
        this.custoUnitarioEstimado = 0.0;
    }

    // Pedido novo reabre a demanda: se ja estava concluida ou cancelada, volta
    // pra PENDENTE. So nao mexe enquanto estiver em producao.
    public void atualizarQuantidade(int novaQuantidade) {
        if (novaQuantidade <= 0 || status == StatusDemanda.EM_PRODUCAO) {
            return;
        }
        quantidadeProdutos = novaQuantidade;
        status = StatusDemanda.PENDENTE;
    }

    public double calcularMateriaPrimaNecessaria(double quantidadeMateriaPrimaPorUnidade) {
        return quantidadeProdutos * quantidadeMateriaPrimaPorUnidade;
    }

    // Transicoes de estado: cada uma devolve true se a mudanca aconteceu.
    public boolean iniciarProducao() {
        if (status != StatusDemanda.PENDENTE) {
            return false;
        }
        status = StatusDemanda.EM_PRODUCAO;
        return true;
    }

    public boolean atender() {
        if (status == StatusDemanda.CANCELADA || status == StatusDemanda.CONCLUIDA) {
            return false;
        }
        status = StatusDemanda.CONCLUIDA;
        return true;
    }

    public boolean cancelar() {
        if (status == StatusDemanda.CONCLUIDA) {
            return false;
        }
        status = StatusDemanda.CANCELADA;
        return true;
    }

    // Viabilidade financeira
    public double calcularCustoEstimado() {
        return quantidadeProdutos * custoUnitarioEstimado;
    }

    public boolean isViavel(double orcamentoDisponivel) {
        return calcularCustoEstimado() <= orcamentoDisponivel;
    }

    public void setCustoUnitarioEstimado(double custoUnitarioEstimado) {
        if (custoUnitarioEstimado >= 0) {
            this.custoUnitarioEstimado = custoUnitarioEstimado;
        }
    }

    public double getCustoUnitarioEstimado() {
        return custoUnitarioEstimado;
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public StatusDemanda getStatus() {
        return status;
    }

    // Filtro que todas as estrategias vao usar (DRY, como o enunciado sugere).
    public boolean isPendente() {
        return status == StatusDemanda.PENDENTE;
    }
}
