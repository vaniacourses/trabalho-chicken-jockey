package net.originmobi.pdv.service;

import net.originmobi.pdv.enumerado.cartao.CartaoSituacao;
import net.originmobi.pdv.enumerado.cartao.CartaoTipo;
import net.originmobi.pdv.filter.CartaoFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.Titulo;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.model.cartao.CartaoLancamento;
import net.originmobi.pdv.model.cartao.MaquinaCartao;
import net.originmobi.pdv.repository.cartao.CartaoLancamentoRepository;
import net.originmobi.pdv.service.cartao.CartaoLancamentoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Date;

class CartaoLancamentoServiceTest {

    @Mock
    private CartaoLancamentoRepository repository;

    @Mock
    private CaixaLancamentoService caixaLancamentoService;

    @Mock
    private UsuarioService usuarioService;
    
    @Captor
    private ArgumentCaptor<CartaoLancamento> cartaoLancamentoCaptor;
    
    @Captor
    private ArgumentCaptor<CaixaLancamento> caixaLancamentoCaptor;

    @InjectMocks
    private CartaoLancamentoService service;
    
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Configurar autenticação para testes
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", null);
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void testeListarLancamentosComFiltrosCompletos() {
        CartaoFilter filter = new CartaoFilter();
        filter.setSituacao(CartaoSituacao.PROCESSADO);
        filter.setTipo(CartaoTipo.DEBITO);
        filter.setData_recebimento("2024-05-01");

        CartaoLancamento lancamento1 = new CartaoLancamento(
        	    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        	    null,
        	    CartaoTipo.DEBITO,
        	    CartaoSituacao.PROCESSADO,
        	    Date.valueOf("2024-05-01"),
        	    Date.valueOf("2024-05-01")
        	);
        CartaoLancamento lancamento2 = new CartaoLancamento(
        	    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
        	    null,
        	    CartaoTipo.DEBITO,
        	    CartaoSituacao.PROCESSADO,
        	    Date.valueOf("2024-05-01"),
        	    Date.valueOf("2024-05-01")
        	);
        List<CartaoLancamento> lancamentos = Arrays.asList(lancamento1, lancamento2);
        
        when(repository.buscaLancamentos("PROCESSADO", "DEBITO", "2024-05-01")).thenReturn(lancamentos);

        List<CartaoLancamento> resultado = service.listar(filter);
        
        verify(repository).buscaLancamentos("PROCESSADO", "DEBITO", "2024-05-01");
        assertEquals(2, resultado.size());
        assertEquals(lancamentos, resultado);
    }

    @Test
    void testeListarLancamentosSemFiltros() {
        CartaoFilter filter = new CartaoFilter();
        
        List<CartaoLancamento> lancamentos = new ArrayList<>();
        when(repository.buscaLancamentos("%", "%", "%")).thenReturn(lancamentos);

        List<CartaoLancamento> resultado = service.listar(filter);
        
        verify(repository).buscaLancamentos("%", "%", "%");
        assertEquals(lancamentos, resultado);
    }
    
    @Test
    void testeListarLancamentosComDataRecebimentoVazia() {
        CartaoFilter filter = new CartaoFilter();
        filter.setSituacao(CartaoSituacao.APROCESSAR);
        filter.setTipo(CartaoTipo.CREDITO);
        filter.setData_recebimento("");
        
        List<CartaoLancamento> lancamentos = new ArrayList<>();
        when(repository.buscaLancamentos("APROCESSAR", "CREDITO", "%")).thenReturn(lancamentos);
        
        service.listar(filter);
        
        verify(repository).buscaLancamentos("APROCESSAR", "CREDITO", "%");
    }
    
    @Test
    void testeListarLancamentosComDataRecebimentoFormatada() {
        CartaoFilter filter = new CartaoFilter();
        filter.setSituacao(null);
        filter.setTipo(null);
        filter.setData_recebimento("15/05/2023");
        
        List<CartaoLancamento> lancamentos = new ArrayList<>();
        when(repository.buscaLancamentos("%", "%", "15-05-2023")).thenReturn(lancamentos);
        
        service.listar(filter);
        
        verify(repository).buscaLancamentos("%", "%", "15-05-2023");
    }

    @Test
    void testeRealizarLancamentoComTituloDebito() {
        // Arrange
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_debito(2.5);
        maquina.setDias_debito(2);
        maquina.setTaxa_antecipacao(1.0);

        net.originmobi.pdv.model.TituloTipo tituloTipo = mock(net.originmobi.pdv.model.TituloTipo.class);
        when(tituloTipo.getSigla()).thenReturn("CARTDEB");

        Titulo titulo = mock(Titulo.class);
        when(titulo.getMaquina()).thenReturn(maquina);
        when(titulo.getTipo()).thenReturn(tituloTipo);
        
        // Act
        service.lancamento(100.0, Optional.of(titulo));
        
        // Assert
        verify(repository).save(any(CartaoLancamento.class));
    }

