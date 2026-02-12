package io.github.pgmarc.space.contracts;

import java.util.Objects;

public final class AddOn {

    private final String name;
    private final long quantity;

    AddOn(String name, long quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public long getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AddOn addOn = (AddOn) o;
        return quantity == addOn.quantity && Objects.equals(name, addOn.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, quantity);
    }

    @Override
    public String toString() {
        return "AddOn{" +
            "name='" + name + '\'' +
            ", quantity=" + quantity +
            '}';
    }
}
