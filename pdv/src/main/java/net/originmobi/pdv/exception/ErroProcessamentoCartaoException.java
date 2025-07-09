package net.originmobi.pdv.exception;

/**
 * Exceção lançada quando ocorre erro durante o processamento de cartão
 */
public class ErroProcessamentoCartaoException extends CartaoLancamentoException {
    
    public ErroProcessamentoCartaoException() {
        super("Erro ao tentar realizar o processamento, chame o suporte");
    }
    
    public ErroProcessamentoCartaoException(String message) {
        super(message);
    }
    
    public ErroProcessamentoCartaoException(String message, Throwable cause) {
        super(message, cause);
    }
} 