    @Test
    void testeRealizarLancamentoComTituloCredito() {
        // Arrange
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_credito(3.5);
        maquina.setDias_credito(30);
        maquina.setTaxa_antecipacao(1.5);

        net.originmobi.pdv.model.TituloTipo tituloTipo = mock(net.originmobi.pdv.model.TituloTipo.class);
        when(tituloTipo.getSigla()).thenReturn("CARTCRED");

        Titulo titulo = mock(Titulo.class);
        when(titulo.getMaquina()).thenReturn(maquina);
        when(titulo.getTipo()).thenReturn(tituloTipo);
        
        // Act
        service.lancamento(200.0, Optional.of(titulo));
        
        // Assert
        verify(repository).save(any(CartaoLancamento.class));
    }

    @Test
    void testeRealizarLancamentoComErroAoSalvar() {
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_debito(2.5);
        maquina.setDias_debito(2);
        maquina.setTaxa_antecipacao(1.0);

        net.originmobi.pdv.model.TituloTipo tituloTipo = mock(net.originmobi.pdv.model.TituloTipo.class);
        when(tituloTipo.getSigla()).thenReturn("CARTDEB");

        Titulo titulo = mock(Titulo.class);
        when(titulo.getMaquina()).thenReturn(maquina);
        when(titulo.getTipo()).thenReturn(tituloTipo);
        
        // Simula uma exceção ao salvar
        doThrow(new RuntimeException("Erro ao salvar")).when(repository).save(any(CartaoLancamento.class));

        // Não deve lançar exceção, apenas logar o erro
        service.lancamento(100.0, Optional.of(titulo));

        // Verifica que o método save foi chamado
        verify(repository).save(any(CartaoLancamento.class));
    }

    @Test
    void testeProcessarCartaoLancamentoComSucesso() {
        // Arrange
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.APROCESSAR);
        when(cartao.getVlLiqParcela()).thenReturn(100.0);

        Caixa caixa = mock(Caixa.class);
        MaquinaCartao maquina = mock(MaquinaCartao.class);
        when(maquina.getBanco()).thenReturn(caixa);
        when(cartao.getMaquina_cartao()).thenReturn(maquina);

