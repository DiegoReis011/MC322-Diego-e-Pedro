public class Esteira {
    private Object item;
    private boolean emMovimento;
    private double capacidadeMaxima;

    public Esteira(double capacidadeMaxima) {
        this.capacidadeMaxima = capacidadeMaxima;
        this.item = null;
        this.emMovimento = false;
    }
}
