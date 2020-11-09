package jolyjdia.test.util.cache;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConcurrentCache<K, V> {
    @SuppressWarnings("rawtypes")
    static final CompletableFuture[] A = new CompletableFuture[0];
    static final int NESTED  = -1;
    static final int REMOVAL = -2;

    private final ConcurrentHashMap<K, Node<V>> map;
    private final CacheBuilder<K, V> builder;
    private final CacheBuilder.AsyncCacheLoader<K, V> cacheLoader;

    public ConcurrentCache(CacheBuilder.AsyncCacheLoader<K, V> cacheLoader, CacheBuilder<K, V> builder) {
        this.cacheLoader = cacheLoader;
        float loadFactor = builder.getLoadFactor();
        int buckets = (int)(builder.getMaxSize() / loadFactor) + 1;
        this.map = new ConcurrentHashMap<>(buckets, loadFactor, builder.getConcurrencyLevel());
        this.builder = builder;
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(builder.getTick());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                for (Map.Entry<K, Node<V>> entry : map.entrySet()) {
                    Node<V> vNode = entry.getValue();
                    if (vNode.refresh == REMOVAL) {//Процесс удаления уже идет
                        return;
                    }
                    long afterAccess = builder.getExpireAfterAccess(),
                            afterWrite = builder.getExpireAfterWrite(),
                            now = System.currentTimeMillis();
                    if ((afterAccess != NESTED && now - vNode.refresh >= afterAccess) ||
                        (afterWrite  != NESTED && now - vNode.start   >= afterWrite))
                    {
                        vNode.refresh = REMOVAL;//задаю статус удаления
                        safeRemoval(entry.getKey(), vNode).thenAccept(remove -> {
                            if (remove) {
                                //Проверяю, вдруг я уже где-то обновил значение
                                if (vNode.refresh == REMOVAL) {
                                    map.remove(entry.getKey());
                                }
                            } else {
                                vNode.refresh = now;
                            }
                        });
                    }
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("cleaner");
        thread.start();
    }
    public CompletableFuture<V> get(K key) {
        Node<V> valueCache = map.get(key);
        if(valueCache != null) {
            valueCache.refresh = System.currentTimeMillis();
        } else {
            valueCache = new Node<>(cacheLoader.asyncLoad(key, builder.getExecutor()));
            map.put(key, valueCache);
        }
        return valueCache.cf;
    }
    public CompletableFuture<Boolean> remove(K key) {
        Node<V> node = map.get(key);
        return safeRemoval(key, node).thenApply(b -> {
            if(b) {
                map.remove(key);
            }
            return b;
        });
    }
    public CompletableFuture<Boolean> removePoxyu(K key) {
        return safeRemoval(key, map.remove(key));
    }

    public CompletableFuture<Void> removeAll() {
        final Set<Map.Entry<K, Node<V>>> entrySet = map.entrySet();
        List<CompletableFuture<Void>> cfs = new ArrayList<>(entrySet.size());
        for(Map.Entry<K, Node<V>> e : entrySet) {
            K key = e.getKey(); Node<V> node = e.getValue();
            CompletableFuture<Void> cf = new CompletableFuture<>();
            safeRemoval(key, node).thenAccept(b -> {
                if (b) {
                    map.remove(key);
                    cf.complete(null);
                }
            });
            cfs.add(cf);
        }
        return CompletableFuture.allOf(cfs.toArray(A));
    }
    private static class Node<V> {
        private final CompletableFuture<V> cf;
        private final long start = System.currentTimeMillis();
        private volatile long refresh = start;

        public Node(CompletableFuture<V> cf) {
            this.cf = cf;
        }
        @Override
        public String toString() {
            @NonNls String s = "Node{" +
                    "CompletableFuture=" + cf +
                    ", start=" + start +
                    ", refresh=" + refresh;
            if (refresh == REMOVAL) {
                s += ", status=removal";
            }
            s += '}';
            return s;
        }
    }

    /**
     * Метод защищающий поток Cleaner от левостороннего кода
     * так же перенаправляет работу в наш ексекютор
     */
    private CompletableFuture<Boolean> safeRemoval(K key, @NotNull Node<V> node) {
        //Согласное спеки, завершенные Cf выполнятся в этом треде
        CompletableFuture<V> cf = node.cf;

        return cf.thenComposeAsync(f -> {
            return builder.getRemoval().onRemoval(key, cf);
        }, builder.getExecutor());
    }
    @NonNls
    @Override
    public String toString() {
        return "AsyncCache{" + "map=" + map + '}';
    }
}
