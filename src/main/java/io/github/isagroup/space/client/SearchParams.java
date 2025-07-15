package io.github.isagroup.space.client;

import java.util.Objects;

public class SearchParams {

    private final Pagination pagination;
    private final SortDirection sortDirection;

    public SearchParams(Pagination pagination) {
        this(pagination, SortDirection.ASCENDING);
    }

    public SearchParams(Pagination pagination, SortDirection sortDirection) {
        Objects.requireNonNull(pagination, "pagination is null");
        Objects.requireNonNull(pagination, "sortDirection is null");
        this.pagination = pagination;
        this.sortDirection = sortDirection;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    @Override
    public String toString() {
        return "SearchParams [pagination=" + pagination + ", sortDirection=" + sortDirection + "]";
    }

}
