package br.unicamp.ic.mc322.persistence;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import br.unicamp.ic.mc322.exception.ArquivoInexistenteException;
import br.unicamp.ic.mc322.exception.DadosInvalidosException;
import br.unicamp.ic.mc322.exception.PersistenciaException;
import br.unicamp.ic.mc322.exception.PlantaException;
import br.unicamp.ic.mc322.model.Cenario;
import br.unicamp.ic.mc322.model.Demanda;
import br.unicamp.ic.mc322.model.MateriaPrima;
import br.unicamp.ic.mc322.model.StatusDemanda;
import br.unicamp.ic.mc322.model.maquina.EstacaoInspecao;
import br.unicamp.ic.mc322.model.maquina.Estampadeira;
import br.unicamp.ic.mc322.model.maquina.Forno;
import br.unicamp.ic.mc322.model.maquina.Maquina;
import br.unicamp.ic.mc322.model.produto.BolachaAmanteigada;
import br.unicamp.ic.mc322.model.produto.BolachaCracker;
import br.unicamp.ic.mc322.model.produto.BolachaTradicional;
import br.unicamp.ic.mc322.model.produto.Produto;
import br.unicamp.ic.mc322.model.produto.StatusBolacha;
import br.unicamp.ic.mc322.service.EstadoPlanta;

/**
 * Salva e carrega o estado da planta num arquivo XML. É a única classe do
 * projeto que conhece o formato do arquivo: o domínio só expõe os dados, e o
 * {@link EstadoPlanta} leva esses dados de um lado para o outro.
 *
 * <p>Estrutura do arquivo (a hierarquia espelha a composição da Tarefa 5):</p>
 * <pre>
 * &lt;planta versao="1"&gt;
 *   &lt;cenario&gt;IDEAL&lt;/cenario&gt;  &lt;budget&gt;...&lt;/budget&gt;  &lt;lotes&gt;...&lt;/lotes&gt;
 *   &lt;despensa&gt;  &lt;materiaPrima funcao="massa|ingrediente" .../&gt;  &lt;/despensa&gt;
 *   &lt;linha&gt;
 *     &lt;maquina tipo="Forno" ...&gt;
 *       &lt;motor saude="..."/&gt;  &lt;painelDeControle saude="..."/&gt;
 *       &lt;engrenagens&gt;  &lt;engrenagem numero="1" saude="..."/&gt;  &lt;/engrenagens&gt;
 *     &lt;/maquina&gt;
 *   &lt;/linha&gt;
 *   &lt;demandas&gt;  &lt;demanda tipoProduto="..." status="PENDENTE" .../&gt;  &lt;/demandas&gt;
 *   &lt;armazem&gt;  &lt;bolacha tipo="BolachaCracker" .../&gt;  &lt;/armazem&gt;
 * &lt;/planta&gt;
 * </pre>
 *
 * <p>Garantias:</p>
 * <ul>
 *   <li>salvar escreve num arquivo temporário e só então substitui o arquivo
 *       real, então uma falha no meio nunca estraga o save anterior;</li>
 *   <li>carregar reconstrói e valida tudo antes de devolver o estado, e não
 *       mexe em nada da planta: quem decide adotar o estado é o gerenciador;</li>
 *   <li>qualquer problema vira uma {@link PersistenciaException} com uma
 *       mensagem que o operador entende.</li>
 * </ul>
 *
 * @author Diego Reis
 */
public class PersistenciaPlanta {
    private static final String VERSAO = "1";
    private static final String NOME_LINHA = "da Casa";

    private final Path arquivo;

    /**
     * Cria a persistência ligada a um arquivo. O arquivo não precisa existir
     * ainda: ele nasce no primeiro {@link #salvar(EstadoPlanta)}.
     *
     * @param arquivo caminho do arquivo XML da planta
     */
    public PersistenciaPlanta(Path arquivo) {
        this.arquivo = arquivo;
    }

    /**
     * Informa o arquivo usado.
     *
     * @return caminho do arquivo XML
     */
    public Path getArquivo() {
        return arquivo;
    }

    /**
     * Diz se já existe um arquivo salvo.
     *
     * @return {@code true} se o arquivo existe
     */
    public boolean existe() {
        return Files.isRegularFile(arquivo);
    }

