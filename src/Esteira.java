public class Esteira {
    private Object item;
    private boolean emMovimento;
    private double capacidadeMaxima;

    public Esteira(double capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
        this.item = null;
        this.emMovimento = false;
    }

    public void ligar() {
        emMovimento = true;
    }

    public void desligar() {
        emMovimento = false;
    }

    // O item e Object porque sem heranca a massa e a bolacha nao tem nada em
    // comum. O peso vem de fora pelo mesmo motivo: a esteira nao tem como
    // perguntar pra um Object quanto ele pesa.
    public boolean adicionarItem(Object novoItem, double peso) {
        if (!emMovimento) {
            return false;
        }
        if (item != null) {
            return false;
        }
        if (!verificarCapacidade(peso)) {
            return false;
        }
        item = novoItem;
        return true;
    }

    public Object removerItem() {
        Object retirado = item;
        item = null;
        return retirado;
    }

    public boolean estaEmMovimento() {
        return emMovimento;
    }

    public boolean verificarCapacidade(double peso) {
        return peso > 0 && peso <= capacidadeMaxima;
    }

    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
}
