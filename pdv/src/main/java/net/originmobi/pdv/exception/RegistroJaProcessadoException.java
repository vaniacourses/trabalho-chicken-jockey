package net.originmobi.pdv.exception;

/**
 * Exceção lançada quando um registro de cartão já foi processado
 */
public class RegistroJaProcessadoException extends CartaoLancamentoException {
    
    public RegistroJaProcessadoException() {
        super("Registro já processado");
    }
    
    public RegistroJaProcessadoException(String message) {
        super(message);
    }
} 