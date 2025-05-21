package net.originmobi.pdv.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.Parcela;
import net.originmobi.pdv.model.Pessoa;
import net.originmobi.pdv.model.Recebimento;
import net.originmobi.pdv.model.Titulo;
import net.originmobi.pdv.model.TituloTipo;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.RecebimentoRepository;
import net.originmobi.pdv.service.cartao.CartaoLancamentoService;
import net.originmobi.pdv.controller.TituloService;

@ExtendWith(MockitoExtension.class)
class RecebimentoServiceTest {

    @Mock
    private RecebimentoRepository recebimentos;

    @Mock
    private PessoaService pessoas;

    @Mock
    private RecebimentoParcelaService receParcelas;

    @Mock
    private ParcelaService parcelas;

    @Mock
    private CaixaService caixas;

    @Mock
    private UsuarioService usuarios;

    @Mock
    private CaixaLancamentoService lancamentos;

    @Mock
    private TituloService titulos;

    @Mock
    private CartaoLancamentoService cartaoLancamentos;

    @InjectMocks
    private RecebimentoService recebimentoService;

    private Pessoa pessoa;
    private Parcela parcela;
    private Recebimento recebimento;
    private Titulo titulo;
    private Caixa caixa;
    private Usuario usuario;
    private TituloTipo tituloTipo;

    @BeforeEach
    void setUp() {
        pessoa = new Pessoa();
        pessoa.setCodigo(1L);
        pessoa.setNome("Teste Cliente");

        parcela = new Parcela();
        parcela.setCodigo(1L);
        parcela.setValor_restante(100.0);
        parcela.setQuitado(0);

        recebimento = new Recebimento();
        recebimento.setCodigo(1L);
        recebimento.setValor_total(100.0);
        recebimento.setPessoa(pessoa);

        tituloTipo = new TituloTipo();
        tituloTipo.setCodigo(1L);
        tituloTipo.setDescricao("Dinheiro");
        tituloTipo.setSigla("DIN");

        titulo = new Titulo();
        titulo.setCodigo(1L);
        titulo.setTipo(tituloTipo);

        caixa = new Caixa();
        caixa.setCodigo(1L);

        usuario = new Usuario();
        usuario.setCodigo(1L);
    }

    @Test
    void abrirRecebimento_Success() {
        String[] arrayParcelas = {"1"};
        when(parcelas.busca(1L)).thenReturn(parcela);
        when(pessoas.buscaPessoa(1L)).thenReturn(Optional.of(pessoa));
        when(recebimentos.save(any(Recebimento.class))).thenReturn(recebimento);

        String result = recebimentoService.abrirRecebimento(1L, arrayParcelas);

        assertEquals("1", result);
        verify(recebimentos).save(any(Recebimento.class));
    }

    @Test
    void abrirRecebimento_QuitadaParcela_ThrowsException() {
        String[] arrayParcelas = {"1"};
        parcela.setQuitado(1);
        when(parcelas.busca(1L)).thenReturn(parcela);

        assertThrows(RuntimeException.class, () -> 
            recebimentoService.abrirRecebimento(1L, arrayParcelas)
        );
    }

    @Test
    void abrirRecebimento_PessoaNotFound_ThrowsException() {
        String[] arrayParcelas = {"1"};
        when(parcelas.busca(1L)).thenReturn(parcela);
        when(pessoas.buscaPessoa(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> 
            recebimentoService.abrirRecebimento(1L, arrayParcelas)
        );
    }

    @Test
    void receber_Success() {
        List<Parcela> parcelasList = new ArrayList<>();
        parcelasList.add(parcela);

        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
        when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
        when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
        when(usuarios.buscaUsuario(any())).thenReturn(usuario);

        String result = recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L);

        assertEquals("Recebimento realizado com sucesso", result);
        verify(recebimentos).save(any(Recebimento.class));
    }

    @Test
    void receber_Processado_ThrowsException() {
        recebimento.setData_processamento(new Timestamp(System.currentTimeMillis()));
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));

        assertThrows(RuntimeException.class, () -> 
            recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_ValorInvalido_ThrowsException() {
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));

        assertThrows(RuntimeException.class, () -> 
            recebimentoService.receber(1L, 0.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void remover_Success() {
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));

        String result = recebimentoService.remover(1L);

        assertEquals("removido com sucesso", result);
        verify(recebimentos).deleteById(1L);
    }

    @Test
    void remover_Processado_ThrowsException() {
        recebimento.setData_processamento(new Timestamp(System.currentTimeMillis()));
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));

        assertThrows(RuntimeException.class, () -> 
            recebimentoService.remover(1L)
        );
    }
} 