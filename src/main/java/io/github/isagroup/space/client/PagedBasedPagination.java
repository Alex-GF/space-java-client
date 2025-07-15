package io.github.isagroup.space.client;

public class PagedBasedPagination extends Pagination {

    private final int page;

    public PagedBasedPagination(int page) {
        super();
        this.page = page;
    }

    public PagedBasedPagination(int limit, int page) {
        super(limit);
        this.page = page;
    }

    public int getPage() {
        return page;
    }
}
