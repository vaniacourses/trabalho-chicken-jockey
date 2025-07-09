package net.originmobi.pdv.service.cartao;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import net.originmobi.pdv.enumerado.TituloTipo;
import net.originmobi.pdv.enumerado.caixa.EstiloLancamento;
import net.originmobi.pdv.enumerado.caixa.TipoLancamento;
import net.originmobi.pdv.enumerado.cartao.CartaoSituacao;
import net.originmobi.pdv.enumerado.cartao.CartaoTipo;
import net.originmobi.pdv.exception.ErroAntecipacaoCartaoException;
import net.originmobi.pdv.exception.ErroProcessamentoCartaoException;
import net.originmobi.pdv.exception.RegistroJaAntecipadoException;
import net.originmobi.pdv.exception.RegistroJaProcessadoException;
import net.originmobi.pdv.filter.CartaoFilter;
import net.originmobi.pdv.model.Caixa;
import net.originmobi.pdv.model.CaixaLancamento;
import net.originmobi.pdv.model.Titulo;
import net.originmobi.pdv.model.Usuario;
import net.originmobi.pdv.model.cartao.CartaoLancamento;
import net.originmobi.pdv.model.cartao.MaquinaCartao;
import net.originmobi.pdv.repository.cartao.CartaoLancamentoRepository;
import net.originmobi.pdv.service.CaixaLancamentoService;
import net.originmobi.pdv.service.UsuarioService;
import net.originmobi.pdv.singleton.Aplicacao;
import net.originmobi.pdv.utilitarios.DataAtual;

@Service
public class CartaoLancamentoService {

	private static final Logger logger = LoggerFactory.getLogger(CartaoLancamentoService.class);
	private final CartaoLancamentoRepository repository;
	private final CaixaLancamentoService caixaLancamentos;
	private final UsuarioService usuarios;

	public CartaoLancamentoService(CartaoLancamentoRepository repository, 
								  CaixaLancamentoService caixaLancamentos, 
								  UsuarioService usuarios) {
		this.repository = repository;
		this.caixaLancamentos = caixaLancamentos;
		this.usuarios = usuarios;
	}

	public void lancamento(Double vlParcela, Optional<Titulo> titulo) {
		if (!titulo.isPresent()) {
			return;
		}

		Double taxa = 0.0;
		Double vlTaxa;
		Double vlLiqParcela;

		Double taxaAnte;
		Double vlTaxaAnte;
		Double vlLiqAnt;

		CartaoTipo tipo = null;
		int dias = 0;

		// verifica se é debito ou crédito e pega os valores corretos do titulo
		if (titulo.get().getTipo().getSigla().equals(TituloTipo.CARTDEB.toString())) {
			taxa = titulo.get().getMaquina().getTaxa_debito();
			dias = titulo.get().getMaquina().getDias_debito();
			tipo = CartaoTipo.DEBITO;

		} else if (titulo.get().getTipo().getSigla().equals(TituloTipo.CARTCRED.toString())) {
			taxa = titulo.get().getMaquina().getTaxa_credito();
			dias = titulo.get().getMaquina().getDias_credito();
			tipo = CartaoTipo.CREDITO;
		}

		vlTaxa = (vlParcela * taxa) / 100;
		vlLiqParcela = vlParcela - vlTaxa;

		taxaAnte = titulo.get().getMaquina().getTaxa_antecipacao();
		vlTaxaAnte = (vlParcela * taxaAnte) / 100;
		vlLiqAnt = vlParcela - vlTaxaAnte;

		MaquinaCartao maquinaCartao = titulo.get().getMaquina();

		DataAtual data = new DataAtual();
		LocalDate dataAtual = LocalDate.now();
		String dataRecebimento = data.DataAtualIncrementa(dias);

		CartaoLancamento lancamento = new CartaoLancamento(vlParcela, taxa, vlTaxa, vlLiqParcela, taxaAnte,
				vlTaxaAnte, vlLiqAnt, maquinaCartao, tipo, CartaoSituacao.APROCESSAR,
				Date.valueOf(dataRecebimento), Date.valueOf(dataAtual));

		try {
			repository.save(lancamento);
		} catch (Exception e) {
			logger.error("Erro ao salvar CartaoLancamento", e);
		}

	}

