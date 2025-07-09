package net.originmobi.pdv.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.ibm.icu.text.DecimalFormat;

import lombok.AllArgsConstructor;
import net.originmobi.pdv.controller.TituloService;
import net.originmobi.pdv.enumerado.TituloTipo;
import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.exception.RecebimentoBusinessException;
import net.originmobi.pdv.exception.RecebimentoNotFoundException;
import net.originmobi.pdv.exception.RecebimentoOperationException;
import net.originmobi.pdv.exception.RecebimentoProcessingException;
import net.originmobi.pdv.exception.RecebimentoValidationException;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.Parcela;
import net.originmobi.pdv.model.Pessoa;
import net.originmobi.pdv.model.Recebimento;
import net.originmobi.pdv.model.Titulo;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.repository.RecebimentoRepository;
import net.originmobi.pdv.service.cartao.CartaoLancamentoService;
import net.originmobi.pdv.singleton.Aplicacao;
import net.originmobi.pdv.utilitarios.DataAtual;

@Service
@AllArgsConstructor
public class RecebimentoService {

    private RecebimentoRepository recebimentos;
    private PessoaService pessoas;
    private RecebimentoParcelaService receParcelas;
    private ParcelaService parcelas;
    private CaixaService caixas;
    private UsuarioService usuarios;
    private CaixaLancamentoService lancamentos;
    private TituloService titulos;
    private CartaoLancamentoService cartaoLancamentos;

    public String abrirRecebimento(Long codpes, String[] arrayParcelas) {
        List<Parcela> lista = new ArrayList<>();
        DataAtual dataAtual = new DataAtual();
        Double vlTotal = 0.0;

        for (int i = 0; i < arrayParcelas.length; i++) {
            Parcela parcela = parcelas.busca(Long.decode(arrayParcelas[i]));

            if (parcela.getQuitado() == 1) {
                throw new RecebimentoBusinessException("Parcela " + parcela.getCodigo() + " já está quitada, verifique.");
            }

            if (!parcela.getReceber().getPessoa().getCodigo().equals(codpes)) {
                throw new RecebimentoBusinessException("A parcela " + parcela.getCodigo() + " não pertence ao cliente selecionado");
            }

            try {
                lista.add(parcela);
                vlTotal = vlTotal + parcela.getValor_restante();
            } catch (Exception e) {
                throw new RecebimentoProcessingException("Erro ao processar parcela", e);
            }
        }

        Optional<Pessoa> pessoa = pessoas.buscaPessoa(codpes);

        if (!pessoa.isPresent()) {
            throw new RecebimentoNotFoundException("Cliente não encontrado");
        }

        Recebimento recebimento = new Recebimento(vlTotal, dataAtual.dataAtualTimeStamp(), pessoa.get(), lista);
        recebimento.setCodigo(codpes);

        try {
            recebimentos.save(recebimento);
        } catch (Exception e) {
            throw new RecebimentoProcessingException("Erro ao salvar recebimento", e);
        }

        return recebimento.getCodigo().toString();
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public String receber(Long codreceber, Double vlrecebido, Double vlacrescimo, Double vldesconto, Long codtitulo) {
        // Validações iniciais
        validarParametrosRecebimento(codtitulo, vlrecebido);
        
        // Buscar entidades
        Recebimento recebimento = obterRecebimento(codreceber);
        Titulo titulo = obterTitulo(codtitulo);
        
        // Validações de negócio
        validarRecebimento(recebimento, vlrecebido);
        
        // Vincular título ao recebimento
        recebimento.setTitulo(titulo);
        
        // Processar parcelas
        processarParcelas(codreceber, vlrecebido);
        
        // Processar lançamento financeiro
        processarLancamentoFinanceiro(vlrecebido, titulo, recebimento);
        
        // Finalizar recebimento
        finalizarRecebimento(recebimento, vlrecebido, vlacrescimo, vldesconto);
        
        return "Recebimento realizado com sucesso";
    }

    private void validarParametrosRecebimento(Long codtitulo, Double vlrecebido) {
        if (codtitulo == null || codtitulo == 0) {
            throw new RecebimentoValidationException("Selecione um título para realizar o recebimento");
        }
        
        if (vlrecebido <= 0.0) {
            throw new RecebimentoValidationException("Valor de recebimento inválido");
        }
    }

    private Recebimento obterRecebimento(Long codreceber) {
        return recebimentos.findById(codreceber)
                .orElseThrow(() -> new RecebimentoNotFoundException("Recebimento não encontrado"));
    }

    private Titulo obterTitulo(Long codtitulo) {
        return titulos.busca(codtitulo)
                .orElseThrow(() -> new RecebimentoNotFoundException("Título não encontrado"));
    }

	private void validarRecebimento(Recebimento recebimento, Double vlrecebido) {
        if (recebimento.getData_processamento() != null) {
            throw new RecebimentoOperationException("Recebimento já está fechado");
        }
        
        Double vlrecebimento = formatarValorRecebimento(recebimento.getValor_total());
        
        if (vlrecebido > vlrecebimento) {
            throw new RecebimentoBusinessException("Valor de recebimento é superior aos títulos");
        }
    }

    private Double formatarValorRecebimento(Double valorTotal) {
        DecimalFormat formata = new DecimalFormat("0.00");
        return Double.valueOf(formata.format(valorTotal).replace(",", "."));
    }

    private void processarParcelas(Long codreceber, Double vlrecebido) {
        List<Parcela> listParcelas = receParcelas.parcelasDoReceber(codreceber);
        
        if (listParcelas.isEmpty()) {
            throw new RecebimentoBusinessException("Recebimento não possui parcelas");
        }
        
        Double valorRestante = vlrecebido;
        
        for (Parcela parcela : listParcelas) {
            if (valorRestante <= 0) {
                break;
            }
            
            valorRestante = processarParcela(parcela, valorRestante);
        }
    }

    private Double processarParcela(Parcela parcela, Double valorRestante) {
        Double vlsobra = valorRestante - parcela.getValor_restante();
        vlsobra = Math.max(vlsobra, 0.0);
        
        Double vlquitado = Math.abs(vlsobra - valorRestante);
        
        try {
            parcelas.receber(parcela.getCodigo(), vlquitado, 0.00, 0.00);
        } catch (Exception e) {
            throw new RecebimentoProcessingException("Erro ao processar parcela " + parcela.getCodigo(), e);
        }
        
        return vlsobra;
    }

    private void processarLancamentoFinanceiro(Double vllancamento, Titulo titulo, Recebimento recebimento) {
        String sigla = titulo.getTipo().getSigla();
        
        if (isLancamentoCartao(sigla)) {
            processarLancamentoCartao(vllancamento, titulo);
        } else {
            processarLancamentoCaixa(vllancamento, recebimento);
        }
    }

    private boolean isLancamentoCartao(String sigla) {
        return sigla.equals(TituloTipo.CARTDEB.toString()) || 
               sigla.equals(TituloTipo.CARTCRED.toString());
    }

    private void processarLancamentoCartao(Double vllancamento, Titulo titulo) {
        try {
            cartaoLancamentos.lancamento(vllancamento, Optional.of(titulo));
        } catch (Exception e) {
            throw new RecebimentoProcessingException("Erro ao processar lançamento no cartão", e);
        }
    }

    private void processarLancamentoCaixa(Double vllancamento, Recebimento recebimento) {
        try {
            Caixa caixa = caixas.caixaAberto()
                    .orElseThrow(() -> new RecebimentoBusinessException("Nenhum caixa aberto encontrado"));
            
            Aplicacao aplicacao = Aplicacao.getInstancia();
		    Usuario usuario = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());
            
            CaixaLancamento lancamento = criarCaixaLancamento(vllancamento, recebimento, caixa, usuario);
            
            lancamentos.lancamento(lancamento);
        } catch (RecebimentoBusinessException e) {
            throw e; // Re-lança exceções de negócio
        } catch (Exception e) {
            throw new RecebimentoProcessingException("Erro ao processar lançamento no caixa", e);
        }
    }

