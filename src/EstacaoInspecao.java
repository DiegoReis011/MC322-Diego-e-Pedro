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

    public boolean estaAtiva() {
        return ativa;
    }

    public int getTotalInspecionados() {
        return produtosInspecionados;
    }
}
