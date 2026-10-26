import java.util.Random;

/**
 * Peça interna de uma {@link Maquina}. Cada parte tem saúde e desgaste
 * próprios, mas só existe dentro da máquina que a criou: é ela quem dá a
 * partida no desgaste, manda reparar e consulta o diagnóstico (composição).
 *
 * <p>Regras de desgaste, iguais às da máquina:</p>
 * <ul>
 *   <li>a saúde vai de 0 a 100 e cai um valor aleatório a cada uso;</li>
 *   <li>abaixo de 30 a parte pede manutenção;</li>
 *   <li>abaixo de 10 ela está quebrada.</li>
 * </ul>
 *
 * <p>O que uma parte quebrada ou gasta causa na máquina é definido em cada
 * subclasse ({@link Motor}, {@link PainelDeControle}, {@link Engrenagem}). A
 * parte não sabe nada de orçamento, estratégia ou demanda.</p>
 *
 * @author Diego Reis
 */
public abstract class ParteDeMaquina implements Auditavel {
    private static final double SAUDE_MAXIMA = 100.0;
    // Abaixo disso a parte pede manutencao.
    private static final double LIMIAR_MANUTENCAO = 30.0;
    // Abaixo disso a parte quebrou.
    private static final double LIMIAR_QUEBRA = 10.0;

    private final Random sorteio;
    private final String nome;
    private final String rotuloOk;
    private final String rotuloDesgastada;
    private final String rotuloQuebrada;
    private double saude;

    /**
     * Cria a parte com saúde cheia.
     *
     * @param nome             nome da parte nos relatórios
     * @param rotuloOk         como o relatório chama a parte em bom estado
     * @param rotuloDesgastada como o relatório chama a parte que pede manutenção
     * @param rotuloQuebrada   como o relatório chama a parte quebrada
     */
    protected ParteDeMaquina(String nome, String rotuloOk, String rotuloDesgastada,
            String rotuloQuebrada) {
        this.sorteio = new Random();
        this.nome = nome;
        this.rotuloOk = rotuloOk;
        this.rotuloDesgastada = rotuloDesgastada;
        this.rotuloQuebrada = rotuloQuebrada;
        this.saude = SAUDE_MAXIMA;
    }

    /**
     * Registra um uso: tira da saúde um valor aleatório entre 0 e o teto de
     * desgaste. A máquina chama isto uma vez por fornada, passando o teto do
     * cenário.
     *
     * @param desgasteMaximo teto de desgaste de um uso, em pontos de saúde
     */
    public void registrarUso(double desgasteMaximo) {
        saude = Math.max(0, saude - sorteio.nextDouble() * Math.max(0, desgasteMaximo));
    }

    /** Devolve a parte a 100% de saúde. Quem cobra o reparo é o gerenciador. */
    public void reparar() {
        saude = SAUDE_MAXIMA;
    }

    /**
     * Diz se a parte quebrou.
     *
     * @return {@code true} se a saúde estiver abaixo de 10
     */
    public boolean estaQuebrada() {
        return saude < LIMIAR_QUEBRA;
    }

    /**
     * Informa a saúde atual.
     *
     * @return saúde, de 0 a 100
     */
    public double getSaude() {
        return saude;
    }

    /**
     * Informa o nome da parte.
     *
     * @return nome da parte
     */
    public String getNome() {
        return nome;
    }

    /**
     * Mede o quanto a parte ainda rende: a saúde dela em fração, ou zero se
     * estiver quebrada.
     *
     * @return rendimento de 0.0 a 1.0
     */
    public double getEficiencia() {
        return estaQuebrada() ? 0.0 : saude / SAUDE_MAXIMA;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Para a parte: saúde abaixo de 30.</p>
     */
    @Override
    public boolean precisaManutencao() {
        return saude < LIMIAR_MANUTENCAO;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Para a parte: uma linha curta, por exemplo
     * {@code Motor: Superaquecido (22%)}.</p>
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("%s: %s (%.0f%%)", nome, descreverEstado(), saude);
    }

    /**
     * Traduz a saúde num rótulo temático, sem o número.
     *
     * @return o rótulo de bom estado, de desgaste ou de quebra
     */
    protected String descreverEstado() {
        if (estaQuebrada()) {
            return rotuloQuebrada;
        }
        if (precisaManutencao()) {
            return rotuloDesgastada;
        }
        return rotuloOk;
    }
}
