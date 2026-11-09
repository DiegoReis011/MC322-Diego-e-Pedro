package br.unicamp.ic.mc322.service;

import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.maquina.EstacaoInspecao;
import br.unicamp.ic.mc322.model.maquina.Estampadeira;
import br.unicamp.ic.mc322.model.maquina.Forno;
import br.unicamp.ic.mc322.model.produto.BolachaAmanteigada;
import br.unicamp.ic.mc322.model.produto.BolachaCracker;
import br.unicamp.ic.mc322.model.produto.BolachaTradicional;
import br.unicamp.ic.mc322.strategy.EstrategiaProducao;
import br.unicamp.ic.mc322.ui.Painel;

/**
 * Monta a fábrica padrão da NotBiscoito: a massa, os três ingredientes, as três
 * máquinas da linha e o catálogo de bolachas. É o que a aplicação usa quando
 * não há planta salva, e o que os testes usam para ter uma fábrica pronta sem
 * passar pelo menu.
 *
 * @author Pedro Magalhães
 */
public final class FabricaNotBiscoito {

    // So metodos estaticos; ninguem cria uma fabrica de fabricas.
    private FabricaNotBiscoito() {
    }

    /**
     * Cria uma fábrica nova com os dados padrão do projeto.
     *
     * @param painel            painel que mostra o andamento das operações
     * @param cenario           cenário de operação escolhido
     * @param estrategiaInicial estratégia usada até o operador trocar
     * @return o gerenciador com a linha, a despensa e o catálogo prontos
     */
    public static GerenciadorProducao criarGerenciador(Painel painel, Cenario cenario,
            EstrategiaProducao estrategiaInicial) {
        // A massa e comum a todas: o tipo da bolacha decide quanta massa vai.
        MateriaPrima massa = new MateriaPrima("MP001", "massa da casa", 5000, "g", 200, 0.30);

        GerenciadorProducao gerenciador =
                new GerenciadorProducao(massa, cenario, estrategiaInicial, painel);

        // O nome tem que ser igual ao sabor da bolacha: e por ele que a
        // linha acha o insumo.
        gerenciador.adicionarIngrediente(ingrediente("IN001", "chocolate", 1.20));
        gerenciador.adicionarIngrediente(ingrediente("IN002", "coco", 0.80));
        gerenciador.adicionarIngrediente(ingrediente("IN003", "aveia", 0.50));

        // A ordem aqui e a ordem em que a bolacha passa pela linha.
        gerenciador.adicionarMaquina(new Estampadeira());
        gerenciador.adicionarMaquina(new Forno());
        gerenciador.adicionarMaquina(new EstacaoInspecao());

        gerenciador.adicionarAoCatalogo(new BolachaAmanteigada("B001", "chocolate"));
        gerenciador.adicionarAoCatalogo(new BolachaTradicional("B002", "aveia"));
        gerenciador.adicionarAoCatalogo(new BolachaCracker("B003", "coco"));

        return gerenciador;
    }

    // Ingrediente de sabor so difere no nome e no preco.
    private static MateriaPrima ingrediente(String id, String nome, double custoPorGrama) {
        return new MateriaPrima(id, nome, 1000, "g", 100, custoPorGrama);
    }
}
