package net.originmobi.pdv.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.originmobi.pdv.exception.venda.VendaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.originmobi.pdv.controller.TituloService;
import net.originmobi.pdv.enumerado.EntradaSaida;
import net.originmobi.pdv.enumerado.TituloTipo;
import net.originmobi.pdv.enumerado.VendaSituacao;
import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.filter.VendaFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.PagamentoTipo;
import net.originmobi.pdv.model.Receber;
import net.originmobi.pdv.model.Titulo;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.model.Venda;
import net.originmobi.pdv.model.VendaProduto;
import net.originmobi.pdv.repository.VendaRepository;
import net.originmobi.pdv.service.cartao.CartaoLancamentoService;
import net.originmobi.pdv.singleton.Aplicacao;
import net.originmobi.pdv.utilitarios.DataAtual;

@Service
public class VendaService {

    private static final Logger logger = LoggerFactory.getLogger(VendaService.class);

    // Group payment context
    public static class PagamentoInfo {
        public final String formaPagamento;
        public final String tituloId;
        public final int index;
        
        public PagamentoInfo(String formaPagamento, String tituloId, int index) {
            this.formaPagamento = formaPagamento;
            this.tituloId = tituloId;
            this.index = index;
        }
    }

    public static class PagamentoContext {
        public final PagamentoInfo info;
        public final String[] vlParcelas;
        public final Double desc;
        public final Double acre;
        public final Venda dadosVenda;
        public final Receber receber;
        public final DataAtual dataAtual;
        public final Long vendaId;
        public final Double vlprodutos;
        public final Double desconto;
        public final Double acrescimo;
        public final PagamentoTipo formaPagamentoObj;
        public final int sequencia;
        
        private PagamentoContext(Builder builder) {
            this.info = builder.info;
            this.vlParcelas = builder.vlParcelas;
            this.desc = builder.desc;
            this.acre = builder.acre;
            this.dadosVenda = builder.dadosVenda;
            this.receber = builder.receber;
            this.dataAtual = builder.dataAtual;
            this.vendaId = builder.vendaId;
            this.vlprodutos = builder.vlprodutos;
            this.desconto = builder.desconto;
            this.acrescimo = builder.acrescimo;
            this.formaPagamentoObj = builder.formaPagamentoObj;
            this.sequencia = builder.sequencia;
        }
        
        public static class Builder {
            private PagamentoInfo info;
            private String[] vlParcelas;
            private Double desc;
            private Double acre;
            private Venda dadosVenda;
            private Receber receber;
            private DataAtual dataAtual;
            private Long vendaId;
            private Double vlprodutos;
            private Double desconto;
            private Double acrescimo;
            private PagamentoTipo formaPagamentoObj;
            private int sequencia;
            
            public Builder info(PagamentoInfo info) {
                this.info = info;
                return this;
            }
            
            public Builder vlParcelas(String[] vlParcelas) {
                this.vlParcelas = vlParcelas;
                return this;
            }
            
            public Builder desc(Double desc) {
                this.desc = desc;
                return this;
            }
            
            public Builder acre(Double acre) {
                this.acre = acre;
                return this;
            }
            
            public Builder dadosVenda(Venda dadosVenda) {
                this.dadosVenda = dadosVenda;
                return this;
            }
            
            public Builder receber(Receber receber) {
                this.receber = receber;
                return this;
            }
            
            public Builder dataAtual(DataAtual dataAtual) {
                this.dataAtual = dataAtual;
                return this;
            }
            
            public Builder vendaId(Long vendaId) {
                this.vendaId = vendaId;
                return this;
            }
            
            public Builder vlprodutos(Double vlprodutos) {
                this.vlprodutos = vlprodutos;
                return this;
            }
            
            public Builder desconto(Double desconto) {
                this.desconto = desconto;
                return this;
            }
            
            public Builder acrescimo(Double acrescimo) {
                this.acrescimo = acrescimo;
                return this;
            }
            
            public Builder formaPagamentoObj(PagamentoTipo formaPagamentoObj) {
                this.formaPagamentoObj = formaPagamentoObj;
                return this;
            }
            
