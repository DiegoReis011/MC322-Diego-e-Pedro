/**
 * Bolacha genérica da fábrica NotBiscoito. É abstrata: toda bolacha de verdade
 * é de um tipo ({@link BolachaAmanteigada}, {@link BolachaTradicional} ou
 * {@link BolachaCracker}), e o tipo decide a qualidade e quanta massa ela
 * gasta. O sabor é um atributo à parte e decide qual ingrediente entra.
 *
 * <p>Regras de negócio:</p>
 * <ul>
 *   <li>Cada bolacha acumula uma probabilidade de falha, que sobe toda vez que
 *       uma máquina erra com ela (limitada a 100%).</li>
 *   <li>Quanto maior a qualidade, mais rigorosa é a inspeção, então bolacha
 *       boa é reprovada com mais facilidade.</li>
 *   <li>Como {@link Auditavel}, a bolacha pede atenção quando o risco
 *       acumulado chega ao limiar de risco.</li>
 * </ul>
 *
 * @author Diego Reis
 * @author Pedro Magalhães
 */
public abstract class Produto implements Auditavel {
    private String id;
    private String nome;
    private StatusBolacha status;
    private double quantidadeMateriaPrimaPorUnidade;
    private double qualidade;
    private double probabilidadeFalhaAcumulada;
    private String sabor;
    // Numero da fornada que produziu a bolacha. 0 enquanto for so molde.
    private int lote;
    private static int totalProdutosFabricados = 0;
    // Limita quando a bolacha e de risco: so chega em 0.15 se as duas maquinas
    // de processamento errarem na mesma bolacha.
    private static final double LIMIAR_RISCO = 0.15;

    /**
     * Cria uma bolacha aguardando processamento e sem risco acumulado. Também
     * incrementa o contador {@link #getTotalProdutosFabricados()}.
     *
     * @param id                              código da bolacha
     * @param nome                            nome para menus e relatórios
     * @param quantidadeMateriaPrimaPorUnidade gramas de massa gastas por bolacha
     * @param qualidade                       qualidade de 0.0 a 1.0
     * @param sabor                           sabor, que é o nome do ingrediente gasto
     */
    public Produto(String id, String nome, double quantidadeMateriaPrimaPorUnidade,
            double qualidade, String sabor) {
        this.id = id;
        this.nome = nome;
        this.quantidadeMateriaPrimaPorUnidade = quantidadeMateriaPrimaPorUnidade;
        this.qualidade = qualidade;
        this.sabor = sabor;
        this.status = StatusBolacha.AGUARDANDO;
        this.probabilidadeFalhaAcumulada = 0.0;
        totalProdutosFabricados++;
    }

    /**
     * Aplica à bolacha o efeito de entrar na linha de produção. Cada tipo de
     * bolacha define o seu.
     */
    public abstract void processar();

    /**
     * Estima o tempo de produção de uma bolacha deste tipo.
     *
     * @return tempo de produção, em unidades de tempo da simulação
     */
    public abstract double calcularTempoProducao();

    /**
     * Informa o tipo da bolacha, que é a sua subclasse.
     *
     * @return nome do tipo, por exemplo "Bolacha Cracker"
     */
    public abstract String getTipo();

    /**
     * Cria uma bolacha nova do mesmo tipo e sabor, sem risco acumulado. Deixa
     * o gerenciador pedir cópias sem saber qual bolacha é.
     *
     * @param novoId código da cópia
     * @return bolacha nova, do mesmo tipo e sabor desta
     */
    public abstract Produto criarCopia(String novoId);

    /**
     * Informa o código da bolacha.
     *
     * @return código da bolacha
     */
    public String getId() {
        return id;
    }

    /**
     * Informa o nome da bolacha.
     *
     * @return nome para menus e relatórios
     */
    public String getNome() {
        return nome;
    }

    /**
     * Informa em que etapa da linha a bolacha está.
     *
     * @return estado atual da bolacha
     */
    public StatusBolacha getStatus() {
        return status;
    }

    /**
     * Muda o estado da bolacha. As máquinas usam isso para marcar corte,
     * assadura e o veredito da inspeção.
     *
     * @param status novo estado
     */
    public void setStatus(StatusBolacha status) {
        this.status = status;
    }

    /**
     * Informa quanta massa uma bolacha gasta.
     *
     * @return gramas de massa por bolacha
     */
    public double getQuantidadeMateriaPrimaPorUnidade() {
        return quantidadeMateriaPrimaPorUnidade;
    }

    /**
     * Informa a qualidade da bolacha. Quanto maior, mais rigorosa a inspeção.
     *
     * @return qualidade de 0.0 a 1.0
     */
    public double getQualidade() {
        return qualidade;
    }

    /**
     * Informa o sabor. Ele diz qual ingrediente a fábrica gasta, enquanto o
     * tipo (a subclasse) diz quanta massa.
     *
     * @return sabor, igual ao nome do ingrediente
     */
    public String getSabor() {
        return sabor;
    }

    /**
     * Soma um incremento ao risco acumulado. As máquinas chamam isso quando
     * erram com a bolacha. O total nunca passa de 1.0 (100%).
     *
     * @param incremento quanto somar, entre 0.0 e 1.0
     */
    public void aumentarProbabilidadeFalha(double incremento) {
        probabilidadeFalhaAcumulada += incremento;
        if (probabilidadeFalhaAcumulada > 1.0) {
            probabilidadeFalhaAcumulada = 1.0;
        }
    }

    /**
     * Informa a fornada que produziu a bolacha.
     *
     * @return número do lote, ou 0 se for só um molde
     */
    public int getLote() {
        return lote;
    }

    /**
     * Registra a fornada que produziu a bolacha.
     *
     * @param lote número do lote
     */
    public void setLote(int lote) {
        this.lote = lote;
    }

    /**
     * Informa o risco acumulado da bolacha.
     *
     * @return probabilidade de falha acumulada, de 0.0 a 1.0
     */
    public double getProbabilidadeFalhaAcumulada() {
        return probabilidadeFalhaAcumulada;
    }

    /**
     * Informa quantos objetos {@code Produto} já foram criados. Conta todos:
     * os moldes do catálogo e as cópias de cada fornada, aprovadas ou não.
     *
     * @return total de produtos criados desde que o programa começou
     */
    public static int getTotalProdutosFabricados() {
        return totalProdutosFabricados;
    }

    /**
     * {@inheritDoc}
     *
     * <p>O texto mostra código, nome, qualidade, risco acumulado (com a
     * classificação baixo, moderado ou alto) e o estado.</p>
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        return String.format("[%s] %s | qualidade %.2f | risco acumulado %.1f%% (%s) | status: %s",
                id, nome, qualidade, probabilidadeFalhaAcumulada * 100,
                classificarRisco(), status.getDescricao());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Para a bolacha, isso significa risco acumulado igual ou acima do
     * limiar de 15%.</p>
     */
    @Override
    public boolean precisaManutencao() {
        return probabilidadeFalhaAcumulada >= LIMIAR_RISCO;
    }

    // Traduz o numero do risco em uma palavra pro relatorio.
    private String classificarRisco() {
        if (probabilidadeFalhaAcumulada >= LIMIAR_RISCO) {
            return "alto";
        }
        if (probabilidadeFalhaAcumulada >= LIMIAR_RISCO / 2) {
            return "moderado";
        }
        return "baixo";
    }
}
