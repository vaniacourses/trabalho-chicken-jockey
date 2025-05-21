package net.originmobi.pdv.service;

import net.originmobi.pdv.enumerado.caixa.CaixaTipo;
import net.originmobi.pdv.filter.BancoFilter;
import net.originmobi.pdv.filter.CaixaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.CaixaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CaixaServiceTest {

    @InjectMocks
    private CaixaService caixaService;

    @Mock
    private CaixaRepository caixaRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CaixaLancamentoService caixaLancamentoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
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
        CaixaFilter filtro = new CaixaFilter(); // data_cadastro null
        when(caixaRepository.listaCaixasAbertos()).thenReturn(Arrays.asList(new Caixa()));

        List<Caixa> resultado = caixaService.listarCaixas(filtro);
        assertEquals(1, resultado.size());
        verify(caixaRepository, times(1)).listaCaixasAbertos();
    }

    @Test
    void caixaAberto_deveRetornarOptional() {
        Optional<Caixa> caixa = Optional.of(new Caixa());
        when(caixaRepository.caixaAberto()).thenReturn(caixa);
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
        Optional<Caixa> caixa = Optional.of(new Caixa());
        when(caixaRepository.findById(10L)).thenReturn(caixa);
        Optional<Caixa> resultado = caixaService.busca(10L);
        assertTrue(resultado.isPresent());
    }

    @Test
    void buscaCaixaUsuario_deveRetornarCaixaDoUsuario() {
        Usuario usuario = new Usuario();
        usuario.setCodigo(1L);
        Caixa caixa = new Caixa();

        when(usuarioService.buscaUsuario("admin")).thenReturn(usuario);
        when(caixaRepository.findByCaixaAbertoUsuario(1L)).thenReturn(caixa);

        Optional<Caixa> resultado = caixaService.buscaCaixaUsuario("admin");
        assertTrue(resultado.isPresent());
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
        BancoFilter filter = new BancoFilter(); // data_cadastro null
        when(caixaRepository.buscaCaixaTipo(CaixaTipo.BANCO)).thenReturn(Arrays.asList(new Caixa()));

        List<Caixa> resultado = caixaService.listaBancosAbertosTipoFilterBanco(CaixaTipo.BANCO, filter);
        assertEquals(1, resultado.size());
    }
}