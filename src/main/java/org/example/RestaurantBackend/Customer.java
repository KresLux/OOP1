package org.example.RestaurantBackend;

import java.util.concurrent.atomic.AtomicInteger;

public class Customer {
    private static final AtomicInteger idCounter = new AtomicInteger(1);

    private final int id;
    private final long arrivalTimestamp;

    public Customer() {
        this.id = idCounter.getAndIncrement();
        this.arrivalTimestamp = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String createOrderInfo() {
        return "Pizza";
    }

    public void receiveOrder(Order order) {
        long waitTimeSeconds = (System.currentTimeMillis() - arrivalTimestamp) / 1000;
        System.out.println("[RestaurantUI.Customer #" + id + "] Забрал " + order +
                " (время ожидания: " + waitTimeSeconds + " сек) и направился в обеденную зону.");
    }

    @Override
    public String toString() {
        return "RestaurantUI.Customer #" + id;
    }
}