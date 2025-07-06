package net.originmobi.pdv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import net.originmobi.pdv.dto.produto.ProdutoMergerDTO;
import net.originmobi.pdv.enumerado.EntradaSaida;
import net.originmobi.pdv.enumerado.produto.ProdutoControleEstoque;
import net.originmobi.pdv.enumerado.produto.ProdutoSubstTributaria;
import net.originmobi.pdv.exception.produto.EstoqueInsuficienteException;
import net.originmobi.pdv.exception.produto.ProdutoNaoControlaEstoqueException;
import net.originmobi.pdv.filter.ProdutoFilter;
import net.originmobi.pdv.model.Produto;
import net.originmobi.pdv.repository.ProdutoRepository;

class ProdutoServiceTest {

	@InjectMocks
	private ProdutoService produtoService;

	@Mock
	private ProdutoRepository produtoRepository;

	@Mock
	private VendaProdutoService vendaProdutoService;

	private Produto produto;
	private Produto produto2;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.initMocks(this);
		produto = new Produto();
		produto2 = new Produto();
	}

	@Test
    void testListar_deveRetornarTodosOsProdutos() {
        when(produtoRepository.findAll()).thenReturn(Arrays.asList(produto, produto2));
        List<Produto> produtos = produtoService.listar();
        assertEquals(2, produtos.size());
        verify(produtoRepository, times(1)).findAll();
    }

	@Test
    void listaProdutosVendaveis_deveRetornarProdutosVendaveis() {
        when(produtoRepository.produtosVendaveis()).thenReturn(Arrays.asList(produto, produto2));
        List<Produto> produtos = produtoService.listaProdutosVendaveis();
        assertEquals(2, produtos.size());
        assertEquals(produto, produtos.get(0));
        assertEquals(produto2, produtos.get(1));
        verify(produtoRepository, times(1)).produtosVendaveis();
    }

	@Test
	void busca_deveRetornarProdutoPorCodigo() {
		Long codigoProduto = 1L;
		when(produtoRepository.findByCodigoIn(codigoProduto)).thenReturn(produto);
		Produto produtoEncontrado = produtoService.busca(codigoProduto);
		assertEquals(produto, produtoEncontrado);
		verify(produtoRepository, times(1)).findByCodigoIn(codigoProduto);
	}

	@Test
    public void buscaProduto_deveRetornarProdutoPorId() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        Optional<Produto> result = produtoService.buscaProduto(1L);
        assertTrue(result.isPresent());
        assertEquals(produto, result.get());
        verify(produtoRepository).findById(1L);
    }

	@Test
	void filter_deveRetornarProdutosFiltrados() {
		ProdutoFilter filter = new ProdutoFilter();
		filter.setDescricao("Produto");
		Pageable pageable = mock(Pageable.class);
		when(produtoRepository.findByDescricaoContaining("Produto", pageable))
				.thenReturn(new PageImpl<>(Collections.emptyList()));

		Page<Produto> resultPage = produtoService.filter(filter, pageable);
		assertNotNull(resultPage);
		assertTrue(resultPage.getContent().isEmpty());
		verify(produtoRepository, times(1)).findByDescricaoContaining("Produto", pageable);
	}

	@Test
	void mergerInsereProduto_deveInserirProduto() {
		Long codprod = 0L;
		Long codforne = 2L;
		Long codcategoria = 3L;
		Long codgrupo = 4L;
		int balanca = 1;
		String descricao = "Novo Produto";
		Double valorCusto = 10.0;
		Double valorVenda = 20.0;
		String controleEstoque = "SIM";
		String situacao = "ATIVO";
		String unitario = "UN";
		String ncm = "12345678";
		String cest = "123456";
		Long tributacao = 6L;
		Long modbc = 7L;
		String vendavel = "SIM";
		ProdutoSubstTributaria produtoSubstTributaria = ProdutoSubstTributaria.SIM;

		ProdutoMergerDTO dto = new ProdutoMergerDTO(codprod, codforne, codcategoria, codgrupo, balanca, descricao,
				valorCusto, valorVenda, null, controleEstoque, situacao, unitario, produtoSubstTributaria, ncm, cest,
				tributacao, modbc, vendavel);

		String resultado = produtoService.merger(dto);

		verify(produtoRepository).insere(eq(codforne), eq(codcategoria), eq(codgrupo), eq(balanca), eq(descricao),
				eq(valorCusto), eq(valorVenda), isNull(), eq(controleEstoque), eq(situacao), eq(unitario),
				eq(produtoSubstTributaria.ordinal()), any(java.sql.Date.class), eq(ncm), eq(cest), eq(tributacao),
				eq(modbc), eq(vendavel));
		assertEquals("Produdo cadastrado com sucesso", resultado);
	}

	@Test
	void mergerAtualizaProduto_deveAtualizarProduto() {
		Long codprod = 1L;
		Long codforne = 2L;
		Long codcategoria = 3L;
		Long codgrupo = 4L;
		int balanca = 1;
		String descricao = "Produto Atualizado";
		Double valorCusto = 15.0;
		Double valorVenda = 25.0;
		String controleEstoque = "SIM";
		String situacao = "ATIVO";
		String unitario = "UN";
		String ncm = "87654321";
		String cest = "654321";
		Long tributacao = 6L;
		Long modbc = 7L;
		String vendavel = "SIM";
		ProdutoSubstTributaria subtribu = ProdutoSubstTributaria.SIM;

		ProdutoMergerDTO dto = new ProdutoMergerDTO(codprod, codforne, codcategoria, codgrupo, balanca, descricao,
				valorCusto, valorVenda, null, controleEstoque, situacao, unitario, subtribu, ncm, cest, tributacao,
				modbc, vendavel);

		String resultado = produtoService.merger(dto);

		verify(produtoRepository).atualiza(eq(codprod), eq(codforne), eq(codcategoria), eq(codgrupo), eq(balanca),
				eq(descricao), eq(valorCusto), eq(valorVenda), isNull(), eq(controleEstoque), eq(situacao),
				eq(unitario), eq(subtribu.ordinal()), eq(ncm), eq(cest), eq(tributacao), eq(modbc), eq(vendavel));
		assertEquals("Produto atualizado com sucesso", resultado);
	}

	@Test
	void ajusteEstoque_deveMovimentarEstoqueQuandoControlaEstoque() {
		Long codprod = 1L;
		int qtd = 5;
		EntradaSaida tipo = EntradaSaida.SAIDA;
		String origem = "Ajuste";
		Date data = new Date(System.currentTimeMillis());

		Produto produtoMock = mock(Produto.class);
		when(produtoRepository.findByCodigoIn(codprod)).thenReturn(produtoMock);
		when(produtoMock.getControla_estoque()).thenReturn(ProdutoControleEstoque.SIM);

		produtoService.ajusteEstoque(codprod, qtd, tipo, origem, data);

		verify(produtoRepository).movimentaEstoque(codprod, tipo.toString(), qtd, origem, data);
	}

	@Test
	void ajusteEstoqueNaoControla_deveLancarExcecaoQuandoNaoControlaEstoque() {
		Long codprod = 1L;
		int qtd = 5;
		EntradaSaida tipo = EntradaSaida.ENTRADA;
		String origem = "Ajuste";
		Date data = new Date(System.currentTimeMillis());

		Produto produtoMock = mock(Produto.class);
		when(produtoRepository.findByCodigoIn(codprod)).thenReturn(produtoMock);
		when(produtoMock.getControla_estoque()).thenReturn(ProdutoControleEstoque.NAO);

		ProdutoNaoControlaEstoqueException ex = assertThrows(ProdutoNaoControlaEstoqueException.class,
				() -> produtoService.ajusteEstoque(codprod, qtd, tipo, origem, data));

		assertTrue(ex.getMessage().contains("não controla estoque"));
		verify(produtoRepository, never()).movimentaEstoque(any(), any(), anyInt(), any(), any());
	}

	@Test
	void movimentaEstoqueSemEstoque_deveLancarExcecaoQuandoEstoqueForInsuficiente() {
		Long codvenda = 1L;
		Object[] item = { 2L, 10 };

		List<Object[]> resultado = new ArrayList<>();
		resultado.add(item);
		when(vendaProdutoService.buscaQtdProduto(codvenda)).thenReturn(resultado);

		Produto produtoMock = mock(Produto.class);
		when(produtoRepository.findByCodigoIn(2L)).thenReturn(produtoMock);
		when(produtoMock.getControla_estoque()).thenReturn(ProdutoControleEstoque.SIM);
		when(produtoRepository.saldoEstoque(2L)).thenReturn(5);

		EstoqueInsuficienteException ex = assertThrows(EstoqueInsuficienteException.class,
				() -> produtoService.movimentaEstoque(codvenda, EntradaSaida.SAIDA));
		assertTrue(ex.getMessage().contains("não tem estoque suficiente"));
		verify(produtoRepository, never()).movimentaEstoque(any(), any(), anyInt(), any(), any());
	}

	@Test
	void movimentaEstoque_deveMovimentarEstoqueQuandoSuficiente() {
		Long codvenda = 1L;
		Object[] item = { 2L, 3 };

		List<Object[]> resultado = new ArrayList<>();
		resultado.add(item);
		when(vendaProdutoService.buscaQtdProduto(codvenda)).thenReturn(resultado);

		Produto produtoMock = mock(Produto.class);
		when(produtoRepository.findByCodigoIn(2L)).thenReturn(produtoMock);
		when(produtoMock.getControla_estoque()).thenReturn(ProdutoControleEstoque.SIM);
		when(produtoRepository.saldoEstoque(2L)).thenReturn(5);

		produtoService.movimentaEstoque(codvenda, EntradaSaida.SAIDA);

		verify(produtoRepository).movimentaEstoque(eq(2L), eq(EntradaSaida.SAIDA.toString()), eq(3), anyString(),
				any(java.sql.Date.class));
	}

	@Test
	void filter_deveRetornarTodosOsProdutosQuandoDescricaoNula() {
		ProdutoFilter filter = new ProdutoFilter();
		Pageable pageable = mock(Pageable.class);

		when(produtoRepository.findByDescricaoContaining(eq("%"), eq(pageable)))
				.thenReturn(new PageImpl<>(Arrays.asList(produto, produto2)));

		Page<Produto> resultPage = produtoService.filter(filter, pageable);

		assertNotNull(resultPage);
		assertEquals(2, resultPage.getContent().size());
		verify(produtoRepository, times(1)).findByDescricaoContaining(eq("%"), eq(pageable));
	}

	@Test
	void movimentaEstoque_deveIgnorarMovimentacaoQuandoNaoControlaEstoque() {
		Long codvenda = 1L;
		Object[] item = { 2L, 5 };

		List<Object[]> resultado = new ArrayList<>();
		resultado.add(item);
		when(vendaProdutoService.buscaQtdProduto(codvenda)).thenReturn(resultado);

		Produto produtoMock = mock(Produto.class);
		when(produtoRepository.findByCodigoIn(2L)).thenReturn(produtoMock);

		when(produtoMock.getControla_estoque()).thenReturn(ProdutoControleEstoque.NAO);

		ProdutoNaoControlaEstoqueException ex = assertThrows(ProdutoNaoControlaEstoqueException.class,
				() -> produtoService.movimentaEstoque(codvenda, EntradaSaida.SAIDA));

		assertTrue(ex.getMessage().contains("não controla estoque"));

		verify(produtoRepository, never()).movimentaEstoque(any(), any(), anyInt(), any(), any());
	}

	@Test
	void movimentaEstoque_deveMovimentarEstoqueQuandoQuantidadeIgualAoEstoque() {
		Long codvenda = 1L;
		Object[] item = { 2L, 5 };
		List<Object[]> resultado = new ArrayList<>();
		resultado.add(item);
		when(vendaProdutoService.buscaQtdProduto(codvenda)).thenReturn(resultado);

		Produto produtoMock = mock(Produto.class);
		when(produtoRepository.findByCodigoIn(2L)).thenReturn(produtoMock);
		when(produtoMock.getControla_estoque()).thenReturn(ProdutoControleEstoque.SIM);
		when(produtoRepository.saldoEstoque(2L)).thenReturn(5);

		produtoService.movimentaEstoque(codvenda, EntradaSaida.SAIDA);

		verify(produtoRepository).movimentaEstoque(eq(2L), eq(EntradaSaida.SAIDA.toString()), eq(5), anyString(),
				any(java.sql.Date.class));
	}

	@Test
	void mergerInsereProduto_deveRetornarErroQuandoExcecaoAoInserir() {
		Long codprod = 0L;
		Long codforne = 1L;
		Long codcategoria = 1L;
		Long codgrupo = 1L;
		int balanca = 0;
		String descricao = "Produto Com Erro";
		Double valorCusto = 10.0;
		Double valorVenda = 20.0;
		String controleEstoque = "SIM";
		String situacao = "ATIVO";
		String unitario = "UN";
		String ncm = "12345678";
		String cest = "123456";
		Long tributacao = 1L;
		Long modbc = 1L;
		String vendavel = "SIM";
		ProdutoSubstTributaria subtribu = ProdutoSubstTributaria.NAO;

		ProdutoMergerDTO dto = new ProdutoMergerDTO(codprod, codforne, codcategoria, codgrupo, balanca, descricao,
				valorCusto, valorVenda, null, controleEstoque, situacao, unitario, subtribu, ncm, cest, tributacao,
				modbc, vendavel);

		doThrow(new RuntimeException("Erro simulado de inserção")).when(produtoRepository).insere(any(), any(), any(),
				anyInt(), any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(), any(), any(),
				any());

		String resultado = produtoService.merger(dto);

		assertEquals("Erro a cadastrar produto, chame o suporte", resultado);
	}

	@Test
	void mergerAtualizaProduto_deveRetornarErroQuandoExcecaoAoAtualizar() {
		Long codprod = 1L;
		Long codforne = 1L;
		Long codcategoria = 1L;
		Long codgrupo = 1L;
		int balanca = 0;
		String descricao = "Produto Com Erro Atualizacao";
		Double valorCusto = 10.0;
		Double valorVenda = 20.0;
		String controleEstoque = "SIM";
		String situacao = "ATIVO";
		String unitario = "UN";
		String ncm = "12345678";
		String cest = "123456";
		Long tributacao = 1L;
		Long modbc = 1L;
		String vendavel = "SIM";
		ProdutoSubstTributaria subtribu = ProdutoSubstTributaria.NAO;

		ProdutoMergerDTO dto = new ProdutoMergerDTO(codprod, codforne, codcategoria, codgrupo, balanca, descricao,
				valorCusto, valorVenda, null, controleEstoque, situacao, unitario, subtribu, ncm, cest, tributacao,
				modbc, vendavel);

		doThrow(new RuntimeException("Erro simulado de atualização")).when(produtoRepository).atualiza(any(), any(),
				any(), any(), anyInt(), any(), any(), any(), any(), any(), any(), any(), anyInt(), any(), any(), any(),
				any(), any());

		String resultado = produtoService.merger(dto);

		assertEquals("Erro a atualizar produto, chame o suporte", resultado);
	}
}