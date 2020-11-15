package jolyjdia.test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class Cities {
    private Map<Character, List<String>> map;
    private char lastLetter;
    private static final int SKIP = 1 << 'ь' | 1 << 'ъ' | 1 << 'ы';

    private String randCity() {
        List<String> cities = map.get(lastLetter);
        String city = cities.get(ThreadLocalRandom.current().nextInt(cities.size()));
        for(int i = city.length()-1; i > 0; --i) {
            char c = city.charAt(i);
            if(!Character.isLetter(c) || (SKIP >> c & 1) != 0) {
                continue;
            }
            lastLetter = Character.toUpperCase(c);
            cities.remove(city);
            return city;
        }
        return randCity();
    }
}