    // ---------------------------------------------------------------
    // Salvar
    // ---------------------------------------------------------------

    /**
     * Grava o estado da planta no arquivo, substituindo o save anterior se
     * houver. A gravação é feita num arquivo temporário e movida para o lugar
     * no fim, então o save antigo só some se o novo ficou completo.
     *
     * @param estado estado a salvar
     * @throws PersistenciaException se não for possível escrever o arquivo
     */
    public void salvar(EstadoPlanta estado) {
        if (estado == null) {
            throw new PersistenciaException("Não há estado de planta para salvar.");
        }
        Document documento = montarDocumento(estado);
        Path pasta = arquivo.toAbsolutePath().getParent();
        Path temporario = null;
        try {
            Files.createDirectories(pasta);
            temporario = Files.createTempFile(pasta, "planta", ".tmp");
            escrever(documento, temporario);
            substituir(temporario, arquivo);
        } catch (IOException | TransformerException erro) {
            apagarSilenciosamente(temporario);
            throw new PersistenciaException("Não foi possível salvar a planta em " + arquivo
                    + ": " + erro.getMessage(), erro);
        }
    }

    private void escrever(Document documento, Path destino) throws TransformerException, IOException {
        TransformerFactory fabrica = TransformerFactory.newInstance();
        Transformer transformador = fabrica.newTransformer();
        transformador.setOutputProperty(OutputKeys.INDENT, "yes");
        transformador.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformador.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        try (var saida = Files.newOutputStream(destino)) {
            transformador.transform(new DOMSource(documento), new StreamResult(saida));
        }
    }

