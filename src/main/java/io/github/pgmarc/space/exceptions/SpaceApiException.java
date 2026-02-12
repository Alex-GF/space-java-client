package io.github.pgmarc.space.exceptions;


public class SpaceApiException extends RuntimeException {

    private transient SpaceApiError error;

    public SpaceApiException(SpaceApiError error) {
        super(error.toString());
        this.error = error;
    }

    public SpaceApiException(String message) {
        super(message);
    }

    public int getCode() {
        return error.getStatusCode();
    }
}
