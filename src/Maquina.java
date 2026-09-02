public class Maquina {
    private String nome;
    private boolean ligada;
    private double capacidadeMaxima;

    public Maquina(String nome, double capacidadeMaxima) {
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ligada = false;
    }
}
