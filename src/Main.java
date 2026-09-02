import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner teclado = new Scanner(System.in);
        Painel painel = new Painel(teclado);

        MateriaPrima massaChocolate = new MateriaPrima("MP001", "massa de chocolate", 5000, "g", 200);
        MateriaPrima massaAveia = new MateriaPrima("MP002", "massa de aveia", 5000, "g", 200);
        MateriaPrima massaCoco = new MateriaPrima("MP003", "massa de coco", 5000, "g", 200);
        MateriaPrima[] massas = { massaChocolate, massaAveia, massaCoco };

        // Os dois vetores andam juntos: bolachas[i] e feita de massas[i].
        Produto[] bolachas = {
            new Produto("B001", "Bolacha de chocolate", 80, massaChocolate),
            new Produto("B002", "Bolacha de aveia", 70, massaAveia),
            new Produto("B003", "Bolacha de coco", 65, massaCoco)
        };

        Maquina estampadeira = new Maquina("Estampadeira", 500);
        Esteira esteira = new Esteira(600);
        EstacaoInspecao inspecao = new EstacaoInspecao();

        painel.exibirIntroducao();
        painel.exibirEstoque(massas);
        painel.exibirCatalogo(bolachas);

        teclado.close();
    }
}
