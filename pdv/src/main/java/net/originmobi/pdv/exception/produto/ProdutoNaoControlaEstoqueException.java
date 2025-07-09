package net.originmobi.pdv.exception.produto;

public class ProdutoNaoControlaEstoqueException extends RuntimeException {

	private static final long serialVersionUID = -6836255411454743695L;

	public ProdutoNaoControlaEstoqueException(String message) {
		super(message);
	}
}
