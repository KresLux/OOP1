package org.example.RestaurantBackend;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderTaker {
    private final BlockingQueue<Order> kitchenQueue;
    private final Map<Integer, CompletableFuture<Order>> pendingOrders;
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public OrderTaker(BlockingQueue<Order> kitchenQueue, Map<Integer, CompletableFuture<Order>> pendingOrders) {
        this.kitchenQueue = kitchenQueue;
        this.pendingOrders = pendingOrders;
    }

    public CompletableFuture<Order> processCustomer(Customer customer) {
        String orderInfo = customer.createOrderInfo();
        int orderId = idCounter.getAndIncrement();
        Order order = new Order(orderId, customer, orderInfo);

        CompletableFuture<Order> orderPromise = new CompletableFuture<>();
        pendingOrders.put(orderId, orderPromise);

        try {
            kitchenQueue.put(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return orderPromise;
    }
}