package jolyjdia.test;

import jolyjdia.test.util.cache.CacheBuilder;
import jolyjdia.test.util.cache.ConcurrentCache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class Example {

    private Example() {
    }

    private static final CompletableFuture<String> cf = new CompletableFuture<>();
    private static final float LOAD_FACTOR = 1.15F;
    private static boolean online = true;
    public static void main(String[] args) throws InterruptedException, IllegalAccessException, NoSuchFieldException {

        ConcurrentCache<String, String> cache = new CacheBuilder<String, String>()
                .expireAfterAccess(10, TimeUnit.SECONDS)
                .removal(new CacheBuilder.RemovalListener<String, String>() {
                    @Override
                    public CompletableFuture<Boolean> onRemoval(String key, CompletableFuture<String> cf) {
                        return cf.thenApply(e -> {
                            if(online) {
                                return false;
                            }
                            System.out.println("ДОЛЖЕН БЫТЬ ПУСТЫМ "+Thread.currentThread().getName());
                            return true;
                        });
                    }
                })
                .executor(Executors.newCachedThreadPool())
                .build(new CacheBuilder.AsyncCacheLoader<String, String>() {
                    @Override
                    public CompletableFuture<String> asyncLoad(String key, Executor executor) {
                       // CompletableFuture<String> cf = new CompletableFuture<>();
                       // cf.complete((key+" 122"));
                        return cf;
                    }
                });
        cache.get("21");
        //cache.get("28");
        //cache.get("241");
        cache.removeAll().thenAccept(e -> {
           System.out.println("VSe");
        });
        int i = 0;
        for (;;) {
            Thread.sleep(100);
            if( i == 50) {
                System.out.println("complete");
                cf.complete( " 122");
            } else if( i == 70) {
                System.out.println("offline");
                online = false;
            }
            if (i % 8 == 0) {
                System.out.println(cache);
            }
            ++i;
        }
    }
}