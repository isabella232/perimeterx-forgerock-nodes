package com.perimeterx.BD.nodes.PX.Exceptions;

public class PXException extends Exception {

    public PXException(Throwable cause) {
        super(cause);
    }

    public PXException(String message) {
        super(message);
    }

    public PXException(String message, Throwable cause) {
        super(message, cause);
    }
}
