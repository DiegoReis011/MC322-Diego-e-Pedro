package br.unicamp.ic.mc322;

import java.util.Scanner;

import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.service.FabricaNotBiscoito;
import br.unicamp.ic.mc322.service.GerenciadorProducao;
import br.unicamp.ic.mc322.strategy.EstrategiaFilaDoForno;
import br.unicamp.ic.mc322.ui.Painel;

/**
 * Apoio dos testes: monta uma fábrica nova a cada chamada, sem ler o teclado
 * (o painel recebe uma entrada vazia), para que cada teste prepare os seus
 * próprios dados e nenhum dependa de outro.
 */
public final class PlantaDeTeste {

    private PlantaDeTeste() {
    }

    /**
     * Cria uma fábrica padrão no cenário pedido.
     *
     * @param cenario cenário de operação
     * @return gerenciador novo, com linha, despensa e catálogo prontos
     */
    public static GerenciadorProducao novoGerenciador(Cenario cenario) {
        return FabricaNotBiscoito.criarGerenciador(new Painel(new Scanner("")), cenario,
                new EstrategiaFilaDoForno());
    }

    /**
     * Cria uma fábrica padrão no cenário ideal.
     *
     * @return gerenciador novo
     */
    public static GerenciadorProducao novoGerenciador() {
        return novoGerenciador(Cenario.IDEAL);
    }

    /**
     * Nome da bolacha do catálogo na posição pedida (0 amanteigada, 1 tradicional, 2 cracker).
     *
     * @param gerenciador fábrica
     * @param posicao     posição no catálogo
     * @return nome da bolacha, igual ao usado nas demandas
     */
    public static String nomeDaBolacha(GerenciadorProducao gerenciador, int posicao) {
        return gerenciador.getCatalogo().get(posicao).getNome();
    }
}
