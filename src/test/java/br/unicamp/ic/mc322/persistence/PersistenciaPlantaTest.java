package br.unicamp.ic.mc322.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import br.unicamp.ic.mc322.PlantaDeTeste;
import br.unicamp.ic.mc322.exception.ArquivoInexistenteException;
import br.unicamp.ic.mc322.exception.DadosInvalidosException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.maquina.EstacaoInspecao;
import br.unicamp.ic.mc322.model.maquina.Estampadeira;
import br.unicamp.ic.mc322.model.maquina.Forno;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.service.EstadoPlanta;
import br.unicamp.ic.mc322.service.GerenciadorProducao;

/**
 * Persistência em XML: salvar, carregar, preservação de dados e relações e
 * tratamento de arquivos ausentes, vazios, corrompidos ou incompatíveis. Todo
 * teste usa uma pasta temporária própria, nunca o arquivo real da aplicação.
 */
class PersistenciaPlantaTest {
    @TempDir
    Path pasta;

    private Path arquivo;
    private PersistenciaPlanta persistencia;
    private GerenciadorProducao fabrica;

    @BeforeEach
    void preparar() {
        arquivo = pasta.resolve("planta.xml");
        persistencia = new PersistenciaPlanta(arquivo);
        fabrica = PlantaDeTeste.novoGerenciador();
    }

    // Deixa a planta com estado variado: demandas em estados diferentes,
    // compra feita, uma fornada produzida e maquinas desgastadas.
    private void movimentarPlanta() {
        String cracker = PlantaDeTeste.nomeDaBolacha(fabrica, 2);
        String amanteigada = PlantaDeTeste.nomeDaBolacha(fabrica, 0);
        String tradicional = PlantaDeTeste.nomeDaBolacha(fabrica, 1);
        fabrica.registrarDemanda(cracker, 20);
        fabrica.executarProximaProducao();
        fabrica.registrarDemanda(amanteigada, 1000);
        try {
            fabrica.executarProximaProducao();
        } catch (br.unicamp.ic.mc322.exception.PlantaException esperado) {
            // Falta de massa: a demanda de amanteigada vira CANCELADA.
        }
        fabrica.registrarDemanda(tradicional, 12);
        fabrica.comprarMateriaPrima(fabrica.getInsumos().get(2), 150);
        fabrica.getMaquinas().get(1).restaurarEstado(55.5, 7, 2, 44.0, 33.0, Arrays.asList(22.0, 66.0, 88.0));
    }

    private String lerArquivo() throws IOException {
        return Files.readString(arquivo, StandardCharsets.UTF_8);
    }

    // Salva uma planta valida e troca um trecho do XML, para simular arquivo estragado.
    private void salvarEAlterar(String antigo, String novo) throws IOException {
        persistencia.salvar(fabrica.exportarEstado());
        String texto = lerArquivo();
        assertTrue(texto.contains(antigo), "o XML salvo deveria conter: " + antigo);
        Files.writeString(arquivo, texto.replace(antigo, novo), StandardCharsets.UTF_8);
    }

    private void compararMaquinas(Maquina esperada, Maquina lida) {
        assertEquals(esperada.getClass(), lida.getClass());
        assertEquals(esperada.getNome(), lida.getNome());
        assertEquals(esperada.getCapacidadeMaxima(), lida.getCapacidadeMaxima(), 1e-9);
        assertEquals(esperada.getProbabilidadeFalhaBase(), lida.getProbabilidadeFalhaBase(), 1e-9);
        assertEquals(esperada.getCustoOperacionalBase(), lida.getCustoOperacionalBase(), 1e-9);
        assertEquals(esperada.getSaude(), lida.getSaude(), 1e-9);
        assertEquals(esperada.getCiclos(), lida.getCiclos());
        assertEquals(esperada.getHistoricoFalhas(), lida.getHistoricoFalhas());
        assertEquals(esperada.getSaudeMotor(), lida.getSaudeMotor(), 1e-9);
        assertEquals(esperada.getSaudePainel(), lida.getSaudePainel(), 1e-9);
        assertEquals(esperada.getSaudesEngrenagens(), lida.getSaudesEngrenagens());
    }

