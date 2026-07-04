package com.tcc.mandarim.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String mensagem) {
        super(mensagem);
    }

    public ResourceNotFoundException(String recurso, Object id) {
        super(String.format("%s não encontrado(a) com id: %s", recurso, id));
    }
}
