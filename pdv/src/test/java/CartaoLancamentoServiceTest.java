import net.originmobi.pdv.service.cartao.CartaoLancamentoService;
import net.originmobi.pdv.enumerado.cartao.CartaoSituacao;
import net.originmobi.pdv.enumerado.cartao.CartaoTipo;
import net.originmobi.pdv.filter.CartaoFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.model.Titulo;
import net.originmobi.pdv.model.TituloTipo;
import net.originmobi.pdv.model.cartao.CartaoLancamento;
import net.originmobi.pdv.model.cartao.MaquinaCartao;
import net.originmobi.pdv.repository.cartao.CartaoLancamentoRepository;
import net.originmobi.pdv.service.CaixaLancamentoService;
import net.originmobi.pdv.service.UsuarioService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CartaoLancamentoServiceTest {

    @Mock
    private CartaoLancamentoRepository repo;

    @Mock
    private CaixaLancamentoService caixaLancamentoService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private CartaoLancamentoService service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testeListarLancamentosComFiltros() {
        CartaoFilter filter = new CartaoFilter();
        filter.setSituacao(CartaoSituacao.PROCESSADO);
        filter.setTipo(CartaoTipo.DEBITO);
        filter.setData_recebimento("2024-05-01");

        service.listar(filter);

        verify(repo).buscaLancamentos("PROCESSADO", "DEBITO", "2024-05-01");
    }

    @Test
    public void testeRealizarLancamentoComTituloDebito() {
        MaquinaCartao maquina = new MaquinaCartao();
        maquina.setTaxa_debito(2.5);
        maquina.setDias_debito(2);
        maquina.setTaxa_antecipacao(1.0);

        Titulo titulo = mock(Titulo.class);
        TituloTipo tipo = mock(TituloTipo.class);
        when(tipo.getSigla()).thenReturn("CARTDEB");
        when(titulo.getTipo()).thenReturn(tipo);
        when(titulo.getMaquina()).thenReturn(maquina);

        service.lancamento(100.0, Optional.of(titulo));

        verify(repo).save(any(CartaoLancamento.class));
    }

    @Test
    public void testeProcessarCartaoLancamentoComSucesso() {
        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.APROCESSAR);
        when(cartao.getVlLiqParcela()).thenReturn(100.0);

        Caixa caixa = mock(Caixa.class);
        MaquinaCartao maquina = mock(MaquinaCartao.class);
        when(maquina.getBanco()).thenReturn(caixa);
        when(cartao.getMaquina_cartao()).thenReturn(maquina);

        Usuario usuario = new Usuario();
        when(usuarioService.buscaUsuario(anyString())).thenReturn(usuario);

        String resultado = service.processar(cartao);

        verify(cartao).setSituacao(CartaoSituacao.PROCESSADO);
        assertEquals("Processamento realizado com sucesso", resultado);
    }

    @Test
    public void testeAnteciparCartaoLancamentoComSucesso() {
        // Cria o Authentication
        Authentication auth = new UsernamePasswordAuthenticationToken("admin", null);
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        CartaoLancamento cartao = mock(CartaoLancamento.class);
        when(cartao.getSituacao()).thenReturn(CartaoSituacao.APROCESSAR);
        when(cartao.getVlLiqAntecipacao()).thenReturn(95.0);
        when(cartao.getCodigo()).thenReturn(123L);

        MaquinaCartao maquina = mock(MaquinaCartao.class);
        when(maquina.getBanco()).thenReturn(mock(Caixa.class));
        when(cartao.getMaquina_cartao()).thenReturn(maquina);

        when(usuarioService.buscaUsuario(anyString())).thenReturn(new Usuario());

        String resultado = service.antecipar(cartao);

        verify(cartao).setSituacao(CartaoSituacao.ANTECIPADO);
        verify(repo).save(cartao);
        assertEquals("Antecipação realizada com sucesso", resultado);

        // Limpa o contexto para evitar interferência em outros testes
        SecurityContextHolder.clearContext();
    }
}