    private void substituir(Path temporario, Path destino) throws IOException {
        try {
            Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException erro) {
            Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void apagarSilenciosamente(Path caminho) {
        if (caminho != null) {
            try {
                Files.deleteIfExists(caminho);
            } catch (IOException ignorado) {
                // Sobrou um .tmp: nao vale esconder o erro original por isso.
            }
        }
    }

    private Document montarDocumento(EstadoPlanta estado) {
        Document documento = novoDocumento();
        Element raiz = documento.createElement("planta");
        raiz.setAttribute("versao", VERSAO);
        documento.appendChild(raiz);

        adicionarTexto(documento, raiz, "cenario", estado.getCenario().name());
        adicionarTexto(documento, raiz, "budget", String.valueOf(estado.getBudget()));
        adicionarTexto(documento, raiz, "lotes", String.valueOf(estado.getLotesProduzidos()));

        Element despensa = documento.createElement("despensa");
        raiz.appendChild(despensa);
        despensa.appendChild(elementoMateriaPrima(documento, estado.getMassa(), "massa"));
        for (MateriaPrima ingrediente : estado.getIngredientes()) {
            despensa.appendChild(elementoMateriaPrima(documento, ingrediente, "ingrediente"));
        }

        Element linha = documento.createElement("linha");
        raiz.appendChild(linha);
        for (Maquina maquina : estado.getMaquinas()) {
            linha.appendChild(elementoMaquina(documento, maquina));
        }

        Element demandas = documento.createElement("demandas");
        raiz.appendChild(demandas);
        for (Demanda demanda : estado.getDemandas()) {
            Element elemento = documento.createElement("demanda");
            elemento.setAttribute("tipoProduto", demanda.getTipoProduto());
            elemento.setAttribute("quantidade", String.valueOf(demanda.getQuantidadeProdutos()));
            elemento.setAttribute("status", demanda.getStatus().name());
            elemento.setAttribute("custoUnitarioEstimado",
                    String.valueOf(demanda.getCustoUnitarioEstimado()));
            demandas.appendChild(elemento);
        }

        Element armazem = documento.createElement("armazem");
        raiz.appendChild(armazem);
        for (Produto bolacha : estado.getArmazem()) {
            Element elemento = documento.createElement("bolacha");
            elemento.setAttribute("tipo", bolacha.getClass().getSimpleName());
            elemento.setAttribute("id", bolacha.getId());
            elemento.setAttribute("sabor", bolacha.getSabor());
            elemento.setAttribute("status", bolacha.getStatus().name());
            elemento.setAttribute("lote", String.valueOf(bolacha.getLote()));
            elemento.setAttribute("risco", String.valueOf(bolacha.getProbabilidadeFalhaAcumulada()));
            armazem.appendChild(elemento);
        }
        return documento;
    }

    private Element elementoMateriaPrima(Document documento, MateriaPrima materia, String funcao) {
        Element elemento = documento.createElement("materiaPrima");
        elemento.setAttribute("id", materia.getId());
        elemento.setAttribute("funcao", funcao);
        adicionarTexto(documento, elemento, "nome", materia.getNome());
        adicionarTexto(documento, elemento, "quantidade", String.valueOf(materia.getQuantidade()));
        adicionarTexto(documento, elemento, "unidade", materia.getUnidade());
        adicionarTexto(documento, elemento, "quantidadeMinima",
                String.valueOf(materia.getQuantidadeMinima()));
        adicionarTexto(documento, elemento, "custoPorUnidade",
                String.valueOf(materia.getCustoPorUnidade()));
        return elemento;
    }

    // A maquina so entrega numeros: as partes continuam escondidas dentro dela.
    private Element elementoMaquina(Document documento, Maquina maquina) {
        Element elemento = documento.createElement("maquina");
        elemento.setAttribute("tipo", maquina.getClass().getSimpleName());
        adicionarTexto(documento, elemento, "capacidadeMaxima",
                String.valueOf(maquina.getCapacidadeMaxima()));
        adicionarTexto(documento, elemento, "probabilidadeFalhaBase",
                String.valueOf(maquina.getProbabilidadeFalhaBase()));
        adicionarTexto(documento, elemento, "custoOperacional",
                String.valueOf(maquina.getCustoOperacionalBase()));
        adicionarTexto(documento, elemento, "saude", String.valueOf(maquina.getSaude()));
        adicionarTexto(documento, elemento, "ciclos", String.valueOf(maquina.getCiclos()));
        adicionarTexto(documento, elemento, "falhas", String.valueOf(maquina.getHistoricoFalhas()));

        Element motor = documento.createElement("motor");
        motor.setAttribute("saude", String.valueOf(maquina.getSaudeMotor()));
        elemento.appendChild(motor);

        Element painel = documento.createElement("painelDeControle");
        painel.setAttribute("saude", String.valueOf(maquina.getSaudePainel()));
        elemento.appendChild(painel);

        Element engrenagens = documento.createElement("engrenagens");
        elemento.appendChild(engrenagens);
        int numero = 1;
        for (double saude : maquina.getSaudesEngrenagens()) {
            Element engrenagem = documento.createElement("engrenagem");
            engrenagem.setAttribute("numero", String.valueOf(numero++));
            engrenagem.setAttribute("saude", String.valueOf(saude));
            engrenagens.appendChild(engrenagem);
        }
        return elemento;
    }

    private void adicionarTexto(Document documento, Element pai, String nome, String valor) {
        Element filho = documento.createElement(nome);
        filho.setTextContent(valor);
        pai.appendChild(filho);
    }

    private Document novoDocumento() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException erro) {
            throw new PersistenciaException("O Java deste computador não consegue montar XML.", erro);
        }
    }

    // ---------------------------------------------------------------
    // Carregar
    // ---------------------------------------------------------------

    /**
     * Lê o arquivo e reconstrói o estado da planta. Não altera nenhuma planta
     * existente: devolve o estado já validado, e quem chamar decide adotá-lo.
     *
     * @return o estado lido do arquivo
     * @throws ArquivoInexistenteException se o arquivo não existe
     * @throws DadosInvalidosException     se o XML é válido mas os dados
     *                                     contrariam as regras da fábrica
     * @throws PersistenciaException       se o arquivo está vazio, não é XML
     *                                     válido ou não pôde ser lido
     */
    public EstadoPlanta carregar() {
        if (!Files.exists(arquivo)) {
            throw new ArquivoInexistenteException(arquivo.toString());
        }
        Document documento = lerDocumento();
        try {
            return reconstruir(documento.getDocumentElement());
        } catch (DadosInvalidosException erro) {
            throw erro;
        } catch (PlantaException | IllegalArgumentException erro) {
            // As proprias classes do dominio recusam valores invalidos.
            throw new DadosInvalidosException("O arquivo " + arquivo.getFileName()
                    + " tem dados que a fábrica recusa: " + erro.getMessage(), erro);
        }
    }

