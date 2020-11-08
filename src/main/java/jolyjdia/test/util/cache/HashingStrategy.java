package jolyjdia.test.util.cache;

public interface HashingStrategy<K> {

    int computeHashCode(K key);

    boolean equals(K key1, K key2);
}
