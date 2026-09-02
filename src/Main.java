public class Main {
    public static void main(String[] args) {
        MateriaPrima massaChocolate = new MateriaPrima("MP001", "Massa de chocolate", 5000, "g", 200);
        MateriaPrima massaAveia = new MateriaPrima("MP002", "Massa de aveia", 5000, "g", 200);
        MateriaPrima massaCoco = new MateriaPrima("MP003", "Massa de coco", 5000, "g", 200);

        Produto chocolate = new Produto("B001", "Bolacha de chocolate", 80, massaChocolate);
        Produto aveia = new Produto("B002", "Bolacha de aveia", 70, massaAveia);
        Produto coco = new Produto("B003", "Bolacha de coco", 65, massaCoco);

        mostrarProduto(chocolate);
        mostrarProduto(aveia);
        mostrarProduto(coco);

        chocolate.processar();
        System.out.println("\nApós processamento:");
        mostrarProduto(chocolate);
    }

    private static void mostrarProduto(Produto produto) {
        System.out.println(produto.getId() + " - " + produto.getNome()
                + " | Massa: " + produto.getMateriaPrima().getNome()
                + " | Demanda: " + produto.getDemandaMateriaPrima() + " g"
                + " | Status: " + produto.getStatus());
    }
}
