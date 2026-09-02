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
