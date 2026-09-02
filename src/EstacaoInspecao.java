public class EstacaoInspecao {
    private boolean ativa;
    private int produtosInspecionados;

    public EstacaoInspecao() {
        this.ativa = false;
        this.produtosInspecionados = 0;
    }

    public void ativar() {
        ativa = true;
    }

    public void desativar() {
        ativa = false;
    }

    // Por enquanto a inspecao so aprova, que e o que a tarefa 1 pede. Reprovar
    // fica pra quando tiver criterio de qualidade.
    public boolean inspecionar(Produto produto) {
        if (!ativa) {
            return false;
        }
        produto.aprovar();
        produtosInspecionados++;
        return true;
    }

    public boolean estaAtiva() {
        return ativa;
    }

    public int getTotalInspecionados() {
        return produtosInspecionados;
    }
}