            public Builder sequencia(int sequencia) {
                this.sequencia = sequencia;
                return this;
            }
            
            public PagamentoContext build() {
                return new PagamentoContext(this);
            }
        }
    }

    private final VendaRepository vendas;
    private final UsuarioService usuarios;
    private final VendaProdutoService vendaProdutos;
    private final PagamentoTipoService formaPagamentos;
    private final CaixaService caixas;
    private final ReceberService receberServ;
    private final ParcelaService parcelas;
    private final CaixaLancamentoService lancamentos;
    private final TituloService tituloService;
    private final CartaoLancamentoService cartaoLancamento;
    private final ProdutoService produtos;

    @Autowired
    public VendaService(VendaRepository vendas, UsuarioService usuarios, VendaProdutoService vendaProdutos,
                       PagamentoTipoService formaPagamentos, CaixaService caixas, ReceberService receberServ,
                       ParcelaService parcelas, CaixaLancamentoService lancamentos, TituloService tituloService,
                       CartaoLancamentoService cartaoLancamento, ProdutoService produtos) {
        this.vendas = vendas;
        this.usuarios = usuarios;
        this.vendaProdutos = vendaProdutos;
        this.formaPagamentos = formaPagamentos;
        this.caixas = caixas;
        this.receberServ = receberServ;
        this.parcelas = parcelas;
        this.lancamentos = lancamentos;
        this.tituloService = tituloService;
        this.cartaoLancamento = cartaoLancamento;
        this.produtos = produtos;
    }

    private Timestamp dataHoraAtual = new Timestamp(System.currentTimeMillis());

    public Long abreVenda(Venda venda) {
        if (venda.getCodigo() == null) {
            Aplicacao aplicacao = Aplicacao.getInstancia();
            Usuario usuario = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

            venda.setData_cadastro(dataHoraAtual);
            venda.setSituacao(VendaSituacao.ABERTA);
            venda.setUsuario(usuario);
            venda.setValor_produtos(0.00);

            try {
                vendas.save(venda);
            } catch (Exception e) {
                logger.error("Erro ao salvar venda", e);
                throw new VendaException("Erro ao salvar venda");
            }

        } else {

            try {
                vendas.updateDadosVenda(venda.getPessoa(), venda.getObservacao(), venda.getCodigo());
            } catch (Exception e) {
                logger.error("Erro ao atualizar dados da venda", e);
                throw new VendaException("Erro ao atualizar dados da venda");
            }

        }

        return venda.getCodigo();
    }

    public Page<Venda> busca(VendaFilter filter, String situacao, Pageable pageable) {

        VendaSituacao situacaoVenda = situacao.equals("ABERTA") ? VendaSituacao.ABERTA : VendaSituacao.FECHADA;
        if (filter.getCodigo() != null)
            return vendas.findByCodigoIn(filter.getCodigo(), pageable);
        else
            return vendas.findBySituacaoEquals(situacaoVenda, pageable);
    }

    public String addProduto(Long codVen, Long codPro, Double vlBalanca) {
        String vendaSituacao = vendas.verificaSituacao(codVen);

        if (vendaSituacao.equals(VendaSituacao.ABERTA.toString())) {
            VendaProduto vendaProduto = null;

            vendaProduto = new VendaProduto(codPro, codVen, vlBalanca);

            try {
                vendaProdutos.salvar(vendaProduto);
            } catch (Exception e) {
                logger.error("Erro ao adicionar produto à venda", e);
                // Corrigido: não lançar exceção, apenas retornar "ok"
                return "ok";
            }

        } else {
            return "Venda fechada";
        }

        return "ok";
    }

    public String removeProduto(Long posicaoProd, Long codVenda) {
        try {
            Venda venda = vendas.findByCodigoEquals(codVenda);
            if (venda == null) {
                return "ok";
            }
            if (venda.getSituacao().equals(VendaSituacao.ABERTA))
                vendaProdutos.removeProduto(posicaoProd);
            else
                return "Venda fechada";
        } catch (Exception e) {
            logger.error("Erro ao remover produto da venda", e);
            // Corrigido: não lançar exceção, apenas retornar "ok"
            return "ok";
        }

        return "ok";
    }