    private Document lerDocumento() {
        try {
            if (Files.size(arquivo) == 0) {
                throw new PersistenciaException("O arquivo " + arquivo.getFileName() + " está vazio.");
            }
            DocumentBuilderFactory fabrica = DocumentBuilderFactory.newInstance();
            // Arquivo de fora nao pode puxar entidades externas nem DOCTYPE.
            fabrica.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            fabrica.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder construtor = fabrica.newDocumentBuilder();
            // Sem isso o parser imprime o erro no terminal alem de lancar.
            construtor.setErrorHandler(null);
            try (var entrada = Files.newInputStream(arquivo)) {
                return construtor.parse(entrada);
            }
        } catch (SAXParseException erro) {
            throw new PersistenciaException("O arquivo " + arquivo.getFileName()
                    + " não é um XML válido (linha " + erro.getLineNumber() + "): "
                    + erro.getMessage(), erro);
        } catch (SAXException erro) {
            throw new PersistenciaException("O arquivo " + arquivo.getFileName()
                    + " não é um XML válido: " + erro.getMessage(), erro);
        } catch (IOException erro) {
            throw new PersistenciaException("Não foi possível ler " + arquivo + ": "
                    + erro.getMessage(), erro);
        } catch (ParserConfigurationException erro) {
            throw new PersistenciaException("O Java deste computador não consegue ler XML.", erro);
        }
    }

    private EstadoPlanta reconstruir(Element raiz) {
        if (!raiz.getTagName().equals("planta")) {
            throw new DadosInvalidosException("O arquivo não é uma planta da NotBiscoito (elemento raiz \""
                    + raiz.getTagName() + "\").");
        }
        if (!VERSAO.equals(raiz.getAttribute("versao"))) {
            throw new DadosInvalidosException("Versão do arquivo não suportada: \""
                    + raiz.getAttribute("versao") + "\".");
        }

        Cenario cenario = enumerado(Cenario.class, texto(raiz, "cenario"), "cenário");
        double budget = naoNegativo(numero(texto(raiz, "budget"), "budget"), "budget");
        int lotes = (int) naoNegativo(inteiro(texto(raiz, "lotes"), "lotes"), "lotes");

        MateriaPrima massa = null;
        List<MateriaPrima> ingredientes = new ArrayList<>();
        for (Element elemento : filhos(obrigatorio(raiz, "despensa"), "materiaPrima")) {
            MateriaPrima materia = lerMateriaPrima(elemento);
            String funcao = elemento.getAttribute("funcao");
            if (funcao.equals("massa") && massa == null) {
                massa = materia;
            } else if (funcao.equals("ingrediente")) {
                ingredientes.add(materia);
            } else {
                throw new DadosInvalidosException("Matéria-prima \"" + materia.getNome()
                        + "\" com função inválida ou repetida: \"" + funcao + "\".");
            }
        }
        if (massa == null) {
            throw new DadosInvalidosException("A despensa do arquivo não tem a massa da casa.");
        }

        List<Maquina> maquinas = new ArrayList<>();
        for (Element elemento : filhos(obrigatorio(raiz, "linha"), "maquina")) {
            maquinas.add(lerMaquina(elemento, cenario));
        }

        List<Demanda> demandas = new ArrayList<>();
        for (Element elemento : filhos(obrigatorio(raiz, "demandas"), "demanda")) {
            demandas.add(lerDemanda(elemento));
        }

        List<Produto> armazem = new ArrayList<>();
        for (Element elemento : filhos(obrigatorio(raiz, "armazem"), "bolacha")) {
            armazem.add(lerBolacha(elemento));
        }
        return new EstadoPlanta(cenario, budget, lotes, massa, ingredientes, maquinas, demandas, armazem);
    }

