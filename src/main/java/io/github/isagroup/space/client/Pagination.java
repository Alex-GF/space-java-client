package io.github.isagroup.space.client;

public abstract class Pagination {

    private static final int DEFAULT_RESULTS_SIZE = 20;

    private final int limit;

    protected Pagination() {
        this.limit = DEFAULT_RESULTS_SIZE;
    }

    protected Pagination(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be a positive number greater than zero");
        }
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }
}
