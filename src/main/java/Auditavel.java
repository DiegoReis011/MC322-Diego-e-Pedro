/**
 * Contrato de qualquer componente da fábrica que sabe se diagnosticar. Máquinas
 * e bolachas não têm parentesco, mas ambas são auditáveis, e por isso o
 * relatório de auditoria consegue percorrer uma lista só com as duas.
 *
 * @author Diego Reis
 */
public interface Auditavel {

    /**
     * Descreve o estado de saúde ou qualidade do componente.
     *
     * @return texto de uma linha para o relatório de auditoria
     */
    String gerarRelatorioDiagnostico();

    /**
     * Diz se o componente pede intervenção (manutenção, no caso de máquina).
     *
     * @return {@code true} se precisa de atenção
     */
    boolean precisaManutencao();
}
