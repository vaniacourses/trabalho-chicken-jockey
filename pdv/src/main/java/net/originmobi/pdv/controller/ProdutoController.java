package net.originmobi.pdv.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import net.originmobi.pdv.dto.produto.ProdutoMergerDTO;
import net.originmobi.pdv.enumerado.Ativo;
import net.originmobi.pdv.enumerado.produto.ProdutoBalanca;
import net.originmobi.pdv.enumerado.produto.ProdutoControleEstoque;
import net.originmobi.pdv.enumerado.produto.ProdutoSubstTributaria;
import net.originmobi.pdv.enumerado.produto.ProdutoVendavel;
import net.originmobi.pdv.filter.ProdutoFilter;
import net.originmobi.pdv.model.Categoria;
import net.originmobi.pdv.model.Fornecedor;
import net.originmobi.pdv.model.Grupo;
import net.originmobi.pdv.model.ModBcIcms;
import net.originmobi.pdv.model.Produto;
import net.originmobi.pdv.model.Tributacao;
import net.originmobi.pdv.service.CategoriaService;
import net.originmobi.pdv.service.FornecedorService;
import net.originmobi.pdv.service.GrupoService;
import net.originmobi.pdv.service.ImagemProdutoService;
import net.originmobi.pdv.service.ProdutoService;
import net.originmobi.pdv.service.TributacaoService;
import net.originmobi.pdv.service.notafiscal.ModBcIcmsService;

@Controller
@RequestMapping("/produto")
@SessionAttributes("filterProduto")
public class ProdutoController {

	private static final String PRODUTO_LIST = "produto/list";
	private static final String PRODUTO_FORM = "produto/form";

	@Autowired
	private ProdutoService produtosService;

	@Autowired
	private FornecedorService fornecedoresService;

	@Autowired
	private GrupoService gruposService;

	@Autowired
	private CategoriaService categoriasService;

	@Autowired
	private ImagemProdutoService imagensService;

	@Autowired
	private TributacaoService tributacoesService;

	@Autowired
	private ModBcIcmsService modbcsService;

	@GetMapping("/form")
	public ModelAndView form(Produto produto) {
		ModelAndView mv = new ModelAndView(PRODUTO_FORM);
		mv.addObject(new Produto());
		return mv;
	}

	@ModelAttribute("filterProduto")
	public ProdutoFilter inicializerProdutoFilter() {
		return new ProdutoFilter();
	}

	@GetMapping
	public ModelAndView lista(@ModelAttribute("filterProduto") ProdutoFilter filter, Pageable pageable, Model model) {
		ModelAndView mv = new ModelAndView(PRODUTO_LIST);
		Page<Produto> listProdutos = produtosService.filter(filter, pageable);
		mv.addObject("produtos", listProdutos);

		model.addAttribute("qtdpaginas", listProdutos.getTotalPages());
		model.addAttribute("pagAtual", listProdutos.getPageable().getPageNumber());
		model.addAttribute("proxPagina", listProdutos.getPageable().next().getPageNumber());
		model.addAttribute("pagAnterior", listProdutos.getPageable().previousOrFirst().getPageNumber());
		model.addAttribute("hasNext", listProdutos.hasNext());
		model.addAttribute("hasPrevious", listProdutos.hasPrevious());

		return mv;
	}

