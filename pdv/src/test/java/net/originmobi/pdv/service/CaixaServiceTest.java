package net.originmobi.pdv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import net.originmobi.pdv.enumerado.caixa.CaixaTipo;
import net.originmobi.pdv.filter.BancoFilter;
import net.originmobi.pdv.filter.CaixaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.CaixaRepository;

class CaixaServiceTest {

    @InjectMocks
    private CaixaService caixaService;

    @Mock
    private CaixaRepository caixaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CaixaLancamentoService caixaLancamentoService;

    @Mock
    private Usuario usuario;

    @Mock
    private Caixa caixa;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", "password")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void caixaIsAberto_deveRetornarTrueQuandoExisteCaixaAberto() {
        when(caixaRepository.caixaAberto()).thenReturn(Optional.of(new Caixa()));
        boolean resultado = caixaService.caixaIsAberto();
        assertTrue(resultado);
        verify(caixaRepository, times(1)).caixaAberto();
    }

    @Test
    void caixaIsAberto_deveRetornarFalseQuandoNaoExisteCaixaAberto() {
        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());
        boolean resultado = caixaService.caixaIsAberto();
        assertFalse(resultado);
        verify(caixaRepository, times(1)).caixaAberto();
    }

    @Test
    void listaTodos_deveRetornarLista() {
        when(caixaRepository.findByCodigoOrdenado()).thenReturn(Arrays.asList(new Caixa(), new Caixa()));
        List<Caixa> resultado = caixaService.listaTodos();
        assertEquals(2, resultado.size());
        verify(caixaRepository, times(1)).findByCodigoOrdenado();
    }

    @Test
    void listarCaixas_comData() {
        CaixaFilter filtro = new CaixaFilter();
        filtro.setData_cadastro("2024/05/20");

        Date data = Date.valueOf("2024-05-20");
        when(caixaRepository.buscaCaixasPorDataAbertura(data)).thenReturn(Arrays.asList(new Caixa()));

        List<Caixa> resultado = caixaService.listarCaixas(filtro);
        assertEquals(1, resultado.size());
        verify(caixaRepository, times(1)).buscaCaixasPorDataAbertura(data);
    }

    @Test
    void listarCaixas_semData() {
        CaixaFilter filtro = new CaixaFilter(); 
        when(caixaRepository.listaCaixasAbertos()).thenReturn(Arrays.asList(new Caixa()));

        List<Caixa> resultado = caixaService.listarCaixas(filtro);
        assertEquals(1, resultado.size());
        verify(caixaRepository, times(1)).listaCaixasAbertos();
    }

    @Test
    void listarCaixas_comDataVazia() {
        CaixaFilter filtro = new CaixaFilter();
        filtro.setData_cadastro("");
        when(caixaRepository.listaCaixasAbertos()).thenReturn(Arrays.asList(new Caixa()));

        List<Caixa> resultado = caixaService.listarCaixas(filtro);
        assertEquals(1, resultado.size());
        verify(caixaRepository, times(1)).listaCaixasAbertos();
    }

    @Test
    void caixaAberto_deveRetornarOptional() {
        Optional<Caixa> caixaOptional = Optional.of(new Caixa());
        when(caixaRepository.caixaAberto()).thenReturn(caixaOptional);
        Optional<Caixa> resultado = caixaService.caixaAberto();
        assertTrue(resultado.isPresent());
    }

    @Test
    void caixasAbertos_deveRetornarLista() {
        when(caixaRepository.caixasAbertos()).thenReturn(Arrays.asList(new Caixa(), new Caixa()));
        List<Caixa> caixas = caixaService.caixasAbertos();
        assertEquals(2, caixas.size());
        verify(caixaRepository).caixasAbertos();
    }

    @Test
    void busca_deveRetornarCaixaPorCodigo() {
        Optional<Caixa> caixaOptional = Optional.of(new Caixa());
        when(caixaRepository.findById(10L)).thenReturn(caixaOptional);
        Optional<Caixa> resultado = caixaService.busca(10L);
        assertTrue(resultado.isPresent());
    }

    @Test
    void buscaCaixaUsuario_deveRetornarCaixaDoUsuario() {
        Usuario usuarioTest = new Usuario();
        usuarioTest.setCodigo(1L);
        Caixa caixaTest = new Caixa();

        when(usuarioService.buscaUsuario("admin")).thenReturn(usuarioTest);
        when(caixaRepository.findByCaixaAbertoUsuario(1L)).thenReturn(caixaTest);

        Optional<Caixa> resultado = caixaService.buscaCaixaUsuario("admin");
        assertTrue(resultado.isPresent());
    }

    @Test
    void buscaCaixaUsuario_deveRetornarEmptyQuandoNaoEncontraCaixa() {
        Usuario usuarioTest = new Usuario();
        usuarioTest.setCodigo(1L);

        when(usuarioService.buscaUsuario("admin")).thenReturn(usuarioTest);
        when(caixaRepository.findByCaixaAbertoUsuario(1L)).thenReturn(null);

        Optional<Caixa> resultado = caixaService.buscaCaixaUsuario("admin");
        assertFalse(resultado.isPresent());
    }

    @Test
    void listaBancos_deveRetornarListaDeBancos() {
        when(caixaRepository.buscaBancos(CaixaTipo.BANCO)).thenReturn(Arrays.asList(new Caixa()));
        List<Caixa> resultado = caixaService.listaBancos();
        assertEquals(1, resultado.size());
    }

    @Test
    void listaCaixasAbertosTipo_deveRetornarCaixasDoTipo() {
        when(caixaRepository.buscaCaixaTipo(CaixaTipo.CAIXA)).thenReturn(Arrays.asList(new Caixa()));
        List<Caixa> resultado = caixaService.listaCaixasAbertosTipo(CaixaTipo.CAIXA);
        assertEquals(1, resultado.size());
    }

    @Test
    void listaBancosAbertosTipoFilterBanco_comData() {
        BancoFilter filter = new BancoFilter();
        filter.setData_cadastro("2024/05/20");

        Date data = Date.valueOf("2024-05-20");
        when(caixaRepository.buscaCaixaTipoData(CaixaTipo.BANCO, data)).thenReturn(Arrays.asList(new Caixa()));

        List<Caixa> resultado = caixaService.listaBancosAbertosTipoFilterBanco(CaixaTipo.BANCO, filter);
        assertEquals(1, resultado.size());
    }

    @Test
    void listaBancosAbertosTipoFilterBanco_semData() {
        BancoFilter filter = new BancoFilter(); 
        when(caixaRepository.buscaCaixaTipo(CaixaTipo.BANCO)).thenReturn(Arrays.asList(new Caixa()));

        List<Caixa> resultado = caixaService.listaBancosAbertosTipoFilterBanco(CaixaTipo.BANCO, filter);
        assertEquals(1, resultado.size());
    }

    @Test
    void listaBancosAbertosTipoFilterBanco_comDataVazia() {
        BancoFilter filter = new BancoFilter();
        filter.setData_cadastro("");
        when(caixaRepository.buscaCaixaTipo(CaixaTipo.BANCO)).thenReturn(Arrays.asList(new Caixa()));

        List<Caixa> resultado = caixaService.listaBancosAbertosTipoFilterBanco(CaixaTipo.BANCO, filter);
        assertEquals(1, resultado.size());
    }

    @Test
    void cadastro_deveLancarExcecaoQuandoCaixaJaAberto() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.CAIXA);
        caixaTest.setValor_abertura(100.0);
        caixaTest.setDescricao("Teste");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.of(new Caixa()));

        assertThrows(RuntimeException.class, () -> caixaService.cadastro(caixaTest));
    }

    @Test
    void cadastro_deveLancarExcecaoQuandoValorAberturaNegativo() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.CAIXA);
        caixaTest.setValor_abertura(-100.0);
        caixaTest.setDescricao("Teste");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> caixaService.cadastro(caixaTest));
    }

    @Test
    void cadastro_deveDefinirValorAberturaComoZeroQuandoNull() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.CAIXA);
        caixaTest.setValor_abertura(null);
        caixaTest.setDescricao("Teste");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());
        when(caixaRepository.save(any(Caixa.class))).thenReturn(caixaTest);
        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);

        caixaService.cadastro(caixaTest);
        assertEquals(Double.valueOf(0.0), caixaTest.getValor_abertura());
        assertEquals(Double.valueOf(0.0), caixaTest.getValor_total());
    }

    @Test
    void cadastro_deveDefinirDescricaoPadraoParaCaixa() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.CAIXA);
        caixaTest.setValor_abertura(100.0);
        caixaTest.setDescricao("");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());
        when(caixaRepository.save(any(Caixa.class))).thenReturn(caixaTest);
        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);

        caixaService.cadastro(caixaTest);
        assertEquals("Caixa diário", caixaTest.getDescricao());
    }

    @Test
    void cadastro_deveDefinirDescricaoPadraoParaCofre() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.COFRE);
        caixaTest.setValor_abertura(100.0);
        caixaTest.setDescricao("");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());
        when(caixaRepository.save(any(Caixa.class))).thenReturn(caixaTest);
        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);

        caixaService.cadastro(caixaTest);
        assertEquals("Cofre", caixaTest.getDescricao());
    }

    @Test
    void cadastro_deveDefinirDescricaoPadraoParaBanco() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.BANCO);
        caixaTest.setValor_abertura(100.0);
        caixaTest.setDescricao("");
        caixaTest.setAgencia("123");
        caixaTest.setConta("456");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());
        when(caixaRepository.save(any(Caixa.class))).thenReturn(caixaTest);
        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);

        caixaService.cadastro(caixaTest);
        assertEquals("Banco", caixaTest.getDescricao());
    }

    @Test
    void cadastro_deveLimparAgenciaEContaParaBanco() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.BANCO);
        caixaTest.setValor_abertura(100.0);
        caixaTest.setDescricao("Teste");
        caixaTest.setAgencia("123-4");
        caixaTest.setConta("12345-6");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());
        when(caixaRepository.save(any(Caixa.class))).thenReturn(caixaTest);
        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);

        caixaService.cadastro(caixaTest);
        assertEquals("1234", caixaTest.getAgencia());
        assertEquals("123456", caixaTest.getConta());
    }

    @Test
    void cadastro_deveLancarExcecaoQuandoErroAoSalvar() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.CAIXA);
        caixaTest.setValor_abertura(100.0);
        caixaTest.setDescricao("Teste");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());
        when(caixaRepository.save(any(Caixa.class))).thenThrow(new RuntimeException("Erro de banco"));
        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);

        assertThrows(RuntimeException.class, () -> caixaService.cadastro(caixaTest));
    }

    @Test
    void cadastro_deveCriarLancamentoQuandoValorAberturaMaiorQueZero() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.CAIXA);
        caixaTest.setValor_abertura(100.0);
        caixaTest.setDescricao("Teste");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());
        when(caixaRepository.save(any(Caixa.class))).thenReturn(caixaTest);
        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);

        caixaService.cadastro(caixaTest);
        verify(caixaLancamentoService, times(1)).lancamento(any(CaixaLancamento.class));
    }

    @Test
    void cadastro_deveLancarExcecaoQuandoErroAoCriarLancamento() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.CAIXA);
        caixaTest.setValor_abertura(100.0);
        caixaTest.setDescricao("Teste");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());
        when(caixaRepository.save(any(Caixa.class))).thenReturn(caixaTest);
        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);
        when(caixaLancamentoService.lancamento(any(CaixaLancamento.class))).thenThrow(new RuntimeException("Erro"));

        assertThrows(RuntimeException.class, () -> caixaService.cadastro(caixaTest));
    }

    @Test
    void cadastro_deveDefinirValorTotalComoZeroQuandoValorAberturaZero() {
        Caixa caixaTest = new Caixa();
        caixaTest.setTipo(CaixaTipo.CAIXA);
        caixaTest.setValor_abertura(0.0);
        caixaTest.setDescricao("Teste");

        when(caixaRepository.caixaAberto()).thenReturn(Optional.empty());
        when(caixaRepository.save(any(Caixa.class))).thenReturn(caixaTest);
        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);

        caixaService.cadastro(caixaTest);
        assertEquals(Double.valueOf(0.0), caixaTest.getValor_total());
        verify(caixaLancamentoService, never()).lancamento(any(CaixaLancamento.class));
    }

    @Test
    void fechaCaixa_deveRetornarMensagemQuandoSenhaVazia() {
        String resultado = caixaService.fechaCaixa(1L, "");
        assertEquals("Favor, informe a senha", resultado);
    }

    @Test
    void fechaCaixa_deveRetornarMensagemQuandoSenhaIncorreta() {
        Usuario usuarioTest = new Usuario();
        usuarioTest.setSenha("encodedPassword");

        when(usuarioService.buscaUsuario("admin")).thenReturn(usuarioTest);

        String resultado = caixaService.fechaCaixa(1L, "senhaIncorreta");
        assertEquals("Senha incorreta, favor verifique", resultado);
    }

    @Test
    void fechaCaixa_deveLancarExcecaoQuandoCaixaJaFechado() {
        Usuario usuarioTest = new Usuario();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("senhaCorreta");
        usuarioTest.setSenha(encodedPassword);

        Caixa caixaTest = new Caixa();
        caixaTest.setData_fechamento(new Timestamp(System.currentTimeMillis()));

        when(usuarioService.buscaUsuario("admin")).thenReturn(usuarioTest);
        when(caixaRepository.findById(1L)).thenReturn(Optional.of(caixaTest));

        assertThrows(RuntimeException.class, () -> caixaService.fechaCaixa(1L, "senhaCorreta"));
    }

    @Test
    void fechaCaixa_deveFecharCaixaComSucesso() {
        Usuario usuarioTest = new Usuario();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("senhaCorreta");
        usuarioTest.setSenha(encodedPassword);

        Caixa caixaTest = new Caixa();
        caixaTest.setValor_total(100.0);

        when(usuarioService.buscaUsuario("admin")).thenReturn(usuarioTest);
        when(caixaRepository.findById(1L)).thenReturn(Optional.of(caixaTest));
        when(caixaRepository.save(any(Caixa.class))).thenReturn(caixaTest);

        String resultado = caixaService.fechaCaixa(1L, "senhaCorreta");
        assertEquals("Caixa fechado com sucesso", resultado);
        assertNotNull(caixaTest.getData_fechamento());
        assertEquals(Double.valueOf(100.0), caixaTest.getValor_fechamento());
    }

    @Test
    void fechaCaixa_deveDefinirValorTotalComoZeroQuandoNull() {
        Usuario usuarioTest = new Usuario();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("senhaCorreta");
        usuarioTest.setSenha(encodedPassword);

        Caixa caixaTest = new Caixa();
        caixaTest.setValor_total(null);

        when(usuarioService.buscaUsuario("admin")).thenReturn(usuarioTest);
        when(caixaRepository.findById(1L)).thenReturn(Optional.of(caixaTest));
        when(caixaRepository.save(any(Caixa.class))).thenReturn(caixaTest);

        String resultado = caixaService.fechaCaixa(1L, "senhaCorreta");
        assertEquals("Caixa fechado com sucesso", resultado);
        assertEquals(Double.valueOf(0.0), caixaTest.getValor_fechamento());
    }

    @Test
    void fechaCaixa_deveLancarExcecaoQuandoErroAoSalvar() {
        Usuario usuarioTest = new Usuario();
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode("senhaCorreta");
        usuarioTest.setSenha(encodedPassword);

        Caixa caixaTest = new Caixa();
        caixaTest.setValor_total(100.0);

        when(usuarioService.buscaUsuario("admin")).thenReturn(usuarioTest);
        when(caixaRepository.findById(1L)).thenReturn(Optional.of(caixaTest));
        when(caixaRepository.save(any(Caixa.class))).thenThrow(new RuntimeException("Erro de banco"));

        assertThrows(RuntimeException.class, () -> caixaService.fechaCaixa(1L, "senhaCorreta"));
    }
}