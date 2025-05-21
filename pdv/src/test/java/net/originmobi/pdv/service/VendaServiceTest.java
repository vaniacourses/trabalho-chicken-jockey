package net.originmobi.pdv.service;

import net.originmobi.pdv.controller.TituloService;
import net.originmobi.pdv.enumerado.EntradaSaida;
import net.originmobi.pdv.enumerado.VendaSituacao;
import net.originmobi.pdv.filter.VendaFilter;
import net.originmobi.pdv.model.*;
import net.originmobi.pdv.repository.VendaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VendaServiceTest {
    @InjectMocks
    private VendaService servicoVenda;
    @Mock
    private VendaRepository repositorioVenda;
    @Mock
    private UsuarioService servicoUsuario;
    @Mock
    private VendaProdutoService servicoVendaProduto;
    @Mock
    private PagamentoTipoService servicoFormaPagamento;
    @Mock
    private TituloService servicoTitulo;
    @Mock
    private CaixaService servicoCaixa;
    @Mock
    private ProdutoService servicoProduto;
    @Mock
    private ReceberService servicoReceber;
    @Mock
    private CaixaLancamentoService servicoLancamento;
    @Mock
    private ParcelaService servicoParcela;

    @BeforeEach
    void configuraTeste() {
        MockitoAnnotations.initMocks(this);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("Teste Erik", "Senha")
        );
    }

    @Test
    void testeAbreVendaNova() {
        Venda venda = new Venda();
        Usuario usuario = new Usuario();
        usuario.setCodigo(1L);

        when(servicoUsuario.buscaUsuario("Teste Erik")).thenReturn(usuario);
        when(repositorioVenda.save(any(Venda.class))).thenAnswer(i -> {
            Venda v = i.getArgument(0);
            v.setCodigo(1L);
            return v;
        });
        Long resultado = servicoVenda.abreVenda(venda);

        assertNotNull(resultado);
        assertEquals(Long.valueOf(1L), resultado);
        assertEquals(VendaSituacao.ABERTA, venda.getSituacao());
        assertEquals(Double.valueOf(0.00), venda.getValor_produtos());
        assertNotNull(venda.getData_cadastro());
        verify(repositorioVenda).save(any(Venda.class));
    }

    @Test
    void testeAbreVendaExistente() {
        Venda venda = new Venda();
        venda.setCodigo(1L);
        venda.setObservacao("");
        Long resultado = servicoVenda.abreVenda(venda);

        assertNotNull(resultado);
        assertEquals(Long.valueOf(1L), resultado);
        verify(repositorioVenda).updateDadosVenda(any(), any(), eq(1L));
    }

    @AfterEach
    void limpaTeste() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testeBuscaComCodigo() {
        VendaFilter filtro = new VendaFilter();
        Long codigo = 1L;
        filtro.setCodigo(codigo);

        Page<Venda> paginaEsperada = new PageImpl<>(Arrays.asList(new Venda()));
        Pageable paginacao = PageRequest.of(0, 10);
        when(repositorioVenda.findByCodigoIn(codigo, paginacao)).thenReturn(paginaEsperada);
        Page<Venda> resultado = servicoVenda.busca(filtro, "ABERTA", paginacao);

        assertNotNull(resultado);
        verify(repositorioVenda).findByCodigoIn(codigo, paginacao);
        verifyNoMoreInteractions(repositorioVenda);
    }

    @Test
    void testeBuscaSemCodigo() {
        VendaFilter filtro = new VendaFilter();
        Pageable paginacao = PageRequest.of(0, 10);
        Page<Venda> paginaEsperada = new PageImpl<>(Arrays.asList(new Venda()));

        when(repositorioVenda.findBySituacaoEquals(VendaSituacao.ABERTA, paginacao))
                .thenReturn(paginaEsperada);
        Page<Venda> resultado = servicoVenda.busca(filtro, "ABERTA", paginacao);

        assertNotNull(resultado);
        verify(repositorioVenda).findBySituacaoEquals(VendaSituacao.ABERTA, paginacao);
        verifyNoMoreInteractions(repositorioVenda);
    }

    @Test
    void testeAddProdutoVendaAberta() {
        Long codigoVenda = 1L;
        Long codigoProduto = 2L;
        Double valorBalanca = 10.0;

        when(repositorioVenda.verificaSituacao(codigoVenda))
                .thenReturn(VendaSituacao.ABERTA.toString());
        String resultado = servicoVenda.addProduto(codigoVenda, codigoProduto, valorBalanca);

        verify(servicoVendaProduto).salvar(argThat(vendaProduto ->
                vendaProduto.getVenda().equals(codigoVenda) &&
                        vendaProduto.getProduto().equals(codigoProduto) &&
                        vendaProduto.getValor_balanca().equals(valorBalanca)
        ));
        assertEquals("ok", resultado);
    }

    @Test
    void testeAddProdutoVendaFechada() {
        Long codigoVenda = 1L;
        Long codigoProduto = 2L;
        Double valorBalanca = 10.0;

        when(repositorioVenda.verificaSituacao(codigoVenda))
                .thenReturn(VendaSituacao.FECHADA.toString());
        String resultado = servicoVenda.addProduto(codigoVenda, codigoProduto, valorBalanca);

        verify(servicoVendaProduto, never()).salvar(any());
        assertEquals("Venda fechada", resultado);
    }

    @Test
    void testeRemoveProdutoVendaAberta() {
        Long posicaoProduto = 1L;
        Long codigoVenda = 1L;
        Venda venda = new Venda();
        venda.setSituacao(VendaSituacao.ABERTA);

        when(repositorioVenda.findByCodigoEquals(codigoVenda))
                .thenReturn(venda);
        String resultado = servicoVenda.removeProduto(posicaoProduto, codigoVenda);

        verify(servicoVendaProduto).removeProduto(posicaoProduto);
        assertEquals("ok", resultado);
    }

    @Test
    void testeRemoveProdutoVendaFechada() {
        Long posicaoProduto = 1L;
        Long codigoVenda = 1L;
        Venda venda = new Venda();
        venda.setSituacao(VendaSituacao.FECHADA);

        when(repositorioVenda.findByCodigoEquals(codigoVenda))
                .thenReturn(venda);
        String resultado = servicoVenda.removeProduto(posicaoProduto, codigoVenda);

        verify(servicoVendaProduto, never()).removeProduto(any());
        assertEquals("Venda fechada", resultado);
    }

    @Test
    void testeLista() {
        List<Venda> vendasEsperadas = Arrays.asList(
                new Venda(),
                new Venda()
        );

        when(repositorioVenda.findAll())
                .thenReturn(vendasEsperadas);

        List<Venda> resultado = servicoVenda.lista();

        assertNotNull(resultado);
        assertEquals(vendasEsperadas.size(), resultado.size());
        assertEquals(vendasEsperadas, resultado);
        verify(repositorioVenda).findAll();
    }

    @Test
    void testeFechaVendaDinheiro() {
        Long codigoVenda = 1L;
        Long codigoTipoPagamento = 2L;
        Double valorProdutos = 100.0;
        Double desconto = 10.0;
        Double acrescimo = 5.0;
        String[] valoresParcelas = {"100.0"};
        String[] codigosTitulos = {"1"};

        Venda venda = new Venda();
        venda.setCodigo(codigoVenda);
        venda.setSituacao(VendaSituacao.ABERTA);
        venda.setValor_produtos(valorProdutos);
        Pessoa pessoa = new Pessoa();
        venda.setPessoa(pessoa);

        PagamentoTipo tipoPagamento = new PagamentoTipo();
        tipoPagamento.setCodigo(codigoTipoPagamento);
        tipoPagamento.setFormaPagamento("00"); // Pagamento à vista

        Titulo titulo = new Titulo();
        titulo.setCodigo(1L);
        TituloTipo tipoTitulo = new TituloTipo();
        tipoTitulo.setSigla("DIN");
        tipoTitulo.setDescricao("Dinheiro");
        titulo.setTipo(tipoTitulo);

        when(repositorioVenda.findByCodigoEquals(codigoVenda)).thenReturn(venda);
        when(servicoFormaPagamento.busca(codigoTipoPagamento)).thenReturn(tipoPagamento);
        when(servicoTitulo.busca(1L)).thenReturn(Optional.of(titulo));
        when(servicoCaixa.caixaIsAberto()).thenReturn(true);
        when(servicoCaixa.caixaAberto()).thenReturn(Optional.of(new Caixa()));

        String resultado = servicoVenda.fechaVenda(codigoVenda, codigoTipoPagamento, valorProdutos, desconto, acrescimo, valoresParcelas, codigosTitulos);

        assertEquals("Venda finalizada com sucesso", resultado);
        verify(repositorioVenda).fechaVenda(eq(codigoVenda), eq(VendaSituacao.FECHADA), eq(95.0), eq(desconto), eq(acrescimo), any(), eq(tipoPagamento));
        verify(servicoReceber).cadastrar(any(Receber.class));
        verify(servicoLancamento).lancamento(any(CaixaLancamento.class));
        verify(servicoProduto).movimentaEstoque(eq(codigoVenda), eq(EntradaSaida.SAIDA));
    }

    @Test
    void testeFechaVendaJaFechada() {
        Long codigoVenda = 1L;
        Long codigoTipoPagamento = 1L;
        Double valorProdutos = 100.0;
        Double desconto = 0.0;
        Double acrescimo = 0.0;
        String[] valoresParcelas = {"100.0"};
        String[] codigosTitulos = {"1"};

        Venda vendaFechada = new Venda();
        vendaFechada.setSituacao(VendaSituacao.FECHADA);

        when(repositorioVenda.findByCodigoEquals(codigoVenda))
                .thenReturn(vendaFechada);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> servicoVenda.fechaVenda(codigoVenda, codigoTipoPagamento, valorProdutos, desconto, acrescimo, valoresParcelas, codigosTitulos));

        assertEquals("venda fechada", ex.getMessage());
    }

    @Test
    void testeFechaVendaSemValor() {
        Long codigoVenda = 1L;
        Long codigoTipoPagamento = 1L;
        Double valorProdutos = 0.0;
        Double desconto = 0.0;
        Double acrescimo = 0.0;
        String[] valoresParcelas = {"0.0"};
        String[] codigosTitulos = {"1"};

        Venda vendaAberta = new Venda();
        vendaAberta.setSituacao(VendaSituacao.ABERTA);

        when(repositorioVenda.findByCodigoEquals(codigoVenda))
                .thenReturn(vendaAberta);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> servicoVenda.fechaVenda(codigoVenda, codigoTipoPagamento, valorProdutos, desconto, acrescimo, valoresParcelas, codigosTitulos));
        assertEquals("Venda sem valor, verifique", ex.getMessage());
    }

    @Test
    void testeFechaVendaCartao() {
        Long codigoVenda = 1L;
        Long codigoTipoPagamento = 2L;
        Double valorProdutos = 100.0;
        Double desconto = 0.0;
        Double acrescimo = 0.0;
        String[] valoresParcelas = {"100.0"};
        String[] codigosTitulos = {"1"};

        Venda venda = new Venda();
        venda.setCodigo(codigoVenda);
        venda.setSituacao(VendaSituacao.ABERTA);
        venda.setValor_produtos(valorProdutos);
        Pessoa pessoa = new Pessoa();
        venda.setPessoa(pessoa);

        PagamentoTipo tipoPagamento = new PagamentoTipo();
        tipoPagamento.setCodigo(codigoTipoPagamento);
        tipoPagamento.setFormaPagamento("30"); // Pagamento a prazo - 30 dias

        Titulo titulo = new Titulo();
        titulo.setCodigo(1L);
        TituloTipo tipoTitulo = new TituloTipo();
        tipoTitulo.setSigla("CARTCRED");
        tipoTitulo.setDescricao("Crédito");
        titulo.setTipo(tipoTitulo);

        when(repositorioVenda.findByCodigoEquals(codigoVenda)).thenReturn(venda);
        when(servicoFormaPagamento.busca(codigoTipoPagamento)).thenReturn(tipoPagamento);
        when(servicoTitulo.busca(1L)).thenReturn(Optional.of(titulo));
        when(servicoCaixa.caixaIsAberto()).thenReturn(true);
        when(servicoCaixa.caixaAberto()).thenReturn(Optional.of(new Caixa()));

        String resultado = servicoVenda.fechaVenda(codigoVenda, codigoTipoPagamento, valorProdutos, desconto, acrescimo, valoresParcelas, codigosTitulos);
        assertEquals("Venda finalizada com sucesso", resultado);
        verify(servicoParcela).gerarParcela(
                eq(100.0), // valor_parcela
                eq(0.00), // desconto
                eq(0.00), // acrescimo
                eq(0.0), // valor_pago
                eq(100.0), // valor_restante
                any(Receber.class), // receber
                eq(0), // dias_atraso
                eq(1), // sequencia
                any(Timestamp.class), // data_cadastro
                any(Date.class) // data_vencimento
        );
    }

    @Test
    void testeAvistaDinheiroParcelaSemValor() {
        Long codigoVenda = 1L;
        Long codigoTipoPagamento = 2L;
        Double valorProdutos = 100.0;
        Double desconto = 0.0;
        Double acrescimo = 0.0;
        String[] valoresParcelas = {""};
        String[] codigosTitulos = {"1"};

        Venda venda = new Venda();
        venda.setCodigo(codigoVenda);
        venda.setSituacao(VendaSituacao.ABERTA);
        venda.setValor_produtos(valorProdutos);
        Pessoa pessoa = new Pessoa();
        venda.setPessoa(pessoa);

        PagamentoTipo tipoPagamento = new PagamentoTipo();
        tipoPagamento.setCodigo(codigoTipoPagamento);
        tipoPagamento.setFormaPagamento("00");

        Titulo titulo = new Titulo();
        titulo.setCodigo(1L);
        TituloTipo tipoTitulo = new TituloTipo();
        tipoTitulo.setSigla("DIN");
        tipoTitulo.setDescricao("Dinheiro");
        titulo.setTipo(tipoTitulo);

        when(repositorioVenda.findByCodigoEquals(codigoVenda)).thenReturn(venda);
        when(servicoFormaPagamento.busca(codigoTipoPagamento)).thenReturn(tipoPagamento);
        when(servicoTitulo.busca(1L)).thenReturn(Optional.of(titulo));
        when(servicoCaixa.caixaIsAberto()).thenReturn(true);
        when(servicoCaixa.caixaAberto()).thenReturn(Optional.of(new Caixa()));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                servicoVenda.fechaVenda(codigoVenda, codigoTipoPagamento, valorProdutos, desconto, acrescimo, valoresParcelas, codigosTitulos)
        );
        assertEquals("Parcela sem valor, verifique", ex.getMessage());
        verify(servicoLancamento, never()).lancamento(any());
    }

    @Test
    void testeAvistaDinheiroValorParcelasDiferente() {
        Long codigoVenda = 1L;
        Long codigoTipoPagamento = 2L;
        Double valorProdutos = 100.0;
        Double desconto = 0.0;
        Double acrescimo = 0.0;
        String[] valoresParcelas = {"50.0"};
        String[] codigosTitulos = {"1"};

        Venda venda = new Venda();
        venda.setCodigo(codigoVenda);
        venda.setSituacao(VendaSituacao.ABERTA);
        venda.setValor_produtos(valorProdutos);
        Pessoa pessoa = new Pessoa();
        venda.setPessoa(pessoa);

        PagamentoTipo tipoPagamento = new PagamentoTipo();
        tipoPagamento.setCodigo(codigoTipoPagamento);
        tipoPagamento.setFormaPagamento("00");

        Titulo titulo = new Titulo();
        titulo.setCodigo(1L);
        TituloTipo tipoTitulo = new TituloTipo();
        tipoTitulo.setSigla("DIN");
        tipoTitulo.setDescricao("Dinheiro");
        titulo.setTipo(tipoTitulo);

        when(repositorioVenda.findByCodigoEquals(codigoVenda)).thenReturn(venda);
        when(servicoFormaPagamento.busca(codigoTipoPagamento)).thenReturn(tipoPagamento);
        when(servicoTitulo.busca(1L)).thenReturn(Optional.of(titulo));
        when(servicoCaixa.caixaIsAberto()).thenReturn(true);
        when(servicoCaixa.caixaAberto()).thenReturn(Optional.of(new Caixa()));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                servicoVenda.fechaVenda(codigoVenda, codigoTipoPagamento, valorProdutos, desconto, acrescimo, valoresParcelas, codigosTitulos)
        );
        assertEquals("Valor das parcelas diferente do valor total de produtos, verifique", ex.getMessage());
        verify(servicoLancamento, never()).lancamento(any());
    }

    @Test
    void testeAvistaDinheiroErroLancamento() {
        Long codigoVenda = 1L;
        Long codigoTipoPagamento = 2L;
        Double valorProdutos = 100.0;
        Double desconto = 0.0;
        Double acrescimo = 0.0;
        String[] valoresParcelas = {"100.0"};
        String[] codigosTitulos = {"1"};

        Venda venda = new Venda();
        venda.setCodigo(codigoVenda);
        venda.setSituacao(VendaSituacao.ABERTA);
        venda.setValor_produtos(valorProdutos);
        Pessoa pessoa = new Pessoa();
        venda.setPessoa(pessoa);

        PagamentoTipo tipoPagamento = new PagamentoTipo();
        tipoPagamento.setCodigo(codigoTipoPagamento);
        tipoPagamento.setFormaPagamento("00");

        Titulo titulo = new Titulo();
        titulo.setCodigo(1L);
        TituloTipo tipoTitulo = new TituloTipo();
        tipoTitulo.setSigla("DIN");
        tipoTitulo.setDescricao("Dinheiro");
        titulo.setTipo(tipoTitulo);

        Usuario usuario = new Usuario();
        usuario.setCodigo(1L);
        Caixa caixa = new Caixa();
        caixa.setCodigo(1L);

        when(repositorioVenda.findByCodigoEquals(codigoVenda)).thenReturn(venda);
        when(servicoFormaPagamento.busca(codigoTipoPagamento)).thenReturn(tipoPagamento);
        when(servicoTitulo.busca(1L)).thenReturn(Optional.of(titulo));
        when(servicoUsuario.buscaUsuario("Teste Erik")).thenReturn(usuario);
        when(servicoCaixa.caixaAberto()).thenReturn(Optional.of(caixa));
        when(servicoCaixa.caixaIsAberto()).thenReturn(true);
        doThrow(new RuntimeException())
                .when(servicoLancamento).lancamento(any());
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                servicoVenda.fechaVenda(codigoVenda, codigoTipoPagamento, valorProdutos, desconto, acrescimo, valoresParcelas, codigosTitulos)
        );
        assertEquals("Erro ao fechar a venda, chame o suporte", ex.getMessage());
    }

    @Test
    void testeQtdAbertosVendasAbertas() {
        int quantidadeEsperada = 5;
        when(repositorioVenda.qtdVendasEmAberto()).thenReturn(quantidadeEsperada);

        int resultado = servicoVenda.qtdAbertos();

        assertEquals(quantidadeEsperada, resultado);
        verify(repositorioVenda).qtdVendasEmAberto();
    }

    @Test
    void testeQtdAbertosSemVendasAbertas() {
        int quantidadeEsperada = 0;
        when(repositorioVenda.qtdVendasEmAberto()).thenReturn(quantidadeEsperada);

        int resultado = servicoVenda.qtdAbertos();

        assertEquals(quantidadeEsperada, resultado);
        verify(repositorioVenda).qtdVendasEmAberto();
    }

    @Test
    void testeAprazoParcelaSemValor() {
        Long codigoVenda = 1L;
        Long codigoTipoPagamento = 2L;
        Double valorProdutos = 100.0;
        Double desconto = 0.0;
        Double acrescimo = 0.0;
        String[] valoresParcelas = {""};
        String[] codigosTitulos = {"1"};

        Venda venda = new Venda();
        venda.setCodigo(codigoVenda);
        venda.setSituacao(VendaSituacao.ABERTA);
        venda.setValor_produtos(valorProdutos);
        Pessoa pessoa = new Pessoa();
        venda.setPessoa(pessoa);

        PagamentoTipo tipoPagamento = new PagamentoTipo();
        tipoPagamento.setCodigo(codigoTipoPagamento);
        tipoPagamento.setFormaPagamento("30");

        Titulo titulo = new Titulo();
        titulo.setCodigo(1L);
        TituloTipo tipoTitulo = new TituloTipo();
        tipoTitulo.setSigla("CARTCRED");
        tipoTitulo.setDescricao("Crédito");
        titulo.setTipo(tipoTitulo);

        when(repositorioVenda.findByCodigoEquals(codigoVenda)).thenReturn(venda);
        when(servicoFormaPagamento.busca(codigoTipoPagamento)).thenReturn(tipoPagamento);
        when(servicoTitulo.busca(1L)).thenReturn(Optional.of(titulo));
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                servicoVenda.fechaVenda(codigoVenda, codigoTipoPagamento, valorProdutos, desconto, acrescimo, valoresParcelas, codigosTitulos)
        );
        assertEquals("valor de recebimento invalido", ex.getMessage());
    }

    @Test
    void testeFechaVendaSemCliente() {
        Long codigoVenda = 1L;
        Long codigoTipoPagamento = 2L;
        Double valorProdutos = 100.0;
        Double desconto = 0.0;
        Double acrescimo = 0.0;
        String[] valoresParcelas = {"100.0"};
        String[] codigosTitulos = {"1"};

        Venda venda = new Venda();
        venda.setCodigo(codigoVenda);
        venda.setSituacao(VendaSituacao.ABERTA);
        venda.setValor_produtos(valorProdutos);
        venda.setPessoa(null); // Venda sem cliente

        PagamentoTipo tipoPagamento = new PagamentoTipo();
        tipoPagamento.setCodigo(codigoTipoPagamento);
        tipoPagamento.setFormaPagamento("30");

        Titulo titulo = new Titulo();
        titulo.setCodigo(1L);
        TituloTipo tipoTitulo = new TituloTipo();
        tipoTitulo.setSigla("CARTCRED");
        tipoTitulo.setDescricao("Crédito");
        titulo.setTipo(tipoTitulo);

        when(repositorioVenda.findByCodigoEquals(codigoVenda)).thenReturn(venda);
        when(servicoFormaPagamento.busca(codigoTipoPagamento)).thenReturn(tipoPagamento);
        when(servicoTitulo.busca(1L)).thenReturn(Optional.of(titulo));
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                servicoVenda.fechaVenda(codigoVenda, codigoTipoPagamento, valorProdutos, desconto, acrescimo, valoresParcelas, codigosTitulos)
        );
        assertEquals("Venda sem cliente, verifique", ex.getMessage());
    }

    @Test
    void testeFechaVendaCaixaFechado() {
        Long codigoVenda = 1L;
        Long codigoTipoPagamento = 2L;
        Double valorProdutos = 100.0;
        Double desconto = 0.0;
        Double acrescimo = 0.0;
        String[] valoresParcelas = {"100.0"};
        String[] codigosTitulos = {"1"};

        Venda venda = new Venda();
        venda.setCodigo(codigoVenda);
        venda.setSituacao(VendaSituacao.ABERTA);
        venda.setValor_produtos(valorProdutos);
        Pessoa pessoa = new Pessoa();
        venda.setPessoa(pessoa);

        PagamentoTipo tipoPagamento = new PagamentoTipo();
        tipoPagamento.setCodigo(codigoTipoPagamento);
        tipoPagamento.setFormaPagamento("00");

        Titulo titulo = new Titulo();
        titulo.setCodigo(1L);
        TituloTipo tipoTitulo = new TituloTipo();
        tipoTitulo.setSigla("DIN");
        tipoTitulo.setDescricao("Dinheiro");
        titulo.setTipo(tipoTitulo);

        when(repositorioVenda.findByCodigoEquals(codigoVenda)).thenReturn(venda);
        when(servicoFormaPagamento.busca(codigoTipoPagamento)).thenReturn(tipoPagamento);
        when(servicoTitulo.busca(1L)).thenReturn(Optional.of(titulo));
        when(servicoCaixa.caixaIsAberto()).thenReturn(false);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                servicoVenda.fechaVenda(codigoVenda, codigoTipoPagamento, valorProdutos, desconto, acrescimo, valoresParcelas, codigosTitulos)
        );
        assertEquals("nenhum caixa aberto", ex.getMessage());
    }
}