    private void compararMateria(MateriaPrima esperada, MateriaPrima lida) {
        assertEquals(esperada.getId(), lida.getId());
        assertEquals(esperada.getNome(), lida.getNome());
        assertEquals(esperada.getQuantidade(), lida.getQuantidade(), 1e-9);
        assertEquals(esperada.getUnidade(), lida.getUnidade());
        assertEquals(esperada.getQuantidadeMinima(), lida.getQuantidadeMinima(), 1e-9);
        assertEquals(esperada.getCustoPorUnidade(), lida.getCustoPorUnidade(), 1e-9);
    }

    // ---------------- salvar e carregar ----------------

    @Test
    @DisplayName("salvar cria um arquivo XML com a raiz <planta>")
    void salvarCriaArquivo() throws IOException {
        assertFalse(persistencia.existe());
        persistencia.salvar(fabrica.exportarEstado());
        assertTrue(persistencia.existe());
        String texto = lerArquivo();
        assertTrue(texto.startsWith("<?xml"));
        assertTrue(texto.contains("<planta"));
        assertTrue(texto.contains("<engrenagem "));
    }

    @Test
    @DisplayName("salvar e carregar preserva budget, cenário, lotes e despensa")
    void preservaDadosGerais() {
        movimentarPlanta();
        EstadoPlanta salvo = fabrica.exportarEstado();
        persistencia.salvar(salvo);

        EstadoPlanta lido = persistencia.carregar();

        assertEquals(salvo.getCenario(), lido.getCenario());
        assertEquals(salvo.getBudget(), lido.getBudget(), 1e-9);
        assertEquals(salvo.getLotesProduzidos(), lido.getLotesProduzidos());
        compararMateria(salvo.getMassa(), lido.getMassa());
        assertEquals(salvo.getIngredientes().size(), lido.getIngredientes().size());
        for (int i = 0; i < salvo.getIngredientes().size(); i++) {
            compararMateria(salvo.getIngredientes().get(i), lido.getIngredientes().get(i));
        }
    }

    @Test
    @DisplayName("preserva as máquinas, a ordem da linha e a composição (motor, painel, engrenagens)")
    void preservaMaquinasEPartes() {
        movimentarPlanta();
        EstadoPlanta salvo = fabrica.exportarEstado();
        persistencia.salvar(salvo);

        List<Maquina> lidas = persistencia.carregar().getMaquinas();

        assertEquals(3, lidas.size());
        assertEquals(Estampadeira.class, lidas.get(0).getClass());
        assertEquals(Forno.class, lidas.get(1).getClass());
        assertEquals(EstacaoInspecao.class, lidas.get(2).getClass());
        for (int i = 0; i < 3; i++) {
            compararMaquinas(salvo.getMaquinas().get(i), lidas.get(i));
            assertEquals(3, lidas.get(i).getQuantidadeEngrenagens());
        }
        Maquina forno = lidas.get(1);
        assertEquals(55.5, forno.getSaude(), 1e-9);
        assertEquals(44.0, forno.getSaudeMotor(), 1e-9);
        assertEquals(33.0, forno.getSaudePainel(), 1e-9);
        assertEquals(Arrays.asList(22.0, 66.0, 88.0), forno.getSaudesEngrenagens());
    }

    @Test
    @DisplayName("preserva as demandas com quantidade, custo e StatusDemanda")
    void preservaDemandas() {
        movimentarPlanta();
        EstadoPlanta salvo = fabrica.exportarEstado();
        persistencia.salvar(salvo);

        List<Demanda> lidas = persistencia.carregar().getDemandas();

        assertEquals(3, lidas.size());
        for (int i = 0; i < 3; i++) {
            Demanda esperada = salvo.getDemandas().get(i);
            assertEquals(esperada.getTipoProduto(), lidas.get(i).getTipoProduto());
            assertEquals(esperada.getQuantidadeProdutos(), lidas.get(i).getQuantidadeProdutos());
            assertEquals(esperada.getStatus(), lidas.get(i).getStatus());
            assertEquals(esperada.getCustoUnitarioEstimado(), lidas.get(i).getCustoUnitarioEstimado(), 1e-9);
        }
        assertEquals(StatusDemanda.CONCLUIDA, lidas.get(0).getStatus());
        assertEquals(StatusDemanda.CANCELADA, lidas.get(1).getStatus());
        assertEquals(StatusDemanda.PENDENTE, lidas.get(2).getStatus());
    }

