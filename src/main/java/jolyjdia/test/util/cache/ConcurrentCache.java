package jolyjdia.test.util.cache;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ConcurrentCache<K, V> {
    @SuppressWarnings("rawtypes")
    static final CompletableFuture[] A = new CompletableFuture[0];
    static final int NESTED = -1;

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
                    if (vNode.removal != null) {//Процесс удаления уже идет
                        return;
                    }
                    long afterAccess = builder.getExpireAfterAccess(),
                            afterWrite = builder.getExpireAfterWrite(),
                            now = System.currentTimeMillis();
                    if ((afterAccess != NESTED && now - vNode.refresh >= afterAccess) ||
                            (afterWrite  != NESTED && now - vNode.start   >= afterWrite))
                    {
                       // vNode.refresh = REMOVAL;//задаю статус удаления
                        vNode.removal = safeRemoval(entry.getKey(), vNode).thenApply(remove -> {
                            if (remove) {
                                //Проверяю, вдруг я уже где-то обновил значение
                                if (vNode.removal != null) {
                                    map.remove(entry.getKey());
                                }
                            } else {
                                vNode.removal = null;
                                vNode.refresh = System.currentTimeMillis();
                            }
                            return remove;
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
        Node<V> vNode = map.get(key);
        if(vNode != null) {
            CompletableFuture<Boolean> removal = vNode.removal;
            if (removal != null) {
                vNode.removal = null;
                if (removal.isDone() && removal.join()) {
                    map.put(key, vNode);
                } else {
                    removal.cancel(true);
                    if (!map.containsKey(key)) {
                        map.put(key, vNode);
                    }
                }
            }
            vNode.refresh = System.currentTimeMillis();
        } else {
            vNode = new Node<>(cacheLoader.asyncLoad(key, builder.getExecutor()));
            map.put(key, vNode);
        }
        return vNode.cf;
    }
    public CompletableFuture<Boolean> remove(K key) {
        Node<V> node = map.get(key);
        CompletableFuture<Boolean> cf;
        if ((cf = node.removal) != null) {
            return cf;
        }
        return node.removal = safeRemoval(key, node).thenApply(b -> {
            if(b) {
                map.remove(key);
            } else {
                node.removal = null;
                node.refresh = System.currentTimeMillis();
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
            CompletableFuture<Boolean> removal;
            if ((removal = node.removal) != null) {
                removal.thenAccept(erase -> {
                    if (erase) {
                        cf.complete(null);
                    }
                });
            } else {
                node.removal = safeRemoval(key, node).thenApply(erase -> {
                    if (erase) {
                        map.remove(key);
                        cf.complete(null);
                    } else {
                        node.removal = null;
                        node.refresh = System.currentTimeMillis();
                    }
                    return erase;
                });
            }
            cfs.add(cf);
        }
        return CompletableFuture.allOf(cfs.toArray(A));
    }
    private static class Node<V> {
        private final CompletableFuture<V> cf;
        private final long start = System.currentTimeMillis();
        private long refresh = start;
        private volatile CompletableFuture<Boolean> removal;//todo: atomic

        public Node(CompletableFuture<V> cf) {
            this.cf = cf;
        }
        @Override
        public String toString() {
            @NonNls String s = "Node{" +
                    "CompletableFuture=" + cf +
                    ", start=" + start +
                    ", refresh=" + refresh;
            if (removal != null) {
                s += ", status=removal";
            }
            s += '}';
            return s;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Node<?> node = (Node<?>) o;

            if (start != node.start) return false;
            if (refresh != node.refresh) return false;
            return cf != null ? cf.equals(node.cf) : node.cf == null;
        }

        @Override
        public int hashCode() {
            int result = cf != null ? cf.hashCode() : 0;
            result = 31 * result + (int) (start ^ (start >>> 32));
            result = 31 * result + (int) (refresh ^ (refresh >>> 32));
            return result;
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
        return "AsyncCache{map=" + map + '}';
    }
}