package br.unicamp.ic.mc322.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.Produto;

/**
 * Retrato do estado da planta num instante: tudo o que a persistência salva e
 * recupera. É só um pacote de dados, sem XML nem regras de negócio, e por isso
 * serve de ponte entre o {@link GerenciadorProducao} e o módulo de persistência
 * sem que um conheça o outro.
 *
 * <p>As listas devolvidas são imutáveis. Os objetos dentro delas são os da
 * própria planta: ao carregar, o gerenciador adota os objetos reconstruídos.</p>
 *
 * @author Diego Reis
 */
public class EstadoPlanta {
    private final Cenario cenario;
    private final double budget;
    private final int lotesProduzidos;
    private final MateriaPrima massa;
    private final List<MateriaPrima> ingredientes;
    private final List<Maquina> maquinas;
    private final List<Demanda> demandas;
    private final List<Produto> armazem;

    /**
     * Monta o retrato da planta.
     *
     * @param cenario         cenário de operação
     * @param budget          dinheiro em caixa, em reais
     * @param lotesProduzidos quantas fornadas já foram feitas
     * @param massa           a massa da casa
     * @param ingredientes    ingredientes de sabor da despensa
     * @param maquinas        máquinas da linha, na ordem em que a bolacha passa
     * @param demandas        demandas anotadas, com os respectivos estados
     * @param armazem         bolachas aprovadas guardadas no armazém
     */
    public EstadoPlanta(Cenario cenario, double budget, int lotesProduzidos, MateriaPrima massa,
            List<MateriaPrima> ingredientes, List<Maquina> maquinas, List<Demanda> demandas,
            List<Produto> armazem) {
        this.cenario = cenario;
        this.budget = budget;
        this.lotesProduzidos = lotesProduzidos;
        this.massa = massa;
        this.ingredientes = copiar(ingredientes);
        this.maquinas = copiar(maquinas);
        this.demandas = copiar(demandas);
        this.armazem = copiar(armazem);
    }

    private static <T> List<T> copiar(List<T> origem) {
        return Collections.unmodifiableList(new ArrayList<>(origem));
    }

    /**
     * Informa o cenário de operação.
     *
     * @return cenário da planta
     */
    public Cenario getCenario() {
        return cenario;
    }

    /**
     * Informa o dinheiro em caixa.
     *
     * @return budget, em reais
     */
    public double getBudget() {
        return budget;
    }

    /**
     * Informa quantas fornadas já foram feitas.
     *
     * @return total de lotes produzidos
     */
    public int getLotesProduzidos() {
        return lotesProduzidos;
    }

    /**
     * Informa a massa da casa.
     *
     * @return matéria-prima principal
     */
    public MateriaPrima getMassa() {
        return massa;
    }

    /**
     * Informa os ingredientes de sabor.
     *
     * @return lista imutável de ingredientes
     */
    public List<MateriaPrima> getIngredientes() {
        return ingredientes;
    }

    /**
     * Informa as máquinas da linha.
     *
     * @return lista imutável de máquinas, na ordem da linha
     */
    public List<Maquina> getMaquinas() {
        return maquinas;
    }

    /**
     * Informa as demandas anotadas.
     *
     * @return lista imutável de demandas
     */
    public List<Demanda> getDemandas() {
        return demandas;
    }

    /**
     * Informa as bolachas do armazém.
     *
     * @return lista imutável de bolachas aprovadas
     */
    public List<Produto> getArmazem() {
        return armazem;
    }
}
