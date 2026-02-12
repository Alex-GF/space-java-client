package io.github.pgmarc.space.exceptions;

public final class FeatureEvaluationError {

    public enum Code {
        EVALUATION_ERROR,
        FLAG_NOT_FOUND,
        GENERAL,
        INVALID_EXPECTED_CONSUMPTION,
        PARSE_ERROR,
        TYPE_MISMATCH
    }

    private final Code code;
    private final String message;

    private FeatureEvaluationError(Code code, String reason) {
        this.code = code;
        this.message = reason;
    }

    public static FeatureEvaluationError of(String code, String reason) {
        return new FeatureEvaluationError(Code.valueOf(code), reason);
    }

    private String formatMessage() {
        return message.endsWith(".") ? message : message + ".";
    }

    @Override
    public String toString() {
        return formatMessage() + " Error code: " + code;
    }

}
