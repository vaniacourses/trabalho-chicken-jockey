package net.originmobi.pdv.exception.produto;

public class EstoqueInsuficienteException extends RuntimeException {

	private static final long serialVersionUID = -6836255411454743695L;

	public EstoqueInsuficienteException(String message) {
		super(message);
	}
}
