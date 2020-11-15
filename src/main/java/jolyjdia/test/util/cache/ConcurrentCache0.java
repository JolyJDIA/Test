package jolyjdia.test.util.cache;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class ConcurrentCache0<K, V> {
    private final LongAdder counter = new LongAdder();
    private volatile Node<K, V>[] buckets = new Node[10];

    public ConcurrentCache0() {
        System.out.println("[eq "+ASHIFT);
    }

    public int size() {
        return counter.intValue();
    }

    public V get(Object key) {
        if (key == null) throw new IllegalArgumentException();
        int hash = hash(key);
        Node<K, V> node;

        // volatile read of bucket head at hash index
        if ((node = volatileGetNode(buckets, getBucketIndex(hash))) != null) {
            // check first node
            if (isKeyEquals(key, hash, node)) {
                return node.value;
            }

            // walk through the rest to find target node
            while ((node = node.next) != null) {
                if (isKeyEquals(key, hash, node))
                    return node.value;
            }
        }

        return null;
    }


    public V put(K key, V value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        int hash = hash(key);
        // no resize in this implementation - so the index will not change
        int bucketIndex = getBucketIndex(hash);
        // cas loop trying not to miss
        for (Node<K,V>[] tab = buckets;;) {
            Node<K, V> node;
            // if bucket is empty try to set new head with cas
            if ((node = volatileGetNode(buckets, bucketIndex)) == null) {
                if (compareAndSwapNode(buckets, bucketIndex, null,
                        new Node<>(hash, key, value, null))) {
                    // if we succeed to set head - then break and return null

                    break;
                }
            } else {
                // head is not null - try to find place to insert or update under lock
                synchronized (node) {
                    // check if node have not been changed since we got it
                    // otherwise let's go to another loop iteration
                    if (volatileGetNode(buckets, bucketIndex) == node) {
                        V prevValue = null;
                        Node<K, V> n = node;
                        while (true) {
                            if (isKeyEquals(key, hash, n)) {
                                prevValue = n.value;
                                n.value = value;
                                break;
                            }

                            Node<K, V> prevNode = n;
                            if ((n = n.next) == null) {
                                prevNode.next = new Node<>(hash, key, value, null);
                                break;
                            }
                        }
                        return prevValue;
                    }
                }
            }
        }
        counter.increment();
        return null;
    }
    public V remove(Object key) {
        if (key == null) throw new IllegalArgumentException();
        int hash = hash(key);

        return null;
    }

    private int hash(Object key) {
        return key.hashCode();
    }

    private int getBucketIndex(int hash) {
        return hash % buckets.length;
    }

    private boolean isKeyEquals(Object key, int hash, Node<K, V> node) {
        return node.hash == hash &&
                node.key == key ||
                (node.key != null && node.key.equals(key));
    }

    private static class Node<K, V> {
        final int hash;
        final K key;
        volatile V value;
        volatile Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
        @Override
        public final int hashCode() {
            return key.hashCode() ^ value.hashCode();
        }
        @Override
        public final boolean equals(Object o) {
            Object k, v, u;
            Map.Entry<?, ?> e;
            return ((o instanceof Map.Entry) &&
                    (k = (e = (Map.Entry<?, ?>) o).getKey()) != null &&
                    (v = e.getValue()) != null &&
                    (k == key || k.equals(key)) &&
                    (v == (u = value) || v.equals(u)));
        }
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Node<K, V> volatileGetNode(Node<K,V>[] tab, int i) {
        return (Node<K, V>) U.getObjectVolatile(tab, ((long) i << ASHIFT) + ABASE);
    }

    private static <K, V> boolean compareAndSwapNode(Node<K,V>[] tab, int i, Node<K, V> expectedNode, Node<K, V> setNode) {
        return U.compareAndSwapObject(tab, ((long) i << ASHIFT) + ABASE, expectedNode, setNode);
    }

    //private static final VarHandle AA = MethodHandles.arrayElementVarHandle(Node[].class);
    private static final int ABASE;
    private static final int ASHIFT;
    private static final sun.misc.Unsafe U;
    static {
        try {
            Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
            unsafeConstructor.setAccessible(true);
            U = unsafeConstructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        int scale = U.arrayIndexScale(Node[].class);
        if ((scale & (scale - 1)) != 0) {
            throw new ExceptionInInitializerError("array index scale not a power of two");
        }
        ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
        ABASE = U.arrayBaseOffset(Node[].class);
    }
}
