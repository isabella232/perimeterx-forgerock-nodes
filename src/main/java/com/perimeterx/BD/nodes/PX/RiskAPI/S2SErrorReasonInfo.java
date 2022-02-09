package com.perimeterx.BD.nodes.PX.RiskAPI;

public class S2SErrorReasonInfo {
    private S2SErrorReason reason;
    private String message;
    private int httpStatus;
    private String httpMessage;

    public S2SErrorReasonInfo() {
        setS2SErrorInfo(S2SErrorReason.NO_ERROR, null);
    }

    public S2SErrorReasonInfo(S2SErrorReason reason, String message) {
        setS2SErrorInfo(reason, message);
    }

    public S2SErrorReasonInfo(S2SErrorReason reason, String message, int httpStatus, String httpMessage) {
        setS2SErrorInfo(reason, message, httpStatus, httpMessage);
    }

    public void setS2SErrorInfo(S2SErrorReason reason, String message) {
        this.reason = reason;
        this.message = message;
    }

    public void setS2SErrorInfo(S2SErrorReason reason, String message, int httpStatus, String httpMessage) {
        setS2SErrorInfo(reason, message);
        this.httpStatus = httpStatus;
        this.httpMessage = httpMessage;
    }

    public boolean isErrorSet() {
        return this.reason != S2SErrorReason.NO_ERROR;
    }

    public S2SErrorReason getReason() {
        return this.reason;
    }

    public String getMessage() {
        return this.message;
    }

    public int getHttpStatus() {
        return this.httpStatus;
    }

    public String getHttpMessage() {
        return this.httpMessage;
    }
}
