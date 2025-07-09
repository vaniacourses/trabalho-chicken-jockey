package net.originmobi.pdv.exception;

/**
 * Exceção base para operações relacionadas a lançamentos de cartão
 */
public class CartaoLancamentoException extends RuntimeException {
    
    public CartaoLancamentoException(String message) {
        super(message);
    }
    
    public CartaoLancamentoException(String message, Throwable cause) {
        super(message, cause);
    }
} 