    private CaixaLancamento criarCaixaLancamento(Double vllancamento, Recebimento recebimento, 
                                               Caixa caixa, Usuario usuario) {
        String descricao = "Referente ao recebimento " + recebimento.getCodigo();
        
        CaixaLancamento lancamento = new CaixaLancamento(
            descricao, 
            vllancamento,
            TipoLancamento.RECEBIMENTO, 
            EstiloLancamento.ENTRADA, 
            caixa, 
            usuario
        );
        
        lancamento.setRecebimento(recebimento);
        return lancamento;
    }

    private void finalizarRecebimento(Recebimento recebimento, Double vlrecebido, 
                                     Double vlacrescimo, Double vldesconto) {
        try {
            DataAtual dataAtual = new DataAtual();
            
            recebimento.setValor_recebido(vlrecebido);
            recebimento.setValor_acrescimo(vlacrescimo);
            recebimento.setValor_desconto(vldesconto);
            recebimento.setData_processamento(dataAtual.dataAtualTimeStamp());
            
            recebimentos.save(recebimento);
        } catch (Exception e) {
            throw new RecebimentoProcessingException("Erro ao finalizar recebimento", e);
        }
    }

    public String remover(Long codigo) {
        Optional<Recebimento> recebimento = recebimentos.findById(codigo);

        if (!recebimento.isPresent()) {
            throw new RecebimentoNotFoundException("Recebimento não encontrado");
        }

        if (recebimento.get().getData_processamento() != null) {
            throw new RecebimentoOperationException("Esse recebimento não pode ser removido, pois ele já está processado");
        }

        try {
            recebimentos.deleteById(codigo);
        } catch (Exception e) {
            throw new RecebimentoProcessingException("Erro ao remover recebimento", e);
        }

        return "Recebimento removido com sucesso";
    }
}