	public List<CartaoLancamento> listar(CartaoFilter filter) {
		String situacao = filter.getSituacao() == null ? "%" : filter.getSituacao().toString();
		String tipo = filter.getTipo() == null ? "%" : filter.getTipo().toString();
		String dataRecebimento = (filter.getData_recebimento() == null || filter.getData_recebimento().isEmpty())
			    ? "%"
			    : filter.getData_recebimento().replace("/", "-");
		return repository.buscaLancamentos(situacao, tipo, dataRecebimento);
	}

	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public String processar(CartaoLancamento cartaoLancamento) {

		if (cartaoLancamento.getSituacao().equals(CartaoSituacao.PROCESSADO))
			throw new RegistroJaProcessadoException();

		if (cartaoLancamento.getSituacao().equals(CartaoSituacao.ANTECIPADO))
			throw new RegistroJaAntecipadoException();

		Double valor = cartaoLancamento.getVlLiqParcela();
		TipoLancamento tipo = TipoLancamento.RECEBIMENTO;
		EstiloLancamento estilo = EstiloLancamento.ENTRADA;
		Caixa banco = cartaoLancamento.getMaquina_cartao().getBanco();

		Aplicacao aplicacao = Aplicacao.getInstancia();
		Usuario usuario = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

		CaixaLancamento lancamento = new CaixaLancamento("Referênte a processamento de cartão", valor, tipo, estilo,
				banco, usuario);

		try {
			caixaLancamentos.lancamento(lancamento);
		} catch (Exception e) {
			throw new ErroProcessamentoCartaoException("Erro ao tentar realizar o processamento, chame o suporte", e);
		}

		try {
			cartaoLancamento.setSituacao(CartaoSituacao.PROCESSADO);
		} catch (Exception e) {
			throw new ErroProcessamentoCartaoException("Erro ao tentar realizar o processamento, chame o suporte", e);
		}

		return "Processamento realizado com sucesso";
	}

	public String antecipar(CartaoLancamento cartaoLancamento) {
		if (cartaoLancamento.getSituacao().equals(CartaoSituacao.PROCESSADO))
			throw new RegistroJaProcessadoException();

		if (cartaoLancamento.getSituacao().equals(CartaoSituacao.ANTECIPADO))
			throw new RegistroJaAntecipadoException();

		Double valor = cartaoLancamento.getVlLiqAntecipacao();
		TipoLancamento tipo = TipoLancamento.RECEBIMENTO;
		EstiloLancamento estilo = EstiloLancamento.ENTRADA;
		Caixa banco = cartaoLancamento.getMaquina_cartao().getBanco();

		Aplicacao aplicacao = Aplicacao.getInstancia();
		Usuario usuario = usuarios.buscaUsuario(aplicacao.getUsuarioAtual());

		CaixaLancamento lancamento = new CaixaLancamento(
				"Referênte a antecipação de cartão código " + cartaoLancamento.getCodigo(), valor, tipo, estilo, banco,
				usuario);

		try {
			caixaLancamentos.lancamento(lancamento);
		} catch (Exception e) {
			throw new ErroAntecipacaoCartaoException("Erro ao tentar realizar a antecipação, chame o suporte", e);
		}

		try {
			cartaoLancamento.setSituacao(CartaoSituacao.ANTECIPADO);
			repository.save(cartaoLancamento);
		} catch (Exception e) {
			throw new ErroAntecipacaoCartaoException("Erro ao tentar realizar a antecipação, chame o suporte", e);
		}

		return "Antecipação realizada com sucesso";
	}

}
