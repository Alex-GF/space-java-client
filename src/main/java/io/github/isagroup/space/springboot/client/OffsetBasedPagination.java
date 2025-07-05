package io.github.isagroup.space.springboot.client;

public class OffsetBasedPagination extends Pagination {

    private final int offset;

    public OffsetBasedPagination(int offset) {
        super();
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be larger than 0");
        }
        this.offset = offset;
    }

    public OffsetBasedPagination(int limit, int offset) {
        super(limit);
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be larger than 0");
        }
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

}