    private MateriaPrima lerMateriaPrima(Element elemento) {
        String nome = naoVazio(texto(elemento, "nome"), "nome da matéria-prima");
        String unidade = naoVazio(texto(elemento, "unidade"), "unidade de " + nome);
        double quantidade = naoNegativo(numero(texto(elemento, "quantidade"), "quantidade de " + nome),
                "quantidade de " + nome);
        double minima = naoNegativo(numero(texto(elemento, "quantidadeMinima"),
                "quantidade mínima de " + nome), "quantidade mínima de " + nome);
        double custo = naoNegativo(numero(texto(elemento, "custoPorUnidade"), "custo de " + nome),
                "custo de " + nome);
        return new MateriaPrima(naoVazio(elemento.getAttribute("id"), "id de " + nome), nome,
                quantidade, unidade, minima, custo);
    }

    private Maquina lerMaquina(Element elemento, Cenario cenario) {
        String tipo = elemento.getAttribute("tipo");
        double capacidade = numero(texto(elemento, "capacidadeMaxima"), "capacidade da " + tipo);
        double falhaBase = numero(texto(elemento, "probabilidadeFalhaBase"), "chance de falha da " + tipo);
        double custo = naoNegativo(numero(texto(elemento, "custoOperacional"), "custo da " + tipo),
                "custo da " + tipo);
        if (capacidade <= 0) {
            throw new DadosInvalidosException("A capacidade da " + tipo + " precisa ser maior que zero.");
        }
        if (falhaBase < 0 || falhaBase > 1) {
            throw new DadosInvalidosException("A chance de falha da " + tipo + " precisa estar entre 0 e 1.");
        }

        Element elementoEngrenagens = obrigatorio(elemento, "engrenagens");
        List<Double> saudesEngrenagens = new ArrayList<>();
        for (Element engrenagem : filhos(elementoEngrenagens, "engrenagem")) {
            saudesEngrenagens.add(numero(engrenagem.getAttribute("saude"), "saúde da engrenagem"));
        }
        if (saudesEngrenagens.isEmpty()) {
            throw new DadosInvalidosException("A " + tipo + " precisa de pelo menos uma engrenagem.");
        }

        Maquina maquina = criarMaquina(tipo, capacidade, falhaBase, custo, saudesEngrenagens.size());
        // O cenario primeiro: ele mexe na saude, que o estado salvo sobrescreve em seguida.
        maquina.aplicarCenario(cenario);
        maquina.restaurarEstado(
                numero(texto(elemento, "saude"), "saúde da " + tipo),
                (int) naoNegativo(inteiro(texto(elemento, "ciclos"), "ciclos da " + tipo), "ciclos"),
                (int) naoNegativo(inteiro(texto(elemento, "falhas"), "falhas da " + tipo), "falhas"),
                numero(obrigatorio(elemento, "motor").getAttribute("saude"), "saúde do motor"),
                numero(obrigatorio(elemento, "painelDeControle").getAttribute("saude"), "saúde do painel"),
                saudesEngrenagens);
        return maquina;
    }

    private Maquina criarMaquina(String tipo, double capacidade, double falhaBase, double custo,
            int engrenagens) {
        return switch (tipo) {
            case "Estampadeira" -> new Estampadeira(capacidade, falhaBase, custo, engrenagens);
            case "Forno" -> new Forno(capacidade, falhaBase, custo, engrenagens);
            case "EstacaoInspecao" -> new EstacaoInspecao(capacidade, falhaBase, custo, engrenagens);
            default -> throw new DadosInvalidosException("Tipo de máquina desconhecido: \"" + tipo + "\".");
        };
    }

    // O status entra pelas transicoes da propria demanda, nao por um setter:
    // assim o arquivo nao consegue criar um estado que o dominio proibe.
    private Demanda lerDemanda(Element elemento) {
        Demanda demanda = new Demanda(elemento.getAttribute("tipoProduto"),
                (int) inteiro(elemento.getAttribute("quantidade"), "quantidade da demanda"));
        demanda.setCustoUnitarioEstimado(naoNegativo(numero(elemento.getAttribute("custoUnitarioEstimado"),
                "custo estimado da demanda"), "custo estimado da demanda"));
        StatusDemanda status = enumerado(StatusDemanda.class, elemento.getAttribute("status"),
                "status da demanda");
        switch (status) {
            case EM_PRODUCAO -> demanda.iniciarProducao();
            case CONCLUIDA -> {
                demanda.iniciarProducao();
                demanda.atender();
            }
            case CANCELADA -> demanda.cancelar();
            default -> { }
        }
        return demanda;
    }

