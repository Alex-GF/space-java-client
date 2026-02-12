package io.github.isagroup.spaceclient.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Feature evaluation result
 */
public class FeatureEvaluationResult {
    
    @JsonProperty("eval")
    private boolean eval;
    
    @JsonProperty("used")
    private Map<String, Object> used;
    
    @JsonProperty("limit")
    private Map<String, Object> limit;
    
    @JsonProperty("error")
    private EvaluationError error;

    public FeatureEvaluationResult() {
    }

    public FeatureEvaluationResult(boolean eval, Map<String, Object> used, Map<String, Object> limit, EvaluationError error) {
        this.eval = eval;
        this.used = used;
        this.limit = limit;
        this.error = error;
    }

    // Getters and Setters
    public boolean isEval() {
        return eval;
    }

    public void setEval(boolean eval) {
        this.eval = eval;
    }

    public Map<String, Object> getUsed() {
        return used;
    }

    public void setUsed(Map<String, Object> used) {
        this.used = used;
    }

    public Map<String, Object> getLimit() {
        return limit;
    }

    public void setLimit(Map<String, Object> limit) {
        this.limit = limit;
    }

    public EvaluationError getError() {
        return error;
    }

    public void setError(EvaluationError error) {
        this.error = error;
    }

    /**
     * Evaluation error information
     */
    public static class EvaluationError {
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("message")
        private String message;

        public EvaluationError() {
        }

        public EvaluationError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
