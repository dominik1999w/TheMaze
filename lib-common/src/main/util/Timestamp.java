package util;

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
}