    private Produto lerBolacha(Element elemento) {
        String id = naoVazio(elemento.getAttribute("id"), "id da bolacha");
        String sabor = naoVazio(elemento.getAttribute("sabor"), "sabor da bolacha");
        Produto bolacha = switch (elemento.getAttribute("tipo")) {
            case "BolachaAmanteigada" -> new BolachaAmanteigada(id, sabor);
            case "BolachaTradicional" -> new BolachaTradicional(id, sabor);
            case "BolachaCracker" -> new BolachaCracker(id, sabor);
            default -> throw new DadosInvalidosException("Tipo de bolacha desconhecido: \""
                    + elemento.getAttribute("tipo") + "\".");
        };
        double risco = numero(elemento.getAttribute("risco"), "risco da bolacha");
        if (risco < 0 || risco > 1) {
            throw new DadosInvalidosException("O risco da bolacha " + id + " precisa estar entre 0 e 1.");
        }
        bolacha.setStatus(enumerado(StatusBolacha.class, elemento.getAttribute("status"),
                "status da bolacha"));
        bolacha.setLote((int) naoNegativo(inteiro(elemento.getAttribute("lote"), "lote da bolacha"),
                "lote da bolacha"));
        bolacha.aumentarProbabilidadeFalha(risco);
        return bolacha;
    }

    // ---------------------------------------------------------------
    // Leitura de valores (validacao de formato e de faixa)
    // ---------------------------------------------------------------

    private static Element obrigatorio(Element pai, String nome) {
        for (Node no = pai.getFirstChild(); no != null; no = no.getNextSibling()) {
            if (no instanceof Element elemento && elemento.getTagName().equals(nome)) {
                return elemento;
            }
        }
        throw new DadosInvalidosException("Falta o elemento <" + nome + "> dentro de <"
                + pai.getTagName() + ">.");
    }

    private static List<Element> filhos(Element pai, String nome) {
        List<Element> encontrados = new ArrayList<>();
        NodeList nos = pai.getChildNodes();
        for (int i = 0; i < nos.getLength(); i++) {
            if (nos.item(i) instanceof Element elemento && elemento.getTagName().equals(nome)) {
                encontrados.add(elemento);
            }
        }
        return encontrados;
    }

    private static String texto(Element pai, String nome) {
        return obrigatorio(pai, nome).getTextContent().trim();
    }

    private static double numero(String valor, String descricao) {
        try {
            double numero = Double.parseDouble(valor.trim());
            if (Double.isNaN(numero) || Double.isInfinite(numero)) {
                throw new NumberFormatException(valor);
            }
            return numero;
        } catch (NumberFormatException erro) {
            throw new DadosInvalidosException("Valor inválido para " + descricao + ": \"" + valor + "\".");
        }
    }

    private static long inteiro(String valor, String descricao) {
        try {
            return Long.parseLong(valor.trim());
        } catch (NumberFormatException erro) {
            throw new DadosInvalidosException("Valor inteiro inválido para " + descricao + ": \""
                    + valor + "\".");
        }
    }

    private static double naoNegativo(double valor, String descricao) {
        if (valor < 0) {
            throw new DadosInvalidosException("O valor de " + descricao + " não pode ser negativo: " + valor + ".");
        }
        return valor;
    }

    private static String naoVazio(String valor, String descricao) {
        if (valor == null || valor.isBlank()) {
            throw new DadosInvalidosException("Falta o valor de " + descricao + ".");
        }
        return valor;
    }

    private static <E extends Enum<E>> E enumerado(Class<E> tipo, String valor, String descricao) {
        try {
            return Enum.valueOf(tipo, valor.trim());
        } catch (IllegalArgumentException erro) {
            throw new DadosInvalidosException("Valor desconhecido para " + descricao + ": \"" + valor + "\".");
        }
    }
}
