package util;

import java.util.Objects;

public final class Timestamp<T> {

    private final long timestamp;
    private final T object;

    public Timestamp(T object) {
        this.timestamp = System.currentTimeMillis();
        this.object = object;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public T get() {
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timestamp<?> timestamp = (Timestamp<?>) o;
        return object.equals(timestamp.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(object);
    }

    @Override
    public String toString() {
        return "Timestamp{" +
                "timestamp=" + timestamp +
                ", object=" + object +
                '}';
    }
}
