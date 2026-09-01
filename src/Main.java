public class Main {
    public static void main(String[] args) {
        Produto chocolate = new Produto("B001", "Bolacha de chocolate", 80);
        Produto aveia = new Produto("B002", "Bolacha de aveia", 70);
        Produto coco = new Produto("B003", "Bolacha de coco", 65);

        mostrarProduto(chocolate);
        mostrarProduto(aveia);
        mostrarProduto(coco);

        chocolate.processar();
        System.out.println("\nApós processamento:");
        mostrarProduto(chocolate);
    }

    private static void mostrarProduto(Produto produto) {
        System.out.println(produto.getId() + " - " + produto.getNome()
                + " | Demanda: " + produto.getDemandaMateriaPrima() + " g"
                + " | Status: " + produto.getStatus());
    }
}
