package net.originmobi.pdv.service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import net.originmobi.pdv.dto.produto.ProdutoMergerDTO;
import net.originmobi.pdv.enumerado.EntradaSaida;
import net.originmobi.pdv.enumerado.produto.ProdutoControleEstoque;
import net.originmobi.pdv.exception.produto.EstoqueInsuficienteException;
import net.originmobi.pdv.exception.produto.ProdutoNaoControlaEstoqueException;
import net.originmobi.pdv.filter.ProdutoFilter;
import net.originmobi.pdv.model.Produto;
import net.originmobi.pdv.repository.ProdutoRepository;

@Service
public class ProdutoService {

	private static final Logger log = LoggerFactory.getLogger(ProdutoService.class);

	public ProdutoService(ProdutoRepository produtos, VendaProdutoService vendaProdutos) {
		this.produtos = produtos;
		this.vendaProdutos = vendaProdutos;
	}

	private ProdutoRepository produtos;

	private VendaProdutoService vendaProdutos;

	private LocalDate dataAtual = LocalDate.now();

	public List<Produto> listar() {
		return produtos.findAll();
	}

	public List<Produto> listaProdutosVendaveis() {
		return produtos.produtosVendaveis();
	}

	public Produto busca(Long codigoProduto) {
		return produtos.findByCodigoIn(codigoProduto);
	}

	public Optional<Produto> buscaProduto(Long codigo) {
		return produtos.findById(codigo);
	}

	public Page<Produto> filter(ProdutoFilter filter, Pageable pageable) {
		String descricao = filter.getDescricao() == null ? "%" : filter.getDescricao();
		return produtos.findByDescricaoContaining(descricao, pageable);
	}

	public String merger(ProdutoMergerDTO dto) {

		if (dto.getCodigo() == null || dto.getCodigo() == 0) {
			try {
				produtos.insere(dto.getCodFornecedor(), dto.getCodCategoria(), dto.getCodGrupo(), dto.getBalanca(),
						dto.getDescricao(), dto.getValorCusto(), dto.getValorVenda(), dto.getDataValidade(),
						dto.getControleEstoque(), dto.getSituacao(), dto.getUnidade(), dto.getSubtributaria().ordinal(),
						Date.valueOf(dataAtual), dto.getNcm(), dto.getCest(), dto.getCodTributacao(), dto.getCodModbc(),
						dto.getVendavel());
			} catch (Exception e) {
				log.error("Erro ao cadastrar produto: {}", e.getMessage());
				return "Erro a cadastrar produto, chame o suporte";
			}
		} else {
			try {
				produtos.atualiza(dto.getCodigo(), dto.getCodFornecedor(), dto.getCodCategoria(), dto.getCodGrupo(),
						dto.getBalanca(), dto.getDescricao(), dto.getValorCusto(), dto.getValorVenda(),
						dto.getDataValidade(), dto.getControleEstoque(), dto.getSituacao(), dto.getUnidade(),
						dto.getSubtributaria().ordinal(), dto.getNcm(), dto.getCest(), dto.getCodTributacao(),
						dto.getCodModbc(), dto.getVendavel());

				return "Produto atualizado com sucesso";
			} catch (Exception e) {
				log.error("Erro ao atualizar produto: {}", e.getMessage());
				return "Erro a atualizar produto, chame o suporte";
			}
		}

		return "Produdo cadastrado com sucesso";
	}

	@SuppressWarnings("static-access")
	public void movimentaEstoque(Long codvenda, EntradaSaida tipo) {
		List<Object[]> resultado = vendaProdutos.buscaQtdProduto(codvenda);

		for (int i = 0; i < resultado.size(); i++) {
			Long codprod = Long.decode(resultado.get(i)[0].toString());
			int qtd = Integer.parseInt(resultado.get(i)[1].toString());

			Produto produto = produtos.findByCodigoIn(codprod);

			if (produto.getControla_estoque().equals(ProdutoControleEstoque.SIM)) {

				// estoque atual do produto
				int qtdEstoque = produtos.saldoEstoque(codprod);
				String origemOperacao = "Venda " + codvenda.toString();

				if (qtd <= qtdEstoque) {
					produtos.movimentaEstoque(codprod, tipo.SAIDA.toString(), qtd, origemOperacao,
							Date.valueOf(dataAtual));
				} else {
					throw new EstoqueInsuficienteException(
							"O produto de código " + codprod + " não tem estoque suficiente, verifique");
				}
			} else {
				log.warn("O produto de código {} não controla estoque, movimentação não realizada", codprod);
				throw new ProdutoNaoControlaEstoqueException(
						"O produto de código " + codprod + " não controla estoque, verifique");
			}
		}

	}

	public void ajusteEstoque(Long codprod, int qtd, EntradaSaida tipo, String origemOperacao, Date dataMovimentacao) {
		Produto produto = produtos.findByCodigoIn(codprod);

		if (produto.getControla_estoque().equals(ProdutoControleEstoque.NAO))
			throw new ProdutoNaoControlaEstoqueException(
					"O produto de código " + codprod + " não controla estoque, verifique");

		produtos.movimentaEstoque(codprod, tipo.toString(), qtd, origemOperacao, dataMovimentacao);

	}

}
