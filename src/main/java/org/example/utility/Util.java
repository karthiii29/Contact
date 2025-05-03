package org.example.utility;

import java.util.Random;

public class Util {
    public static long generateUniqueID() {
        long timeComponent = System.currentTimeMillis();
        long randomComponent = new Random().nextInt(100000);
        return timeComponent * 100000 + randomComponent;
    }
}
