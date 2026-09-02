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

    // As tres recusas vem antes de consumir, senao dava pra gastar massa numa
    // fornada que nem ia acontecer.
    public boolean processar(MateriaPrima materiaPrima, Produto produto, double demanda) {
        if (!ligada) {
            return false;
        }
        if (demanda > capacidadeMaxima) {
            return false;
        }
        if (!materiaPrima.consumir(demanda)) {
            return false;
        }
        produto.processar();
        return true;
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
