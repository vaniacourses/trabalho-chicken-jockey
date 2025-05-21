import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;

import net.originmobi.pdv.enumerado.caixa.CaixaTipo;
import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.filter.BancoFilter;
import net.originmobi.pdv.filter.CaixaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.CaixaRepository;
import net.originmobi.pdv.service.*;
import net.originmobi.pdv.singleton.Aplicacao;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CaixaServiceTest {

    @InjectMocks
    private CaixaService service;

    @Mock
    private CaixaRepository caixas;

    @Mock
    private UsuarioService usuarios;

    @Mock
    private CaixaLancamentoService lancamentos;

    @BeforeEach
    void setup() {
        Aplicacao.getInstancia().setUsuarioAtual("user");
    }

    @Test
    void cadastroCaixaValido() {
        Caixa caixa = new Caixa();
        caixa.setTipo(CaixaTipo.CAIXA);
        caixa.setValor_abertura(100.0);

        when(caixas.caixaAberto()).thenReturn(Optional.empty());
        Usuario usuario = new Usuario();
        usuario.setCodigo(1L);
        when(usuarios.buscaUsuario(anyString())).thenReturn(usuario);

        Long id = 1L;
        caixa.setCodigo(id);

        assertDoesNotThrow(() -> service.cadastro(caixa));
        verify(caixas).save(any(Caixa.class));
    }

    @Test
    void cadastroCaixaValorNegativo() {
        Caixa caixa = new Caixa();
        caixa.setTipo(CaixaTipo.CAIXA);
        caixa.setValor_abertura(-10.0);

        assertThrows(RuntimeException.class, () -> service.cadastro(caixa));
    }

    @Test
    void cadastroComCaixaAberto() {
        Caixa caixa = new Caixa();
        caixa.setTipo(CaixaTipo.CAIXA);

        when(caixas.caixaAberto()).thenReturn(Optional.of(new Caixa()));

        assertThrows(RuntimeException.class, () -> service.cadastro(caixa));
    }

    @Test
    void fecharCaixaSenhaCorreta() {
        Long id = 1L;
        String senha = "1234";
        Usuario usuario = new Usuario();
        usuario.setSenha(new BCryptPasswordEncoder().encode(senha));

        Caixa caixa = new Caixa();
        caixa.setValor_total(200.0);
        when(usuarios.buscaUsuario(anyString())).thenReturn(usuario);
        when(caixas.findById(id)).thenReturn(Optional.of(caixa));

        String result = service.fechaCaixa(id, senha);
        assertEquals("Caixa fechado com sucesso", result);
    }

    @Test
    void fecharCaixaSenhaIncorreta() {
        Long id = 1L;
        Usuario usuario = new Usuario();
        usuario.setSenha(new BCryptPasswordEncoder().encode("senhaCorreta"));

        when(usuarios.buscaUsuario(anyString())).thenReturn(usuario);

        String result = service.fechaCaixa(id, "errada");
        assertEquals("Senha incorreta, favor verifique", result);
    }

    @Test
    void caixaIsAbertoTrue() {
        when(caixas.caixaAberto()).thenReturn(Optional.of(new Caixa()));
        assertTrue(service.caixaIsAberto());
    }

    @Test
    void caixaIsAbertoFalse() {
        when(caixas.caixaAberto()).thenReturn(Optional.empty());
        assertFalse(service.caixaIsAberto());
    }

    @Test
    void listarTodos() {
        List<Caixa> lista = Arrays.asList(new Caixa(), new Caixa());
        when(caixas.findByCodigoOrdenado()).thenReturn(lista);
        assertEquals(2, service.listaTodos().size());
    }

    @Test
    void listarCaixasPorData() {
        CaixaFilter filter = new CaixaFilter();
        filter.setData_cadastro("2025-05-20");

        when(caixas.buscaCaixasPorDataAbertura(any())).thenReturn(List.of(new Caixa()));
        List<Caixa> result = service.listarCaixas(filter);
        assertEquals(1, result.size());
    }

    @Test
    void listarCaixasSemData() {
        CaixaFilter filter = new CaixaFilter();
        List<Caixa> caixasList = List.of(new Caixa(), new Caixa());
        when(caixas.listaCaixasAbertos()).thenReturn(caixasList);
        assertEquals(2, service.listarCaixas(filter).size());
    }

    @Test
    void buscaCaixaPorId() {
        Caixa caixa = new Caixa();
        when(caixas.findById(1L)).thenReturn(Optional.of(caixa));
        assertTrue(service.busca(1L).isPresent());
    }

    @Test
    void buscaCaixaUsuario() {
        Usuario usuario = new Usuario();
        usuario.setCodigo(10L);
        Caixa caixa = new Caixa();

        when(usuarios.buscaUsuario("user")).thenReturn(usuario);
        when(caixas.findByCaixaAbertoUsuario(10L)).thenReturn(caixa);

        Optional<Caixa> result = service.buscaCaixaUsuario("user");
        assertTrue(result.isPresent());
    }

    @Test
    void listaBancos() {
        when(caixas.buscaBancos(CaixaTipo.BANCO)).thenReturn(List.of(new Caixa()));
        assertEquals(1, service.listaBancos().size());
    }

    @Test
    void listaCaixasAbertosTipo() {
        when(caixas.buscaCaixaTipo(CaixaTipo.CAIXA)).thenReturn(List.of(new Caixa()));
        assertEquals(1, service.listaCaixasAbertosTipo(CaixaTipo.CAIXA).size());
    }

    @Test
    void listaBancosAbertosTipoFilterBancoComData() {
        BancoFilter filter = new BancoFilter();
        filter.setData_cadastro("2025-05-20");

        when(caixas.buscaCaixaTipoData(eq(CaixaTipo.BANCO), any())).thenReturn(List.of(new Caixa()));
        List<Caixa> result = service.listaBancosAbertosTipoFilterBanco(CaixaTipo.BANCO, filter);
        assertEquals(1, result.size());
    }

    @Test
    void listaBancosAbertosTipoFilterBancoSemData() {
        BancoFilter filter = new BancoFilter();

        when(caixas.buscaCaixaTipo(CaixaTipo.BANCO)).thenReturn(List.of(new Caixa()));
        List<Caixa> result = service.listaBancosAbertosTipoFilterBanco(CaixaTipo.BANCO, filter);
        assertEquals(1, result.size());
    }
}