package net.originmobi.pdv.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.originmobi.pdv.enumerado.TituloTipo;
import net.originmobi.pdv.exception.*;
import net.originmobi.pdv.model.*;
import net.originmobi.pdv.model.cartao.MaquinaCartao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import net.originmobi.pdv.repository.RecebimentoRepository;
import net.originmobi.pdv.service.cartao.CartaoLancamentoService;
import net.originmobi.pdv.controller.TituloService;
import net.originmobi.pdv.singleton.Aplicacao;

@SuppressWarnings("deprecation")
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
    private net.originmobi.pdv.model.TituloTipo tituloTipo;
    private MaquinaCartao maquinaCartao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("Teste João Pedro", "Senha")
        );
        pessoa = new Pessoa();
        pessoa.setCodigo(1L);
        pessoa.setNome("Teste Cliente");

        Receber receber = new Receber();
        receber.setPessoa(pessoa);

        parcela = new Parcela();
        parcela.setCodigo(1L);
        parcela.setValor_restante(100.0);
        parcela.setQuitado(0);
        parcela.setReceber(receber);

        recebimento = new Recebimento();
        recebimento.setCodigo(1L);
        recebimento.setValor_total(100.0);
        recebimento.setPessoa(pessoa);

        tituloTipo = new net.originmobi.pdv.model.TituloTipo();
        tituloTipo.setCodigo(1L);
        tituloTipo.setDescricao("Dinheiro");
        tituloTipo.setSigla("DIN");

        titulo = new Titulo();
        titulo.setCodigo(1L);
        titulo.setDescricao("");
        titulo.setMaquina(maquinaCartao);
        titulo.setTipo(tituloTipo);

        caixa = new Caixa();
        caixa.setCodigo(1L);

        Usuario usuario = new Usuario();
        usuario.setCodigo(1L);
    }

    // ========== TESTES PARA abrirRecebimento ==========

    @Test
    void abrirRecebimento_Success() {
        // Configurar array de parcelas
        String[] arrayParcelas = {"1"};

        // Configurar pessoa
        assertNotNull(pessoa, "Pessoa não deve ser nula");
        pessoa.setCodigo(1L); // Garantir que o código está configurado

        // Configurar objeto Receber
        Receber receber = new Receber();
        receber.setPessoa(pessoa);

        // Configurar parcela
        Parcela parcela = new Parcela();
        parcela.setCodigo(1L);
        parcela.setValor_restante(100.0);
        parcela.setQuitado(0);
        parcela.setReceber(receber);

        // Configurar mocks
        when(parcelas.busca(1L)).thenReturn(parcela);
        when(pessoas.buscaPessoa(1L)).thenReturn(Optional.of(pessoa));

        // Configurar recebimento que será salvo
        Recebimento recebimentoSalvo = new Recebimento();
        recebimentoSalvo.setCodigo(1L);
        when(recebimentos.save(any(Recebimento.class))).thenReturn(recebimentoSalvo);

        // Executar o método
        String result = recebimentoService.abrirRecebimento(1L, arrayParcelas);

        // Verificações
        assertEquals("1", result);
        
        // Verificar se o save foi chamado com os parâmetros corretos
        verify(recebimentos).save(argThat(rec -> {
            return rec.getValor_total().equals(100.0) && 
                   rec.getPessoa().equals(pessoa) &&
                   rec.getParcela().size() == 1 &&
                   rec.getData_processamento() == null;
        }));
        
        verify(parcelas).busca(1L);
        verify(pessoas).buscaPessoa(1L);
    }

    @Test
    void abrirRecebimento_QuitadaParcela_ThrowsException() {
        String[] arrayParcelas = {"1"};
        parcela.setQuitado(1);
        when(parcelas.busca(1L)).thenReturn(parcela);

        assertThrows(RecebimentoBusinessException.class, () ->
                recebimentoService.abrirRecebimento(1L, arrayParcelas)
        );
    }

    @Test
    void abrirRecebimento_ParcelaNaoPertencenteAoCliente_ThrowsException() {
        String[] arrayParcelas = {"1"};
        
        // Criar pessoa diferente para a parcela
        Pessoa outraPessoa = new Pessoa();
        outraPessoa.setCodigo(2L);
        
        Receber outroReceber = new Receber();
        outroReceber.setPessoa(outraPessoa);
        
        parcela.setReceber(outroReceber);
        
        when(parcelas.busca(1L)).thenReturn(parcela);

        assertThrows(RecebimentoBusinessException.class, () ->
                recebimentoService.abrirRecebimento(1L, arrayParcelas)
        );
    }

    @Test
    void abrirRecebimento_PessoaNotFound_ThrowsException() {
        String[] arrayParcelas = {"1"};
        when(parcelas.busca(1L)).thenReturn(parcela);
        when(pessoas.buscaPessoa(1L)).thenReturn(Optional.empty());

        assertThrows(RecebimentoNotFoundException.class, () ->
                recebimentoService.abrirRecebimento(1L, arrayParcelas)
        );
    }

    @Test
    void abrirRecebimento_ErroAoSalvar_ThrowsException() {
        String[] arrayParcelas = {"1"};
        
        when(parcelas.busca(1L)).thenReturn(parcela);
        when(pessoas.buscaPessoa(1L)).thenReturn(Optional.of(pessoa));
        when(recebimentos.save(any(Recebimento.class))).thenThrow(new RuntimeException("Erro na base"));

        assertThrows(RecebimentoProcessingException.class, () ->
                recebimentoService.abrirRecebimento(1L, arrayParcelas)
        );
    }

    // ========== TESTES PARA receber ==========

    @Test
    void receber_Success_CartaoDebito() {
        // Configurar título como cartão de débito
        tituloTipo.setSigla(TituloTipo.CARTDEB.toString());
        
        List<Parcela> parcelasList = new ArrayList<>();
        parcelasList.add(parcela);

        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
        when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);

        String result = recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L);

        assertEquals("Recebimento realizado com sucesso", result);
        verify(cartaoLancamentos).lancamento(100.0, Optional.of(titulo));
        verify(lancamentos, never()).lancamento(any(CaixaLancamento.class));
    }

    @Test
    void receber_Success_CartaoCredito() {
        // Configurar título como cartão de crédito
        tituloTipo.setSigla(TituloTipo.CARTCRED.toString());
        
        List<Parcela> parcelasList = new ArrayList<>();
        parcelasList.add(parcela);

        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
        when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);

        String result = recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L);

        assertEquals("Recebimento realizado com sucesso", result);
        verify(cartaoLancamentos).lancamento(100.0, Optional.of(titulo));
        verify(lancamentos, never()).lancamento(any(CaixaLancamento.class));
    }

    @Test
    void receber_TituloNulo_ThrowsException() {
        assertThrows(RecebimentoValidationException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, null)
        );
    }

    @Test
    void receber_TituloZero_ThrowsException() {
        assertThrows(RecebimentoValidationException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, 0L)
        );
    }

    @Test
    void receber_ValorInvalido_ThrowsException() {
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));

        assertThrows(RecebimentoValidationException.class, () ->
                recebimentoService.receber(1L, 0.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_ValorNegativo_ThrowsException() {
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));

        assertThrows(RecebimentoValidationException.class, () ->
                recebimentoService.receber(1L, -50.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_RecebimentoNaoEncontrado_ThrowsException() {
        when(recebimentos.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecebimentoNotFoundException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_TituloNaoEncontrado_ThrowsException() {
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.empty());

        assertThrows(RecebimentoNotFoundException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_Processado_ThrowsException() {
        recebimento.setData_processamento(new Timestamp(System.currentTimeMillis()));
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));

        assertThrows(RecebimentoOperationException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_ValorSuperiorAoTitulo_ThrowsException() {
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));

        assertThrows(RecebimentoBusinessException.class, () ->
                recebimentoService.receber(1L, 150.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_SemParcelas_ThrowsException() {
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
        when(receParcelas.parcelasDoReceber(1L)).thenReturn(new ArrayList<>());

        assertThrows(RecebimentoBusinessException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_SemCaixaAberto_ThrowsException() {
        List<Parcela> parcelasList = new ArrayList<>();
        parcelasList.add(parcela);

        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
        when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
        when(caixas.caixaAberto()).thenReturn(Optional.empty());

        assertThrows(RecebimentoBusinessException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_ErroAoProcessarParcela_ThrowsException() {
        List<Parcela> parcelasList = new ArrayList<>();
        parcelasList.add(parcela);

        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
        when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
        when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
        when(usuarios.buscaUsuario("Teste João Pedro")).thenReturn(usuario);
        doThrow(new RuntimeException("Erro ao processar")).when(parcelas).receber(anyLong(), anyDouble(), anyDouble(), anyDouble());

        assertThrows(RecebimentoProcessingException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_ErroAoLancarCartao_ThrowsException() {
        tituloTipo.setSigla(TituloTipo.CARTDEB.toString());
        
        List<Parcela> parcelasList = new ArrayList<>();
        parcelasList.add(parcela);

        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
        when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
        doThrow(new RuntimeException("Erro no cartão")).when(cartaoLancamentos).lancamento(anyDouble(), any());

        assertThrows(RecebimentoProcessingException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_ErroAoLancarCaixa_ThrowsException() {
        List<Parcela> parcelasList = new ArrayList<>();
        parcelasList.add(parcela);

        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
        when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
        when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
        when(usuarios.buscaUsuario("Teste João Pedro")).thenReturn(usuario);
        doThrow(new RuntimeException("Erro no caixa")).when(lancamentos).lancamento(any());

        assertThrows(RecebimentoProcessingException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L)
        );
    }

    @Test
    void receber_ErroAoFinalizar_ThrowsException() {
        List<Parcela> parcelasList = new ArrayList<>();
        parcelasList.add(parcela);

        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
        when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
        when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
        when(usuarios.buscaUsuario("Teste João Pedro")).thenReturn(usuario);
        when(recebimentos.save(any(Recebimento.class))).thenThrow(new RuntimeException("Erro ao salvar"));

        assertThrows(RecebimentoProcessingException.class, () ->
                recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L)
        );
    }

    // ========== TESTES PARA remover ==========

    @Test
    void remover_Success() {
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));

        String result = recebimentoService.remover(1L);

        assertEquals("Recebimento removido com sucesso", result);
        verify(recebimentos).deleteById(1L);
    }

    @Test
    void remover_RecebimentoNaoEncontrado_ThrowsException() {
        when(recebimentos.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecebimentoNotFoundException.class, () ->
                recebimentoService.remover(1L)
        );
    }

    @Test
    void remover_Processado_ThrowsException() {
        recebimento.setData_processamento(new Timestamp(System.currentTimeMillis()));
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));

        assertThrows(RecebimentoOperationException.class, () ->
                recebimentoService.remover(1L)
        );
    }

    @Test
    void remover_ErroAoRemover_ThrowsException() {
        when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
        doThrow(new RuntimeException("Erro na base")).when(recebimentos).deleteById(1L);

        assertThrows(RecebimentoProcessingException.class, () ->
                recebimentoService.remover(1L)
        );
    }
    
 // Adicione estes testes à sua classe RecebimentoServiceTest

 // ========== TESTES ADICIONAIS PARA abrirRecebimento ==========

 @Test
 void abrirRecebimento_MultiplasParcelasSuccess() {
     // Teste com múltiplas parcelas
     String[] arrayParcelas = {"1", "2", "3"};
     
     // Configurar parcelas
     Parcela parcela1 = criarParcela(1L, 100.0, 0);
     Parcela parcela2 = criarParcela(2L, 200.0, 0);
     Parcela parcela3 = criarParcela(3L, 150.0, 0);
     
     when(parcelas.busca(1L)).thenReturn(parcela1);
     when(parcelas.busca(2L)).thenReturn(parcela2);
     when(parcelas.busca(3L)).thenReturn(parcela3);
     when(pessoas.buscaPessoa(1L)).thenReturn(Optional.of(pessoa));
     
     Recebimento recebimentoSalvo = new Recebimento();
     recebimentoSalvo.setCodigo(1L);
     when(recebimentos.save(any(Recebimento.class))).thenReturn(recebimentoSalvo);
     
     String result = recebimentoService.abrirRecebimento(1L, arrayParcelas);
     
     assertEquals("1", result);
     verify(recebimentos).save(argThat(rec -> 
         rec.getValor_total().equals(450.0) && rec.getParcela().size() == 3
     ));
 }

 @Test
 void abrirRecebimento_ParcelaComValorZero() {
     String[] arrayParcelas = {"1"};
     
     Parcela parcelaZero = criarParcela(1L, 0.0, 0);
     
     when(parcelas.busca(1L)).thenReturn(parcelaZero);
     when(pessoas.buscaPessoa(1L)).thenReturn(Optional.of(pessoa));
     
     Recebimento recebimentoSalvo = new Recebimento();
     recebimentoSalvo.setCodigo(1L);
     when(recebimentos.save(any(Recebimento.class))).thenReturn(recebimentoSalvo);
     
     String result = recebimentoService.abrirRecebimento(1L, arrayParcelas);
     
     assertEquals("1", result);
     verify(recebimentos).save(argThat(rec -> rec.getValor_total().equals(0.0)));
 }

 @Test
 void abrirRecebimento_ArrayVazio_ThrowsException() {
     String[] arrayParcelas = {};
     
     // Deve lançar exceção por não ter parcelas
     assertThrows(Exception.class, () ->
         recebimentoService.abrirRecebimento(1L, arrayParcelas)
     );
 }

 @Test
 void abrirRecebimento_ParcelaInexistente_ThrowsException() {
     String[] arrayParcelas = {"999"};
     
     when(parcelas.busca(999L)).thenThrow(new RecebimentoProcessingException("Parcela não encontrada"));
     
     assertThrows(RecebimentoProcessingException.class, () ->
         recebimentoService.abrirRecebimento(1L, arrayParcelas)
     );
 }

 // ========== TESTES ADICIONAIS PARA receber ==========

 @Test
 void receber_Success_Dinheiro() {
     // Teste específico para pagamento em dinheiro
     tituloTipo.setSigla("DIN");
     
     List<Parcela> parcelasList = new ArrayList<>();
     parcelasList.add(parcela);
     
     when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
     when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
     when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
     when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
     when(usuarios.buscaUsuario("Teste João Pedro")).thenReturn(usuario);
     
     String result = recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L);
     
     assertEquals("Recebimento realizado com sucesso", result);
     verify(lancamentos).lancamento(any(CaixaLancamento.class));
     verify(cartaoLancamentos, never()).lancamento(anyDouble(), any());
 }

 @Test
 void receber_ValorMenorQueParcela_Success() {
     // Teste quando valor recebido é menor que o total das parcelas
     List<Parcela> parcelasList = new ArrayList<>();
     parcelasList.add(parcela);

     when(usuarios.buscaUsuario("Teste João Pedro")).thenReturn(usuario);
     when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
     when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
     when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
     when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
     
     String result = recebimentoService.receber(1L, 50.0, 0.0, 0.0, 1L);
     
     assertEquals("Recebimento realizado com sucesso", result);
     verify(parcelas).receber(1L, 50.0, 0.0, 0.0);
 }

 @Test
 void receber_FormatacaoValorComVirgula() {
     // Teste específico para formatação de valor
     recebimento.setValor_total(100.555); // Valor com mais casas decimais
     
     List<Parcela> parcelasList = new ArrayList<>();
     parcelasList.add(parcela);
     
     when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
     when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
     when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
     when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
     when(usuarios.buscaUsuario("Teste João Pedro")).thenReturn(usuario);
     
     String result = recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L);
     
     assertEquals("Recebimento realizado com sucesso", result);
 }

 @Test
 void receber_ValorExatamenteIgualAoTitulo() {
     // Teste limite: valor exatamente igual ao título
     List<Parcela> parcelasList = new ArrayList<>();
     parcelasList.add(parcela);

     when(usuarios.buscaUsuario("Teste João Pedro")).thenReturn(usuario);
     when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
     when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
     when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
     when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
     
     String result = recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L);
     
     assertEquals("Recebimento realizado com sucesso", result);
     verify(recebimentos).save(argThat(rec -> 
         rec.getValor_recebido().equals(100.0) && rec.getData_processamento() != null
     ));
 }

 @Test
 void receber_ProcessarParcelaComValorRestanteZero() {
     // Teste edge case: parcela com valor restante zero
     Parcela parcelaZero = criarParcela(1L, 0.0, 0);
     
     List<Parcela> parcelasList = new ArrayList<>();
     parcelasList.add(parcelaZero);
     
     when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
     when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
     when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
     when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
     when(usuarios.buscaUsuario("Teste João Pedro")).thenReturn(usuario);
     
     String result = recebimentoService.receber(1L, 50.0, 0.0, 0.0, 1L);
     
     assertEquals("Recebimento realizado com sucesso", result);
     verify(parcelas).receber(1L, 0.0, 0.0, 0.0);
 }

 @Test
 void receber_OutroTipoTitulo_NaoCartao() {
     // Teste para outros tipos de título que não sejam cartão
     tituloTipo.setSigla("PIX");
     
     List<Parcela> parcelasList = new ArrayList<>();
     parcelasList.add(parcela);
     
     when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
     when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
     when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
     when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
     when(usuarios.buscaUsuario("Teste João Pedro")).thenReturn(usuario);
     
     String result = recebimentoService.receber(1L, 100.0, 0.0, 0.0, 1L);
     
     assertEquals("Recebimento realizado com sucesso", result);
     verify(lancamentos).lancamento(any(CaixaLancamento.class));
     verify(cartaoLancamentos, never()).lancamento(anyDouble(), any());
 }

 // ========== TESTES ADICIONAIS PARA remover ==========

 @Test
 void remover_DataProcessamentoNula_Success() {
     // Teste explícito para data_processamento nula
     recebimento.setData_processamento(null);
     when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
     
     String result = recebimentoService.remover(1L);
     
     assertEquals("Recebimento removido com sucesso", result);
     verify(recebimentos).deleteById(1L);
 }

 @Test
 void remover_VerificarChamadaExataDoRepository() {
     // Teste para verificar chamada exata do repository
     when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
     
     recebimentoService.remover(1L);
     
     verify(recebimentos, times(1)).findById(1L);
     verify(recebimentos, times(1)).deleteById(1L);
     verifyNoMoreInteractions(recebimentos);
 }

 // ========== MÉTODOS AUXILIARES ==========

 private Parcela criarParcela(Long codigo, Double valorRestante, int quitado) {
     Parcela p = new Parcela();
     p.setCodigo(codigo);
     p.setValor_restante(valorRestante);
     p.setQuitado(quitado);
     
     Receber receber = new Receber();
     receber.setPessoa(pessoa);
     p.setReceber(receber);
     
     return p;
 }

 // ========== TESTES DE VALIDAÇÃO ESPECÍFICOS ==========

 @Test
 void validarParametrosRecebimento_TituloNulo() {
     // Teste isolado para validação de parâmetros
     assertThrows(RecebimentoValidationException.class, () ->
         recebimentoService.receber(1L, 100.0, 0.0, 0.0, null)
     );
 }

 @Test
 void validarParametrosRecebimento_ValorMuitoPequeno() {
     // Teste com valor muito pequeno mas positivo
     when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
     when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
     
     assertThrows(RecebimentoBusinessException.class, () ->
         recebimentoService.receber(1L, 0.001, 0.0, 0.0, 1L)
     );
 }

 @Test
 void processarParcela_ValorSobraZero() {
     // Teste específico para valor sobra zero
     
    List<Parcela> parcelasList = new ArrayList<>();
    parcelasList.add(parcela);

    when(usuarios.buscaUsuario("Teste JP")).thenReturn(usuario);
    when(recebimentos.findById(1L)).thenReturn(Optional.of(recebimento));
    when(titulos.busca(1L)).thenReturn(Optional.of(titulo));
    when(receParcelas.parcelasDoReceber(1L)).thenReturn(parcelasList);
    when(caixas.caixaAberto()).thenReturn(Optional.of(caixa));
    
    // Valor exatamente igual ao valor da parcela
    String result = recebimentoService.receber(1L, parcela.getValor_restante(), 0.0, 0.0, 1L);
    
    assertEquals("Recebimento realizado com sucesso", result);
    verify(parcelas).receber(1L, parcela.getValor_restante(), 0.0, 0.0);
 }
}