    @Test
    @DisplayName("preserva as bolachas do armazém")
    void preservaArmazem() {
        movimentarPlanta();
        EstadoPlanta salvo = fabrica.exportarEstado();
        persistencia.salvar(salvo);

        List<Produto> lidas = persistencia.carregar().getArmazem();

        assertEquals(salvo.getArmazem().size(), lidas.size());
        for (int i = 0; i < lidas.size(); i++) {
            Produto esperado = salvo.getArmazem().get(i);
            assertEquals(esperado.getClass(), lidas.get(i).getClass());
            assertEquals(esperado.getId(), lidas.get(i).getId());
            assertEquals(esperado.getStatus(), lidas.get(i).getStatus());
            assertEquals(esperado.getLote(), lidas.get(i).getLote());
            assertEquals(esperado.getProbabilidadeFalhaAcumulada(),
                    lidas.get(i).getProbabilidadeFalhaAcumulada(), 1e-9);
        }
    }

    @Test
    @DisplayName("ciclo completo: salvar, carregar em outra fábrica e continuar produzindo")
    void cicloCompleto() {
        movimentarPlanta();
        persistencia.salvar(fabrica.exportarEstado());

        GerenciadorProducao outra = PlantaDeTeste.novoGerenciador(Cenario.APOCALIPTICO);
        outra.restaurarEstado(persistencia.carregar());

        assertEquals(fabrica.getBudget(), outra.getBudget(), 1e-9);
        assertEquals(Cenario.IDEAL, outra.getCenario());
        assertEquals(fabrica.getDemandas().size(), outra.getDemandas().size());
        assertEquals(fabrica.getInsumos().get(2).getQuantidade(),
                outra.getInsumos().get(2).getQuantidade(), 1e-9);
        // A fabrica recuperada continua operando de verdade.
        outra.getMaquinas().get(1).reparar();
        double budgetAntes = outra.getBudget();
        outra.executarProximaProducao();
        assertTrue(outra.getBudget() < budgetAntes);
    }

    @Test
    @DisplayName("carregar substitui o estado atual, sem juntar com o que já existia")
    void carregarSubstitui() {
        fabrica.registrarDemanda(PlantaDeTeste.nomeDaBolacha(fabrica, 2), 4);
        persistencia.salvar(fabrica.exportarEstado());

        fabrica.registrarDemanda(PlantaDeTeste.nomeDaBolacha(fabrica, 1), 9);
        fabrica.comprarMateriaPrima(fabrica.getInsumos().get(0), 500);
        assertEquals(2, fabrica.getDemandas().size());

        fabrica.restaurarEstado(persistencia.carregar());

        assertEquals(1, fabrica.getDemandas().size());
        assertEquals(10000.0, fabrica.getBudget(), 1e-9);
        assertEquals(5000.0, fabrica.getInsumos().get(0).getQuantidade(), 1e-9);
        assertEquals(3, fabrica.getMaquinas().size());
    }

    @Test
    @DisplayName("salvar de novo atualiza o arquivo e não deixa lixo na pasta")
    void atualizar() {
        persistencia.salvar(fabrica.exportarEstado());
        fabrica.comprarMateriaPrima(fabrica.getInsumos().get(0), 100);
        persistencia.salvar(fabrica.exportarEstado());

        EstadoPlanta lido = persistencia.carregar();

        assertEquals(10000.0 - 30.0, lido.getBudget(), 1e-9);
        assertEquals(5100.0, lido.getMassa().getQuantidade(), 1e-9);
        assertEquals(1, pasta.toFile().list().length);
    }

