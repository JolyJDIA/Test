package jolyjdia.test;

import jolyjdia.test.util.cache.CacheBuilder;
import jolyjdia.test.util.cache.ConcurrentCache;
import jolyjdia.test.util.cache.ConcurrentCache0;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class Example {

    private Example() {
    }

    private static final CompletableFuture<String> cf = new CompletableFuture<>();
    private static boolean online = true;
    public static void main(String[] args) throws InterruptedException, IllegalAccessException, NoSuchFieldException {
        ConcurrentCache<String, String> cache = new CacheBuilder<String, String>()
                .expireAfterAccess(100, TimeUnit.SECONDS)
                .removal((key, cf) -> cf.thenApply(e -> {
                    //System.out.println("ДОЛЖЕН БЫТЬ ПУСТЫМ " + Thread.currentThread().getName());
                    return true;
                }))
                .executor(Executors.newCachedThreadPool())
                .build((key, executor) -> {
                    CompletableFuture<String> cf = new CompletableFuture<>();
                    cf.complete((key + " 122"));
                    return cf;
                });
        new Thread(() -> {
            for(;;) {
                System.out.println(cache.get("s"));
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
            }
        }).start();
        for (;;) {
            cache.remove("s");
            Thread.sleep(10);
        }
    }
}