public class Demanda {
    private String tipoProduto;
    private int quantidadeProdutos;
    private boolean atendida;

    public Demanda(String tipoProduto, int quantidadeProdutos) {
        this.tipoProduto = tipoProduto;
        this.quantidadeProdutos = quantidadeProdutos;
        this.atendida = false;
    }

    public void atualizarQuantidade(int novaQuantidade) {
        if (novaQuantidade > 0) {
            quantidadeProdutos = novaQuantidade;
        }
    }

    public double calcularMateriaPrimaNecessaria(double quantidadeMateriaPrimaPorUnidade) {
        return quantidadeProdutos * quantidadeMateriaPrimaPorUnidade;
    }

    public void atender() {
        atendida = true;
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public int getQuantidadeProdutos() {
        return quantidadeProdutos;
    }

    public boolean isAtendida() {
        return atendida;
    }
}
