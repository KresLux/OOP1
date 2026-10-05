package org.example.RestaurantBackend;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

public class CashRegister implements Runnable {
    private final int id;
    private final BlockingQueue<Customer> queue;
    private final OrderTaker orderTaker;
    private final int processingTimeSec;

    private volatile boolean running = true;
    private volatile Customer currentCustomer = null;

    public CashRegister(int id, OrderTaker orderTaker, int processingTimeSec) {
        this.id = id;
        this.orderTaker = orderTaker;
        this.processingTimeSec = processingTimeSec;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void addCustomer(Customer customer) {
        queue.offer(customer);
    }

    public int getQueueSize() {
        return queue.size() + (currentCustomer != null ? 1 : 0);
    }

    public BlockingQueue<Customer> getQueue() {
        return queue;
    }

    public Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public int getId() {
        return id;
    }

    @Override
    public void run() {
        try {
            while (running) {
                // Ждем покупателя в очереди кассы
                currentCustomer = queue.take();

                // Имитация времени обслуживания на кассе
                Thread.sleep(processingTimeSec * 1000L);

                // Оформляем заказ и передаем на кухню
                CompletableFuture<Order> promise = orderTaker.processCustomer(currentCustomer);

                // Чтобы покупатель дождался заказа
                Customer tempCustomer = currentCustomer;
                promise.thenAccept(tempCustomer::receiveOrder);

                currentCustomer = null;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        running = false;
    }
}