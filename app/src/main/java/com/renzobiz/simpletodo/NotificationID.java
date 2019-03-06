package com.renzobiz.simpletodo;

import java.util.concurrent.atomic.AtomicInteger;

public class NotificationID {
    private final static AtomicInteger c = new AtomicInteger(0);
    private static final NotificationID ourInstance = new NotificationID();

    public static NotificationID getInstance() {
        return ourInstance;
    }

    public static int getID() {
        return c.incrementAndGet();
    }

    private NotificationID() {
    }
}