	@RequestMapping(method = RequestMethod.POST)
	public String cadastrar(@RequestParam Map<String, String> request, RedirectAttributes attributes) {

		Long codigoProd = request.get("codigo") == null || request.get("codigo").isEmpty() ? null : Long.decode(request.get("codigo"));
		String descricao = request.get("descricao");
		Long codForne = request.get("fornecedor") == null || request.get("fornecedor").isEmpty() ? null : Long.decode(request.get("fornecedor"));
		Long codCategoria = request.get("categoria") == null || request.get("categoria").isEmpty() ? null : Long.decode(request.get("categoria"));
		Long codGrupo = request.get("grupo") == null || request.get("grupo").isEmpty() ? null : Long.decode(request.get("grupo"));
		int balanca = request.get("balanca") != null && request.get("balanca").equals("SIM") ? 1 : 0;
		Double valorCusto = request.get("valor_custo") == null || request.get("valor_custo").isEmpty() ? 0.0 : Double.valueOf(request.get("valor_custo").replace(",", "."));
		Double valorVenda = request.get("valor_venda") == null || request.get("valor_venda").isEmpty() ? 0.0 : Double.valueOf(request.get("valor_venda").replace(",", "."));
		String validadeStr = request.get("data_validade") == null ? "" : request.get("data_validade");
		String controleEstoque = request.get("controla_estoque");
		String ativo = request.get("ativo");
		String unidade = request.get("unidade");
		ProdutoSubstTributaria substituicao = request.get("subtributaria") != null && request.get("subtributaria").equals("SIM") ? ProdutoSubstTributaria.SIM : ProdutoSubstTributaria.NAO;
		String ncm = request.get("ncm");
		String cest = request.get("cest");
		Long codTributacao = request.get("tributacao") == null || request.get("tributacao").isEmpty() ? null : Long.decode(request.get("tributacao"));
		Long codModbc = request.get("modBcIcms") == null || request.get("modBcIcms").isEmpty() ? null : Long.decode(request.get("modBcIcms"));
		String vendavel = request.get("vendavel");
		String enviar = request.get("enviar");

		Date dataValidade = null;
		if (!validadeStr.isEmpty()) {
			SimpleDateFormat formatoUser = new SimpleDateFormat("dd/MM/yyyy");
			try {
				dataValidade = formatoUser.parse(validadeStr);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		ProdutoMergerDTO dto = new ProdutoMergerDTO(codigoProd, codForne, codCategoria, codGrupo, balanca, descricao,
				valorCusto, valorVenda, dataValidade, controleEstoque, ativo, unidade, substituicao, ncm, cest,
				codTributacao, codModbc, vendavel, enviar);

		String mensagem = produtosService.merger(dto);

		attributes.addFlashAttribute("mensagem", mensagem);

		if (codigoProd != null && codigoProd != 0)
			return "redirect:/produto/" + codigoProd.toString();

		return "redirect:/produto";
	}

	@GetMapping("{codigo}")
	public ModelAndView editar(@PathVariable("codigo") Produto produto) {
		ModelAndView mv = new ModelAndView(PRODUTO_FORM);
		mv.addObject("produto", produto);
		mv.addObject("imagem", imagensService.busca(produto.getCodigo()));
		return mv;
	}

	@ModelAttribute("ativo")
	public List<Ativo> ativo() {
		return Arrays.asList(Ativo.values());
	}

	@ModelAttribute("fornecedores")
	public List<Fornecedor> fornecedores() {
		return fornecedoresService.lista();
	}

	@ModelAttribute("grupos")
	public List<Grupo> grupos() {
		return gruposService.lista();
	}

	@ModelAttribute("categorias")
	public List<Categoria> categorias() {
		return categoriasService.lista();
	}

	@ModelAttribute("balanca")
	public List<ProdutoBalanca> balanca() {
		return Arrays.asList(ProdutoBalanca.values());
	}

	@ModelAttribute("subtributaria")
	public List<ProdutoSubstTributaria> substTributaria() {
		return Arrays.asList(ProdutoSubstTributaria.values());
	}

	@ModelAttribute("tributacoes")
	public List<Tributacao> tributacoes() {
		return tributacoesService.lista();
	}

	@ModelAttribute("modbcs")
	private List<ModBcIcms> modbc() {
		return modbcsService.lista();
	}

	@ModelAttribute("controlaestoque")
	private List<ProdutoControleEstoque> controlaestoque() {
		return Arrays.asList(ProdutoControleEstoque.values());
	}

	@ModelAttribute("produtoVendavel")
	public List<ProdutoVendavel> produtoVendavel() {
		return Arrays.asList(ProdutoVendavel.values());
	}
}
