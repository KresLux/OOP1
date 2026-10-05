package org.example.RestaurantBackend;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

public class Server implements Runnable {
    private final BlockingQueue<Order> serviceQueue;
    private final Map<Integer, CompletableFuture<Order>> pendingOrders;
    private volatile boolean running = true;
    private volatile Order currentPickupOrder;

    public Server(BlockingQueue<Order> serviceQueue, Map<Integer, CompletableFuture<Order>> pendingOrders) {
        this.serviceQueue = serviceQueue;
        this.pendingOrders = pendingOrders;
    }

    @Override
    public void run() {
        try {
            while (running) {
                Order readyOrder = serviceQueue.take();
                this.currentPickupOrder = readyOrder;

                CompletableFuture<Order> promise = pendingOrders.remove(readyOrder.getId());

                if (promise != null) {
                    Thread.sleep(1000);
                    promise.complete(readyOrder);
                }
                this.currentPickupOrder = null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        running = false;
    }

    public Order getCurrentPickupOrder() {
        return currentPickupOrder;
    }
}