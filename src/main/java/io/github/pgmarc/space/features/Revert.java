package io.github.pgmarc.space.features;

public enum Revert {
    OLDEST_VALUE(false),
    NEWEST_VALUE(true);

    private final boolean latest;

    Revert(boolean latest) {
        this.latest = latest;
    }

    public boolean isLatest() {
        return latest;
    }
}