    @Test
    @DisplayName("salvar cria a pasta do arquivo se ela ainda não existe")
    void criaPasta() {
        PersistenciaPlanta aninhada = new PersistenciaPlanta(pasta.resolve("dados").resolve("planta.xml"));
        aninhada.salvar(fabrica.exportarEstado());
        assertTrue(aninhada.existe());
    }

    // ---------------- erros ----------------

    @Test
    @DisplayName("arquivo inexistente lança ArquivoInexistenteException")
    void arquivoInexistente() {
        assertFalse(persistencia.existe());
        ArquivoInexistenteException erro =
                assertThrows(ArquivoInexistenteException.class, () -> persistencia.carregar());
        assertTrue(erro.getMessage().contains("planta.xml"));
    }

    @Test
    @DisplayName("arquivo vazio lança PersistenciaException")
    void arquivoVazio() throws IOException {
        Files.createFile(arquivo);
        PersistenciaException erro = assertThrows(PersistenciaException.class, () -> persistencia.carregar());
        assertTrue(erro.getMessage().contains("vazio"));
    }

    @Test
    @DisplayName("texto que não é XML lança PersistenciaException")
    void xmlMalformado() throws IOException {
        Files.writeString(arquivo, "isto nao e xml <planta", StandardCharsets.UTF_8);
        PersistenciaException erro = assertThrows(PersistenciaException.class, () -> persistencia.carregar());
        assertFalse(erro instanceof DadosInvalidosException);
        assertTrue(erro.getMessage().contains("XML"));
    }

