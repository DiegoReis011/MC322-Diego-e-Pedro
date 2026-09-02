public class Maquina {
    private String nome;
    private boolean ligada;
    private double capacidadeMaxima;

    public Maquina(String nome, double capacidadeMaxima) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ligada = false;
    }

    public void ligar() {
        ligada = true;
    }

    public void desligar() {
        ligada = false;
    }

    public boolean estaLigada() {
        return ligada;
    }

    public String getNome() {
        return nome;
    }

    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }
}
