import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Equipamento da linha de produção. Cada subclasse decide o que faz com a
 * bolacha em {@link #processar(Produto)}; esta classe cuida do que é comum a
 * todas: ligar e desligar, desgaste, falhas e diagnóstico.
 *
 * <p>Composição: a máquina é o Todo. Ela cria, no próprio construtor, as suas
 * partes ({@link Motor}, {@link PainelDeControle} e várias {@link Engrenagem}),
 * que não existem fora dela e nunca são entregues a quem usa a máquina. Quem
 * está fora fala só com a máquina, que repassa o trabalho às partes (desgaste,
 * reparo e diagnóstico).</p>
 *
 * <p>Regras de desgaste:</p>
 * <ul>
 *   <li>a saúde vai de 0 a 100 e cai um valor aleatório a cada fornada
 *       ({@link #registrarCiclo()}), até o teto de desgaste do cenário; as
 *       partes se desgastam junto;</li>
 *   <li>a chance de falha é inversamente proporcional à saúde: com metade da
 *       saúde, a máquina falha o dobro; painel descalibrado e engrenagens
 *       gastas somam ainda mais chance de falha;</li>
 *   <li>motor gasto encarece a operação;</li>
 *   <li>abaixo de 30 de saúde (ou com qualquer peça abaixo de 30) ela pede
 *       manutenção, e abaixo de 10 (ou com o motor quebrado) quebra e não opera
 *       mais até ser reparada.</li>
 * </ul>
 *
 * @author Pedro Magalhães
 * @author Diego Reis
 */
public abstract class Maquina implements Auditavel {
    private static final double SAUDE_MAXIMA = 100.0;
    // Abaixo disso a maquina pede manutencao.
    private static final double LIMIAR_MANUTENCAO = 30.0;
    // Abaixo disso a maquina para de vez ate ser reparada.
    private static final double LIMIAR_QUEBRA = 10.0;
    // Quantas engrenagens a maquina leva quando ninguem diz o contrario.
    private static final int ENGRENAGENS_PADRAO = 3;

    private Random random;
    private String nome;
    private boolean ligada;
    private double capacidadeMaxima;
    private double probabilidadeFalhaBase;
    private double custoOperacional;

    private double saude;
    private double fatorFalha;
    private double desgasteMaximo;
    private int ciclos;
    private int historicoFalhas;

    // Partes internas: nascem com a maquina e morrem com ela (composicao).
    // Nenhum getter as entrega pra fora.
    private final Motor motor;
    private final PainelDeControle painel;
    private final List<Engrenagem> engrenagens;

    /**
     * Cria a máquina desligada, com saúde cheia e sem cenário aplicado.
     *
     * @param nome              nome mostrado nos relatórios
     * @param capacidadeMaxima  quantas bolachas ela aguenta por fornada
     * @param probabilidadeFalha chance de falha com saúde cheia, de 0 a 1
     *                          (valores fora da faixa são ajustados)
     * @param custoOperacional  quanto ela cobra por bolacha processada, em reais
     *                          (com o motor em perfeito estado)
     */
    public Maquina(String nome, double capacidadeMaxima, double probabilidadeFalha, double custoOperacional) {
        this(nome, capacidadeMaxima, probabilidadeFalha, custoOperacional, ENGRENAGENS_PADRAO);
    }

    /**
     * Cria a máquina com o número de engrenagens escolhido. É aqui que as
     * partes nascem: motor, painel e engrenagens são criados por esta máquina e
     * por mais ninguém.
     *
     * @param nome                  nome mostrado nos relatórios
     * @param capacidadeMaxima      quantas bolachas ela aguenta por fornada
     * @param probabilidadeFalha    chance de falha com saúde cheia, de 0 a 1
     * @param custoOperacional      quanto ela cobra por bolacha processada, em reais
     * @param quantidadeEngrenagens quantas engrenagens ela tem (pelo menos 1)
     * @throws IllegalArgumentException se a quantidade de engrenagens for menor que 1
     */
    public Maquina(String nome, double capacidadeMaxima, double probabilidadeFalha,
            double custoOperacional, int quantidadeEngrenagens) {
        if (quantidadeEngrenagens < 1) {
            throw new IllegalArgumentException("Uma máquina precisa de pelo menos uma engrenagem.");
        }
        this.random = new Random();
        this.nome = nome;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ligada = false;
        this.probabilidadeFalhaBase = limitar(probabilidadeFalha, 0, 1);
        this.custoOperacional = custoOperacional;
        this.saude = SAUDE_MAXIMA;
        this.fatorFalha = 1.0;
        this.desgasteMaximo = 3.0;
        this.ciclos = 0;
        this.historicoFalhas = 0;

        this.motor = new Motor();
        this.painel = new PainelDeControle();
        this.engrenagens = new ArrayList<>();
        for (int i = 1; i <= quantidadeEngrenagens; i++) {
            this.engrenagens.add(new Engrenagem(i));
        }
    }

    /**
     * Faz o trabalho desta máquina na bolacha e atualiza o status dela.
     *
     * @param produto bolacha que está passando pela máquina
     * @return {@code true} se a máquina operou, {@code false} se ela estava
     *         desligada ou quebrada e a bolacha passou sem ser tocada
     */
    public abstract boolean processar(Produto produto);

    /**
     * Categoria da máquina para os relatórios.
     *
     * @return tipo da máquina, por exemplo "Processamento"
     */
    public abstract String getTipo();

    /**
     * Ajusta a máquina ao cenário: multiplicador de falha, teto de desgaste e
     * saúde inicial. O gerenciador chama isto quando a máquina entra na linha.
     *
     * @param cenario cenário de operação escolhido no início
     */
    public void aplicarCenario(Cenario cenario) {
        this.fatorFalha = cenario.getFatorFalha();
        this.desgasteMaximo = cenario.getDesgasteMaximo();
        this.saude = limitar(cenario.getSaudeInicialMaquinas(), 0, SAUDE_MAXIMA);
    }

    /** Liga a máquina. Ligada não basta para operar: ela também não pode estar quebrada. */
    public void ligar() {
        ligada = true;
    }

    /** Desliga a máquina. */
    public void desligar() {
        ligada = false;
    }

    /**
     * Diz se a máquina está ligada.
     *
     * @return {@code true} se estiver ligada
     */
    public boolean estaLigada() {
        return ligada;
    }

    /**
     * Diz se a máquina pode trabalhar agora. Máquina quebrada não roda nem
     * ligada.
     *
     * @return {@code true} se estiver ligada e não estiver quebrada
     */
    public boolean podeOperar() {
        return ligada && !estaQuebrada();
    }

    /**
     * Diz se a máquina quebrou: saúde abaixo de 10 ou motor quebrado.
     *
     * @return {@code true} se a máquina não pode mais operar
     */
    public boolean estaQuebrada() {
        return saude < LIMIAR_QUEBRA || motor.estaQuebrada();
    }

    /**
     * Registra uma fornada: tira da saúde um valor aleatório entre 0 e o teto
     * de desgaste do cenário, e manda cada parte se desgastar também. Chamado
     * uma vez por fornada.
     */
    public void registrarCiclo() {
        saude = limitar(saude - random.nextDouble() * desgasteMaximo, 0, SAUDE_MAXIMA);
        ciclos++;
        motor.registrarUso(desgasteMaximo);
        painel.registrarUso(desgasteMaximo);
        for (Engrenagem engrenagem : engrenagens) {
            engrenagem.registrarUso(desgasteMaximo);
        }
    }

    /**
     * Devolve a máquina e todas as suas partes a 100% de saúde. Quem cobra o
     * reparo é o gerenciador.
     */
    public void reparar() {
        saude = SAUDE_MAXIMA;
        motor.reparar();
        painel.reparar();
        for (Engrenagem engrenagem : engrenagens) {
            engrenagem.reparar();
        }
    }

    /**
     * Calcula a chance de falha agora. Com saúde cheia é a taxa base vezes o
     * fator do cenário; com metade da saúde, o dobro. A isso se soma o que o
     * painel descalibrado e as engrenagens gastas acrescentam.
     *
     * @return chance de falha, de 0 a 1 (1 se a saúde chegou a zero)
     */
    public double calcularProbabilidadeFalha() {
        if (saude <= 0) {
            return 1.0;
        }
        double chance = probabilidadeFalhaBase * fatorFalha * (SAUDE_MAXIMA / saude)
                + calcularAcrescimoDasPartes();
        return limitar(chance, 0, 1);
    }

    // Soma o quanto as partes gastas fazem a maquina errar a mais.
    private double calcularAcrescimoDasPartes() {
        double acrescimo = painel.getAcrescimoFalha();
        for (Engrenagem engrenagem : engrenagens) {
            acrescimo += engrenagem.getAcrescimoFalha();
        }
        return acrescimo;
    }

    /**
     * Traduz a saúde num estado do enum.
     *
     * @return {@link EstadoMaquina#QUEBRADA}, {@link EstadoMaquina#PRECISA_MANUTENCAO}
     *         ou {@link EstadoMaquina#OPERANDO}
     */
    public EstadoMaquina getEstado() {
        if (estaQuebrada()) {
            return EstadoMaquina.QUEBRADA;
        }
        if (precisaManutencao()) {
            return EstadoMaquina.PRECISA_MANUTENCAO;
        }
        return EstadoMaquina.OPERANDO;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Para máquina: saúde, chance de falha, falhas por fornada e estado,
     * seguidos de uma linha para cada parte (auditoria em cascata). O texto tem
     * várias linhas, e as das partes já vêm indentadas.</p>
     */
    @Override
    public String gerarRelatorioDiagnostico() {
        StringBuilder texto = new StringBuilder(String.format(
                "%-20s | saúde %5.1f%% | chance de falha %5.1f%% | %d falhas em %d fornadas | %s",
                nome, saude, calcularProbabilidadeFalha() * 100, historicoFalhas, ciclos,
                getEstado().getDescricao()));
        texto.append("\n  - ").append(motor.gerarRelatorioDiagnostico());
        texto.append("\n  - ").append(painel.gerarRelatorioDiagnostico());
        for (Engrenagem engrenagem : engrenagens) {
            texto.append("\n  - ").append(engrenagem.gerarRelatorioDiagnostico());
        }
        return texto.toString();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Para máquina: saúde abaixo de 30 ou qualquer parte pedindo
     * manutenção.</p>
     */
    @Override
    public boolean precisaManutencao() {
        if (saude < LIMIAR_MANUTENCAO || motor.precisaManutencao() || painel.precisaManutencao()) {
            return true;
        }
        for (Engrenagem engrenagem : engrenagens) {
            if (engrenagem.precisaManutencao()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Informa o nome da máquina.
     *
     * @return nome da máquina
     */
    public String getNome() {
        return nome;
    }

    /**
     * Informa quantas bolachas ela aguenta por fornada.
     *
     * @return capacidade por fornada
     */
    public double getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    /**
     * Informa quanto a máquina cobra para processar uma bolacha. Motor gasto
     * encarece: com ele totalmente gasto o preço sobe 50%.
     *
     * @return custo por bolacha, em reais
     */
    public double getCustoOperacional() {
        return custoOperacional * motor.getFatorCusto();
    }

    /**
     * Informa a saúde atual da máquina em si, sem contar as partes.
     *
     * @return saúde, de 0 a 100
     */
    public double getSaude() {
        return saude;
    }

    /**
     * Informa a saúde geral: a média entre a saúde da máquina e a de cada uma
     * das suas partes. É ela que mede o quanto falta para a máquina ficar nova,
     * e por isso o reparo é cobrado em cima dela.
     *
     * @return saúde geral, de 0 a 100
     */
    public double getSaudeGeral() {
        double soma = saude + motor.getSaude() + painel.getSaude();
        for (Engrenagem engrenagem : engrenagens) {
            soma += engrenagem.getSaude();
        }
        return soma / (3 + engrenagens.size());
    }

    /**
     * Sorteia se a máquina falhou nesta bolacha, usando a chance atual, e
     * conta a falha no histórico. O que a falha significa (corte torto,
     * bolacha queimada, julgamento errado) é a subclasse que decide.
     *
     * @return {@code true} se falhou
     */
    protected boolean verificarFalha() {
        boolean falhou = random.nextDouble() < calcularProbabilidadeFalha();
        if (falhou) {
            historicoFalhas++;
        }
        return falhou;
    }

    private static double limitar(double valor, double minimo, double maximo) {
        return Math.max(minimo, Math.min(maximo, valor));
    }
}
