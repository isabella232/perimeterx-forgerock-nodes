package com.perimeterx.BD.nodes.PX.Exceptions;

public class PXCookieDecryptionException extends Exception {

    public PXCookieDecryptionException(Throwable cause) {
        super(cause);
    }

    public PXCookieDecryptionException(String message) {
        super(message);
    }

    public PXCookieDecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
