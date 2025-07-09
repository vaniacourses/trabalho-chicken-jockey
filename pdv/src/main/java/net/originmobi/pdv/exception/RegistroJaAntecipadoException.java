package net.originmobi.pdv.exception;

/**
 * Exceção lançada quando um registro de cartão já foi antecipado
 */
public class RegistroJaAntecipadoException extends CartaoLancamentoException {
    
    public RegistroJaAntecipadoException() {
        super("Registro já foi antecipado");
    }
    
    public RegistroJaAntecipadoException(String message) {
        super(message);
    }
} 