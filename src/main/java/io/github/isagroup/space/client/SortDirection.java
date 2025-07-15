package io.github.isagroup.space.client;

public enum SortDirection {
    ASCENDING,
    DESCENDING;

    @Override
    public String toString() {
        return switch (this) {
            case ASCENDING -> "asc";
            case DESCENDING -> "desc";
        };
    }
}
