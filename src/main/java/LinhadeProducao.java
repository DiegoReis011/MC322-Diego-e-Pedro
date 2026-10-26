import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Agrupador da fábrica: uma linha de produção com várias máquinas, na ordem
 * em que a bolacha passa por elas.
 *
 * <p>É uma agregação, não uma composição: as máquinas nascem fora da linha e
 * continuam existindo se saírem dela, podendo até entrar em outra. Já as
 * partes de cada máquina (motor, painel, engrenagens) são composição e a linha
 * nem as enxerga.</p>
 *
 * <p>A lista de máquinas nunca sai daqui para ser alterada: quem precisa
 * mexer na linha usa {@link #adicionarMaquina(Maquina)} e
 * {@link #removerMaquina(Maquina)}.</p>
 *
 * @author Diego Reis
 */
public class LinhaDeProducao implements Auditavel {
    private final String nome;
    private final List<Maquina> maquinas;

    /**
     * Cria uma linha vazia.
     *
     * @param nome nome da linha nos menus e relatórios
     */
    public LinhaDeProducao(String nome) {
        this.nome = nome;
        this.maquinas = new ArrayList<>();
    }

    /**
     * Coloca uma máquina no fim da linha.
     *
     * @param maquina máquina a incluir
     * @throws IllegalArgumentException se a máquina for nula ou já estiver nesta linha
     */
    public void adicionarMaquina(Maquina maquina) {
        if (maquina == null) {
            throw new IllegalArgumentException("A linha precisa de uma máquina de verdade.");
        }
        if (maquinas.contains(maquina)) {
            throw new IllegalArgumentException("A máquina " + maquina.getNome()
                    + " já está na linha " + nome + ".");
        }
        maquinas.add(maquina);
    }

    /**
     * Tira uma máquina da linha. A máquina não é destruída: continua existindo
     * e pode ser colocada em outra linha.
     *
     * @param maquina máquina a retirar
     * @return {@code true} se ela estava na linha e saiu
     */
    public boolean removerMaquina(Maquina maquina) {
        return maquinas.remove(maquina);
    }

    /**
     * Devolve as máquinas na ordem da linha, só para leitura.
     *
     * @return visão imutável da lista; tentar alterá-la lança
     *         {@link UnsupportedOperationException}
     */
    public List<Maquina> getMaquinas() {
        return Collections.unmodifiableList(maquinas);
    }

    /**
     * Informa o nome da linha.
     *
     * @return nome da linha
     */
    public String getNome() {
        return nome;
    }

    /** Liga todas as máquinas da linha. */
    public void ligar() {
        for (Maquina maquina : maquinas) {
            maquina.ligar();
        }
    }

    /** Desliga todas as máquinas da linha. */
    public void desligar() {
        for (Maquina maquina : maquinas) {
            maquina.desligar();
        }
    }

    /**
     * Passa a bolacha por todas as máquinas, na ordem da linha. Máquina
     * desligada ou quebrada deixa a bolacha passar sem tocá-la.
     *
     * @param produto bolacha que entra na linha
     */
    public void processar(Produto produto) {
        for (Maquina maquina : maquinas) {
            maquina.processar(produto);
        }
    }

    /**
     * Soma o que cada máquina cobra para processar uma bolacha.
     *
     * @return custo de operação de uma bolacha na linha toda, em reais
     */
    public double calcularCustoPorProduto() {
        double total = 0;
        for (Maquina maquina : maquinas) {
            total += maquina.getCustoOperacional();
        }
        return total;
    }

    /**
     * Procura a primeira máquina quebrada da linha.
     *
     * @return a máquina quebrada, ou {@code null} se todas estiverem inteiras
     */
    public Maquina buscarMaquinaQuebrada() {
        for (Maquina maquina : maquinas) {
            if (maquina.estaQuebrada()) {
                return maquina;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Auditoria em cascata: o texto tem uma linha com o nome da linha e,
     * abaixo, o relatório de cada máquina (que por sua vez inclui as partes),
     * cada nível um pouco mais indentado.</p>
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        if (maquinas.isEmpty()) {
            return "Linha " + nome + " (sem máquinas)";
        }
        StringBuilder texto = new StringBuilder("Linha " + nome);
        if (precisaManutencao()) {
            texto.append("  [precisa de manutenção]");
        }
        for (Maquina maquina : maquinas) {
            for (String linha : maquina.gerarRelatorioDiagnostico().split("\n")) {
                texto.append("\n  ").append(linha);
            }
        }
        return texto.toString();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Para a linha: alguma máquina dela pede manutenção.</p>
     */
    @Override
    public boolean precisaManutencao() {
        for (Maquina maquina : maquinas) {
            if (maquina.precisaManutencao()) {
                return true;
            }
        }
        return false;
    }
}