        Usuario usuario = new Usuario();
        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);

        // Act
        String resultado = service.processar(cartao);

        // Assert
        verify(cartao).setSituacao(CartaoSituacao.PROCESSADO);
        verify(caixaLancamentoService).lancamento(any(CaixaLancamento.class));
        assertEquals("Processamento realizado com sucesso", resultado);
    }

    @Test
    void testeProcessarCartaoLancamentoErroAoSetarSituacao() {
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.APROCESSAR);
        when(cartao.getVlLiqParcela()).thenReturn(100.0);

        Caixa caixa = mock(Caixa.class);
        MaquinaCartao maquina = mock(MaquinaCartao.class);
        when(maquina.getBanco()).thenReturn(caixa);
        when(cartao.getMaquina_cartao()).thenReturn(maquina);

        Usuario usuario = new Usuario();
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);

        // Simula uma exceção ao chamar setSituacao
        doThrow(new RuntimeException("Erro ao setar situação")).when(cartao).setSituacao(any(CartaoSituacao.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.processar(cartao));
        assertEquals("Erro ao tentar realizar o processamento, chame o suporte", exception.getMessage());
        
        // Verifica que o lançamento foi realizado antes do erro
        verify(caixaLancamentoService).lancamento(any());
    }

    @Test
    void testeAnteciparCartaoLancamentoComSucesso() {
        // Arrange
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.APROCESSAR);
        when(cartao.getVlLiqAntecipacao()).thenReturn(95.0);
        when(cartao.getCodigo()).thenReturn(123L);

        Caixa caixa = mock(Caixa.class);
        MaquinaCartao maquina = mock(MaquinaCartao.class);
        when(maquina.getBanco()).thenReturn(caixa);
        when(cartao.getMaquina_cartao()).thenReturn(maquina);

        Usuario usuario = new Usuario();
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);

        // Act
        String resultado = service.antecipar(cartao);

        // Assert
        verify(cartao).setSituacao(CartaoSituacao.ANTECIPADO);
        verify(repository).save(cartao);
        verify(caixaLancamentoService).lancamento(any(CaixaLancamento.class));
        assertEquals("Antecipação realizada com sucesso", resultado);
    }

    @Test
    void testeProcessarCartaoLancamentoJaProcessado() {
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.PROCESSADO);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.processar(cartao));
        assertEquals("Registro já processado", exception.getMessage());
    }

    @Test
    void testeProcessarCartaoLancamentoJaAntecipado() {
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.ANTECIPADO);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.processar(cartao));
        assertEquals("Registro já foi antecipado", exception.getMessage());
    }
    @Test
    void testeAnteciparCartaoLancamentoJaProcessado() {
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.PROCESSADO);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.antecipar(cartao));
        assertEquals("Registro já processado", exception.getMessage());
    }

    @Test
    void testeAnteciparCartaoLancamentoJaAntecipado() {
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.ANTECIPADO);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.antecipar(cartao));
        assertEquals("Registro já foi antecipado", exception.getMessage());
    }

    @Test
    void testeProcessarCartaoLancamentoErroAoRealizarLancamento() {
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.APROCESSAR);
        when(cartao.getVlLiqParcela()).thenReturn(100.0);

        MaquinaCartao maquina = mock(MaquinaCartao.class);
        when(maquina.getBanco()).thenReturn(mock(Caixa.class));
        when(cartao.getMaquina_cartao()).thenReturn(maquina);

        Usuario usuario = new Usuario();
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);

        doThrow(new RuntimeException("Erro ao realizar lançamento")).when(caixaLancamentoService).lancamento(any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.processar(cartao));
        assertEquals("Erro ao tentar realizar o processamento, chame o suporte", exception.getMessage());
    }

    @Test
    void testeAnteciparCartaoLancamentoErroAoRealizarLancamento() {
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.APROCESSAR);
        when(cartao.getVlLiqAntecipacao()).thenReturn(95.0);
        when(cartao.getCodigo()).thenReturn(123L);

        MaquinaCartao maquina = mock(MaquinaCartao.class);
        when(maquina.getBanco()).thenReturn(mock(Caixa.class));
        when(cartao.getMaquina_cartao()).thenReturn(maquina);

        Usuario usuario = new Usuario();
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);

        doThrow(new RuntimeException("Erro ao realizar lançamento")).when(caixaLancamentoService).lancamento(any());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.antecipar(cartao));
        assertEquals("Erro ao tentar realizar a antecipação, chame o suporte", exception.getMessage());
    }

    @Test
    void testeAnteciparCartaoLancamentoErroAoSalvar() {
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.APROCESSAR);
        when(cartao.getVlLiqAntecipacao()).thenReturn(95.0);
        when(cartao.getCodigo()).thenReturn(123L);

        MaquinaCartao maquina = mock(MaquinaCartao.class);
        when(maquina.getBanco()).thenReturn(mock(Caixa.class));
        when(cartao.getMaquina_cartao()).thenReturn(maquina);

        Usuario usuario = new Usuario();
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);

        doThrow(new RuntimeException("Erro ao salvar")).when(repository).save(any(CartaoLancamento.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> service.antecipar(cartao));
        assertEquals("Erro ao tentar realizar a antecipação, chame o suporte", exception.getMessage());
    }
    
    // Testes específicos para detectar mutações no código
    
    /**
     * Testa a mutação da linha 63: Negou a condição (if (cond) virou if (!cond))
     * Verifica se o tipo de cartão é corretamente definido como DEBITO quando o título é do tipo CARTDEB
     */
    @Test
    void testeMutacaoLinha63_CondicaoNegada() {
        // Arrange
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_debito(2.5);
        maquina.setDias_debito(2);
        maquina.setTaxa_antecipacao(1.0);

        net.originmobi.pdv.model.TituloTipo tituloTipo = mock(net.originmobi.pdv.model.TituloTipo.class);
        when(tituloTipo.getSigla()).thenReturn("CARTDEB");

        Titulo titulo = mock(Titulo.class);
        when(titulo.getMaquina()).thenReturn(maquina);
        when(titulo.getTipo()).thenReturn(tituloTipo);

        // Act
        service.lancamento(100.0, Optional.of(titulo));

        // Assert
        verify(repository).save(cartaoLancamentoCaptor.capture());
        CartaoLancamento lancamentoSalvo = cartaoLancamentoCaptor.getValue();
        
        // Verifica se o tipo foi definido como DEBITO
        assertEquals(CartaoTipo.DEBITO, lancamentoSalvo.getTipo());
    }

    /**
     * Testa a mutação da linha 69: Substituiu uma multiplicação de double por divisão
     * Verifica se o cálculo do valor da taxa está correto
     */
    @Test
    void testeMutacaoLinha69_MultiplicacaoPorDivisao() {
        // Arrange
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_debito(10.0); // 10% de taxa
        maquina.setDias_debito(2);
        maquina.setTaxa_antecipacao(1.0);

        net.originmobi.pdv.model.TituloTipo tituloTipo = mock(net.originmobi.pdv.model.TituloTipo.class);
        when(tituloTipo.getSigla()).thenReturn("CARTDEB");

        Titulo titulo = mock(Titulo.class);
        when(titulo.getMaquina()).thenReturn(maquina);
        when(titulo.getTipo()).thenReturn(tituloTipo);

        // Act
        service.lancamento(100.0, Optional.of(titulo));

        // Assert
        verify(repository).save(cartaoLancamentoCaptor.capture());
        CartaoLancamento lancamentoSalvo = cartaoLancamentoCaptor.getValue();
        
        // Verifica se o valor da taxa está correto: (100 * 10) / 100 = 10.0
        assertEquals(10.0, lancamentoSalvo.getVlTaxa(), 0.001);
    }

    /**
     * Testa a mutação da linha 69: Substituiu uma divisão de double por multiplicação
     * Verifica se o cálculo do valor da taxa está correto
     */
    @Test
    void testeMutacaoLinha69_DivisaoPorMultiplicacao() {
        // Arrange
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_debito(10.0); // 10% de taxa
        maquina.setDias_debito(2);
        maquina.setTaxa_antecipacao(1.0);

        net.originmobi.pdv.model.TituloTipo tituloTipo = mock(net.originmobi.pdv.model.TituloTipo.class);
        when(tituloTipo.getSigla()).thenReturn("CARTDEB");

        Titulo titulo = mock(Titulo.class);
        when(titulo.getMaquina()).thenReturn(maquina);
        when(titulo.getTipo()).thenReturn(tituloTipo);

        // Act
        service.lancamento(100.0, Optional.of(titulo));

        // Assert
        verify(repository).save(cartaoLancamentoCaptor.capture());
        CartaoLancamento lancamentoSalvo = cartaoLancamentoCaptor.getValue();
        
        // Se a divisão fosse substituída por multiplicação, o valor seria muito maior
        // (100 * 10) * 100 = 100000.0
        assertNotEquals(100000.0, lancamentoSalvo.getVlTaxa());
        assertEquals(10.0, lancamentoSalvo.getVlTaxa(), 0.001);
    }

    /**
     * Testa a mutação da linha 70: Substituiu uma subtração de double por adição
     * Verifica se o cálculo do valor líquido da parcela está correto
     */
    @Test
    void testeMutacaoLinha70_SubtracaoPorAdicao() {
        // Arrange
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_debito(10.0); // 10% de taxa
        maquina.setDias_debito(2);
        maquina.setTaxa_antecipacao(1.0);

        net.originmobi.pdv.model.TituloTipo tituloTipo = mock(net.originmobi.pdv.model.TituloTipo.class);
        when(tituloTipo.getSigla()).thenReturn("CARTDEB");

        Titulo titulo = mock(Titulo.class);
        when(titulo.getMaquina()).thenReturn(maquina);
        when(titulo.getTipo()).thenReturn(tituloTipo);

        // Act
        service.lancamento(100.0, Optional.of(titulo));

        // Assert
        verify(repository).save(cartaoLancamentoCaptor.capture());
        CartaoLancamento lancamentoSalvo = cartaoLancamentoCaptor.getValue();
        
        // Verifica se o valor líquido está correto: 100 - 10 = 90.0
        // Se fosse adição: 100 + 10 = 110.0
        assertEquals(90.0, lancamentoSalvo.getVlLiqParcela(), 0.001);
        assertNotEquals(110.0, lancamentoSalvo.getVlLiqParcela());
    }

    /**
     * Testa as mutações das linhas 73 e 74: Substituiu uma divisão por multiplicação e subtração por adição
     * Verifica se o cálculo do valor da taxa de antecipação e valor líquido de antecipação estão corretos
     */
    @Test
    void testeMutacaoLinhas73e74_CalculoAntecipacao() {
        // Arrange
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_debito(10.0);
        maquina.setDias_debito(2);
        maquina.setTaxa_antecipacao(5.0); // 5% de taxa de antecipação

        net.originmobi.pdv.model.TituloTipo tituloTipo = mock(net.originmobi.pdv.model.TituloTipo.class);
        when(tituloTipo.getSigla()).thenReturn("CARTDEB");

        Titulo titulo = mock(Titulo.class);
        when(titulo.getMaquina()).thenReturn(maquina);
        when(titulo.getTipo()).thenReturn(tituloTipo);

        // Act
        service.lancamento(100.0, Optional.of(titulo));

        // Assert
        verify(repository).save(cartaoLancamentoCaptor.capture());
        CartaoLancamento lancamentoSalvo = cartaoLancamentoCaptor.getValue();
        
        // Verifica se o valor da taxa de antecipação está correto: (100 * 5) / 100 = 5.0
        // Se fosse multiplicação: (100 * 5) * 100 = 50000.0
        assertEquals(5.0, lancamentoSalvo.getVlTaxaAntecipacao(), 0.001);
        assertNotEquals(50000.0, lancamentoSalvo.getVlTaxaAntecipacao());
        
        // Verifica se o valor líquido de antecipação está correto: 100 - 5 = 95.0
        // Se fosse adição: 100 + 5 = 105.0
        assertEquals(95.0, lancamentoSalvo.getVlLiqAntecipacao(), 0.001);
        assertNotEquals(105.0, lancamentoSalvo.getVlLiqAntecipacao());
    }

    /**
     * Testa a mutação da linha 89: Removeu a chamada ao System.out.println()
     * Verifica se a exceção é capturada e o erro é logado
     */
    @Test
    void testeMutacaoLinha89_RemocaoSystemOut() {
        // Arrange
        System.setOut(new PrintStream(outContent));
        
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_debito(10.0);
        maquina.setDias_debito(2);
        maquina.setTaxa_antecipacao(5.0);

        net.originmobi.pdv.model.TituloTipo tituloTipo = mock(net.originmobi.pdv.model.TituloTipo.class);
        when(tituloTipo.getSigla()).thenReturn("CARTDEB");

        Titulo titulo = mock(Titulo.class);
        when(titulo.getMaquina()).thenReturn(maquina);
        when(titulo.getTipo()).thenReturn(tituloTipo);
        
        // Simula uma exceção ao salvar
        doThrow(new RuntimeException("Erro ao salvar")).when(repository).save(any(CartaoLancamento.class));

        try {
            // Act
            service.lancamento(100.0, Optional.of(titulo));
            
            // Assert
            // Verifica se algo foi impresso no console
            assertTrue(outContent.toString().contains("Erro ao salvar") || 
                       outContent.toString().contains("java.lang.RuntimeException"),
                      "A exceção deveria ser logada no console");
        } finally {
            System.setOut(originalOut);
        }
    }

    /**
     * Testa o comportamento quando o título é do tipo CARTCRED
     * Verifica se os cálculos são feitos com as taxas de crédito
     */
    @Test
    void testeLancamentoComTituloCredito_VerificaCalculos() {
        // Arrange
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_credito(15.0); // 15% de taxa de crédito
        maquina.setDias_credito(30);
        maquina.setTaxa_antecipacao(7.0); // 7% de taxa de antecipação

        net.originmobi.pdv.model.TituloTipo tituloTipo = mock(net.originmobi.pdv.model.TituloTipo.class);
        when(tituloTipo.getSigla()).thenReturn("CARTCRED");

        Titulo titulo = mock(Titulo.class);
        when(titulo.getMaquina()).thenReturn(maquina);
        when(titulo.getTipo()).thenReturn(tituloTipo);

        // Act
        service.lancamento(200.0, Optional.of(titulo));

        // Assert 
        verify(repository).save(cartaoLancamentoCaptor.capture());
        CartaoLancamento lancamentoSalvo = cartaoLancamentoCaptor.getValue();
        
        // Verifica se o tipo foi definido como CREDITO
        assertEquals(CartaoTipo.CREDITO, lancamentoSalvo.getTipo());
        
        // Verifica se o valor da taxa está correto: (200 * 15) / 100 = 30.0
        assertEquals(30.0, lancamentoSalvo.getVlTaxa(), 0.001);
        
        // Verifica se o valor líquido está correto: 200 - 30 = 170.0
        assertEquals(170.0, lancamentoSalvo.getVlLiqParcela(), 0.001);
        
        // Verifica se o valor da taxa de antecipação está correto: (200 * 7) / 100 = 14.0
        assertEquals(14.0, lancamentoSalvo.getVlTaxaAntecipacao(), 0.001);
        
        // Verifica se o valor líquido de antecipação está correto: 200 - 14 = 186.0
        assertEquals(186.0, lancamentoSalvo.getVlLiqAntecipacao(), 0.001);
    }
}