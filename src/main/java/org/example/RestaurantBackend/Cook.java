package org.example.RestaurantBackend;

import java.util.concurrent.BlockingQueue;

public class Cook implements Runnable {
    private final int id;
    private final BlockingQueue<Order> kitchenQueue;
    private final BlockingQueue<Order> serviceQueue;
    private volatile Order currentCookingOrder;
    private final int cookingIntervalSeconds;
    private volatile boolean running = true;

    public Cook(int id, BlockingQueue<Order> kitchenQueue, BlockingQueue<Order> serviceQueue, int cookingIntervalSeconds) {
        this.id = id;
        this.kitchenQueue = kitchenQueue;
        this.serviceQueue = serviceQueue;
        this.cookingIntervalSeconds = cookingIntervalSeconds;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Order order = kitchenQueue.take();
                currentCookingOrder = order;
                Thread.sleep(cookingIntervalSeconds * 1000L);
                serviceQueue.put(order);
                currentCookingOrder = null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        running = false;
    }

    public int getId() {
        return id;
    }

    public Order getCurrentCookingOrder() {
        return this.currentCookingOrder;
    }
}