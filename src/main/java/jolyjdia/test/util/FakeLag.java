package jolyjdia.test.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeLag {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(16);
    private static final List<Packet> linkedList = Collections.synchronizedList(new LinkedList<>());
    private static final AtomicInteger count = new AtomicInteger();

    public static void main(String[] args) {
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (linkedList) {
               // boolean first = true; Packet last = null;
                for (Packet packet : linkedList) {
                 //   if (first) {
                 //       System.out.println(packet);
                 //       first = false;
                  //  }
                  //  last = packet;
                }
              //  System.out.println(last);
                linkedList.clear();
                System.out.println("is clear "+linkedList);
            }
        }, 50, 50, TimeUnit.MILLISECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            linkedList.add(new Packet(count.getAndIncrement()));
        }, 1, 1, TimeUnit.MILLISECONDS);
    }
    public static class Packet {
        private final int anInt;
        public Packet(int anInt) {
            this.anInt = anInt;
        }

        @Override
        public String toString() {
            return String.valueOf(anInt);
        }
    }
}