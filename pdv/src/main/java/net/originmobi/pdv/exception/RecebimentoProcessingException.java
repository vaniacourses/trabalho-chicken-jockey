package net.originmobi.pdv.exception;

public class RecebimentoProcessingException extends RuntimeException {

    private static final long serialVersionUID = -5678901234567890123L;

    public RecebimentoProcessingException(String message) {
        super(message);
    }
    
    public RecebimentoProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
