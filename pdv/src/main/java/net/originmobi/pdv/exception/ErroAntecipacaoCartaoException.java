package net.originmobi.pdv.exception;

/**
 * Exceção lançada quando ocorre erro durante a antecipação de cartão
 */
public class ErroAntecipacaoCartaoException extends CartaoLancamentoException {
    
    public ErroAntecipacaoCartaoException() {
        super("Erro ao tentar realizar a antecipação, chame o suporte");
    }
    
    public ErroAntecipacaoCartaoException(String message) {
        super(message);
    }
    
    public ErroAntecipacaoCartaoException(String message, Throwable cause) {
        super(message, cause);
    }
} 