    @Test
    @DisplayName("XML com DOCTYPE é recusado (arquivo de fora não puxa entidades externas)")
    void doctypeRecusado() throws IOException {
        Files.writeString(arquivo, "<?xml version=\"1.0\"?><!DOCTYPE planta [<!ENTITY x \"y\">]><planta versao=\"1\"/>",
                StandardCharsets.UTF_8);
        assertThrows(PersistenciaException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("XML válido que não é uma planta lança DadosInvalidosException")
    void raizErrada() throws IOException {
        Files.writeString(arquivo, "<outraCoisa versao=\"1\"/>", StandardCharsets.UTF_8);
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("versão desconhecida do arquivo é recusada")
    void versaoDesconhecida() throws IOException {
        salvarEAlterar("versao=\"1\"", "versao=\"99\"");
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("elemento obrigatório ausente é recusado")
    void faltaElemento() throws IOException {
        salvarEAlterar("<budget>10000.0</budget>", "");
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
        assertTrue(erro.getMessage().contains("budget"));
    }

    @Test
    @DisplayName("saúde de máquina fora de 0 a 100 é recusada")
    void saudeForaDaFaixa() throws IOException {
        salvarEAlterar("<motor saude=\"100.0\"/>", "<motor saude=\"150.0\"/>");
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("número ilegível é recusado com mensagem clara")
    void numeroIlegivel() throws IOException {
        salvarEAlterar("<budget>10000.0</budget>", "<budget>muito dinheiro</budget>");
        DadosInvalidosException erro = assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
        assertTrue(erro.getMessage().contains("budget"));
    }

    @Test
    @DisplayName("budget negativo é recusado")
    void budgetNegativo() throws IOException {
        salvarEAlterar("<budget>10000.0</budget>", "<budget>-5.0</budget>");
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("estoque negativo é recusado")
    void estoqueNegativo() throws IOException {
        salvarEAlterar("<quantidade>5000.0</quantidade>", "<quantidade>-1.0</quantidade>");
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("cenário e status desconhecidos são recusados")
    void enumsDesconhecidos() throws IOException {
        salvarEAlterar("<cenario>IDEAL</cenario>", "<cenario>MARCIANO</cenario>");
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());

        fabrica.registrarDemanda(PlantaDeTeste.nomeDaBolacha(fabrica, 2), 3);
        salvarEAlterar("status=\"PENDENTE\"", "status=\"ATRASADA\"");
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("tipo de máquina desconhecido é recusado")
    void tipoDeMaquinaDesconhecido() throws IOException {
        salvarEAlterar("tipo=\"Forno\"", "tipo=\"Liquidificador\"");
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("máquina sem nenhuma engrenagem é recusada")
    void semEngrenagens() throws IOException {
        persistencia.salvar(fabrica.exportarEstado());
        String texto = lerArquivo();
        String semEngrenagens = texto.replaceAll("<engrenagem [^>]*/>", "");
        Files.writeString(arquivo, semEngrenagens, StandardCharsets.UTF_8);
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("arquivo sem a massa da casa é recusado")
    void semMassa() throws IOException {
        salvarEAlterar("funcao=\"massa\"", "funcao=\"ingrediente\"");
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("demanda de bolacha que a fábrica não faz: lê, mas a fábrica recusa e fica intacta")
    void demandaDeBolachaDesconhecida() throws IOException {
        fabrica.registrarDemanda(PlantaDeTeste.nomeDaBolacha(fabrica, 2), 3);
        salvarEAlterar("tipoProduto=\"" + PlantaDeTeste.nomeDaBolacha(fabrica, 2) + "\"",
                "tipoProduto=\"Bolacha de marte\"");
        EstadoPlanta lido = persistencia.carregar();

        assertThrows(DadosInvalidosException.class, () -> fabrica.restaurarEstado(lido));

        assertEquals(1, fabrica.getDemandas().size());
        assertEquals(PlantaDeTeste.nomeDaBolacha(fabrica, 2), fabrica.getDemandas().get(0).getTipoProduto());
    }

    @Test
    @DisplayName("quantidade de demanda inválida no arquivo é recusada pelas regras do domínio")
    void demandaComQuantidadeInvalida() throws IOException {
        fabrica.registrarDemanda(PlantaDeTeste.nomeDaBolacha(fabrica, 2), 3);
        salvarEAlterar("quantidade=\"3\"", "quantidade=\"0\"");
        assertThrows(DadosInvalidosException.class, () -> persistencia.carregar());
    }

    @Test
    @DisplayName("falha ao carregar preserva a planta em uso")
    void falhaPreservaEstado() throws IOException {
        fabrica.registrarDemanda(PlantaDeTeste.nomeDaBolacha(fabrica, 2), 8);
        double budgetAntes = fabrica.getBudget();
        Files.writeString(arquivo, "<planta versao=\"1\"><cenario>IDEAL</cenario>", StandardCharsets.UTF_8);

        assertThrows(PersistenciaException.class, () -> fabrica.restaurarEstado(persistencia.carregar()));

        assertEquals(budgetAntes, fabrica.getBudget(), 1e-9);
        assertEquals(1, fabrica.getDemandas().size());
        assertEquals(3, fabrica.getMaquinas().size());
    }

    @Test
    @DisplayName("falha de escrita lança PersistenciaException e não deixa arquivo temporário")
    void falhaDeEscrita() throws IOException {
        Path umArquivo = pasta.resolve("umArquivo.txt");
        Files.writeString(umArquivo, "oi", StandardCharsets.UTF_8);
        // A "pasta" do save e um arquivo comum: criar o diretorio e impossivel.
        PersistenciaPlanta impossivel = new PersistenciaPlanta(umArquivo.resolve("planta.xml"));

        PersistenciaException erro = assertThrows(PersistenciaException.class,
                () -> impossivel.salvar(fabrica.exportarEstado()));

        assertTrue(erro.getMessage().contains("salvar"));
        assertEquals(1, pasta.toFile().list().length);
    }

    @Test
    @DisplayName("falha ao salvar não estraga o save anterior")
    void saveAnteriorSobrevive() throws IOException {
        persistencia.salvar(fabrica.exportarEstado());
        String antes = lerArquivo();
        assertThrows(PersistenciaException.class, () -> persistencia.salvar(null));
        assertEquals(antes, lerArquivo());
        assertEquals(10000.0, persistencia.carregar().getBudget(), 1e-9);
    }
}