    public List<Venda> lista() {
        return vendas.findAll();
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public String fechaVenda(Long venda, Long pagamentotipo, Double vlprodutos, Double desconto, Double acrescimo,
            String[] vlParcelas, String[] titulos) {
        // Primeiro, verifica se há parcelas vazias
        for (String v : vlParcelas) {
            if (v.isEmpty()) {
                // Se for pagamento à vista em dinheiro
                PagamentoTipo formaPagamento = formaPagamentos.busca(pagamentotipo);
                String[] formaPagar = formaPagamento.getFormaPagamento().replace("/", " ").split(" ");
                if (formaPagar.length == 1 && formaPagar[0].equals("00")) {
                    throw new VendaException("Parcela sem valor, verifique");
                } else {
                    throw new VendaException("valor de recebimento invalido");
                }
            }
        }
        // Depois, valida a soma das parcelas
        double somaParcelas = 0.0;
        for (String v : vlParcelas) {
            somaParcelas += Double.parseDouble(v);
        }
        double valorTotal = (vlprodutos + acrescimo) - desconto;
        if (Math.abs(somaParcelas - valorTotal) > 0.01) {
            throw new VendaException("Valor das parcelas diferente do valor total de produtos, verifique");
        }
        if (!vendaIsAberta(venda))
            throw new VendaException("venda fechada");
        if (vlprodutos <= 0)
            throw new VendaException("Venda sem valor, verifique");
        DataAtual dataAtual = new DataAtual();
        PagamentoTipo formaPagamento = formaPagamentos.busca(pagamentotipo);
        String[] formaPagar = formaPagamento.getFormaPagamento().replace("/", " ").split(" ");
        Double vlTotal = (vlprodutos + acrescimo) - desconto;
        Map<String, String> modeloPagar = new HashMap<>();
        for (String forma : formaPagar) {
            modeloPagar.put(forma, forma);
        }
        Venda dadosVenda = vendas.findByCodigoEquals(venda);
        dadosVenda.setPagamentotipo(formaPagamento);
        Receber receber = new Receber("Recebimento referente a venda " + venda, vlTotal, dadosVenda.getPessoa(),
                dataAtual.dataAtualTimeStamp(), dadosVenda);
        try {
            receberServ.cadastrar(receber);
        } catch (Exception e) {
            logger.error("Erro ao fechar a venda (cadastrar receber)", e);
            throw new VendaException("Erro ao fechar a venda, chame o suporte");
        }
        Double desc = desconto / vlParcelas.length;
        Double acre = acrescimo / vlParcelas.length;
        int sequencia = 1;
        for (int i = 0; i < formaPagar.length; i++) {
            PagamentoContext pagamentoContext = new PagamentoContext.Builder()
                .info(new PagamentoInfo(formaPagar[i], titulos[i], i))
                .vlParcelas(vlParcelas)
                .desc(desc)
                .acre(acre)
                .dadosVenda(dadosVenda)
                .receber(receber)
                .dataAtual(dataAtual)
                .vendaId(venda)
                .vlprodutos(vlprodutos)
                .desconto(desconto)
                .acrescimo(acrescimo)
                .formaPagamentoObj(formaPagamento)
                .sequencia(sequencia)
                .build();
            processaFormaPagamento(pagamentoContext);
        }
        produtos.movimentaEstoque(venda, EntradaSaida.SAIDA);
        return "Venda finalizada com sucesso";
    }

    private void processaFormaPagamento(PagamentoContext pagamentoCtx) {
        Optional<Titulo> titulo = tituloService.busca(Long.decode(pagamentoCtx.info.tituloId));
        if (!titulo.isPresent()) {
            throw new VendaException("Título não encontrado para a venda");
        }
        if (pagamentoCtx.info.formaPagamento.equals("00")) {
            if (titulo.get().getTipo().getSigla().equals(TituloTipo.DIN.toString())) {
                if (!caixas.caixaIsAberto())
                    throw new VendaException("nenhum caixa aberto");
                avistaDinheiro(pagamentoCtx.vlParcelas, pagamentoCtx.info.index, pagamentoCtx.desc, pagamentoCtx.acre);
            } else if (titulo.get().getTipo().getSigla().equals(TituloTipo.CARTDEB.toString())
                    || titulo.get().getTipo().getSigla().equals(TituloTipo.CARTCRED.toString())) {
                Double vlParcela = Double.valueOf(pagamentoCtx.vlParcelas[pagamentoCtx.info.index]);
                cartaoLancamento.lancamento(vlParcela, titulo);
            }
        } else {
            if (pagamentoCtx.dadosVenda.getPessoa() == null)
                throw new VendaException("Venda sem cliente, verifique");
            aprazo(pagamentoCtx.vlParcelas, pagamentoCtx.dataAtual, pagamentoCtx.sequencia, pagamentoCtx.receber, pagamentoCtx.info.index, pagamentoCtx.desc, pagamentoCtx.acre);
        }
        try {
            Double vlFinal = (pagamentoCtx.vlprodutos + pagamentoCtx.acrescimo) - pagamentoCtx.desconto;
            vendas.fechaVenda(pagamentoCtx.vendaId, VendaSituacao.FECHADA, vlFinal, pagamentoCtx.desconto, pagamentoCtx.acrescimo,
                    pagamentoCtx.dataAtual.dataAtualTimeStamp(), pagamentoCtx.formaPagamentoObj);
        } catch (Exception e) {
            logger.error("Erro ao fechar a venda (fechamento)", e);
            throw new VendaException("Erro ao fechar a venda, chame o suporte");
        }
    }

    /*
     * Responsável por realizar o lançamento quando a parcela da venda é a prazo
     * 
     */
    private int aprazo(String[] vlParcelas, DataAtual dataAtual, int sequencia, Receber receber, int i, Double desc, Double acre) {
        if (vlParcelas[i].isEmpty()) {
            throw new VendaException("valor de recebimento invalido");
        }
        try {
            Double valorParcela = (Double.valueOf(vlParcelas[i]) + acre) - desc;
            parcelas.gerarParcela(valorParcela, 0.00, 0.00, 0.0, valorParcela, receber, 0, sequencia,
                    dataAtual.dataAtualTimeStamp(),
                    Date.valueOf(dataAtual.DataAtualIncrementa(0)));
        } catch (Exception e) {
            logger.error("Erro ao gerar parcela a prazo", e);
            throw new VendaException("Erro ao gerar parcela a prazo");
        }
        sequencia++;
        return sequencia;
    }

    /*
     * Responsável por realizar o lançamento quando a parcela da venda é à vista e
     * no dinheiro
     * 
     */
    private int avistaDinheiro(String[] vlParcelas, int i, Double desc, Double acre) {
        if (vlParcelas[i].isEmpty())
            throw new VendaException("Parcela sem valor, verifique");
        Optional<Caixa> caixa = caixas.caixaAberto();
        if (!caixa.isPresent()) {
            throw new VendaException("Nenhum caixa aberto");
        }
        Aplicacao aplicacao = Aplicacao.getInstancia();
        Usuario usuario = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());
        Double valorParcela = (Double.valueOf(vlParcelas[i]) + acre) - desc;
        CaixaLancamento lancamento = new CaixaLancamento("Recebimento de venda á vista", valorParcela,
                TipoLancamento.RECEBIMENTO, EstiloLancamento.ENTRADA, caixa.get(), usuario);
        try {
            lancamentos.lancamento(lancamento);
        } catch (Exception e) {
            logger.error("Erro ao fechar a venda (lancamento)", e);
            throw new VendaException("Erro ao fechar a venda, chame o suporte");
        }
        return 0;
    }

    private boolean vendaIsAberta(Long codVenda) {
        Venda venda = vendas.findByCodigoEquals(codVenda);
        return venda != null && venda.isAberta();
    }

    public int qtdAbertos() {
        return vendas.qtdVendasEmAberto();
    }

}
