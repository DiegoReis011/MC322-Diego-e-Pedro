import java.util.Random;

public abstract class Maquina {
    private Random random;
    private String nome;
    private boolean ligada;
    private double capacidadeMaxima;
    private double probabilidadeFalha;
    private double custoOperacional;

    public Maquina(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacional) {
        this.random = new Random();
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ligada = false;
        this.probabilidadeFalha = limitarProbabilidadeFalha(probabilidadeFalha);
        this.custoOperacional = custoOperacional;
    }
    
    public abstract boolean processar(Produto produto);
    
    public abstract String getTipo();

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

    public double getCustoOperacional() {
        return custoOperacional;
    }

    protected boolean verificarFalha() {
        return random.nextDouble() < probabilidadeFalha;
    }

    private double limitarProbabilidadeFalha(double valor) {
        return Math.max(0, Math.min(1, valor));
    }
}
