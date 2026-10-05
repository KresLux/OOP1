package org.example.RestaurantBackend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Restaurant {
    private final BlockingQueue<Order> kitchenQueue;
    private final BlockingQueue<Order> serviceQueue;
    private final Map<Integer, CompletableFuture<Order>> pendingOrders;

    private final OrderTaker orderTaker;
    private List<Cook> cooks;
    private Server server;
    private List<CashRegister> cashRegisters;

    private ExecutorService workersPool;
    private ExecutorService customerExecutor;
    private volatile boolean isRunning = false;
    private final AtomicInteger waitingCustomersToOrder = new AtomicInteger(0);

    public Restaurant() {
        this.kitchenQueue = new LinkedBlockingQueue<>();
        this.serviceQueue = new LinkedBlockingQueue<>();
        this.pendingOrders = new ConcurrentHashMap<>();
        this.orderTaker = new OrderTaker(kitchenQueue, pendingOrders);
    }

    public void startSimulation(int customerArrivalIntervalSec, int cookingTimeSec, int registerTimeSec, int numRegisters, int numCooks) {
        this.isRunning = true;

        this.cooks = new ArrayList<>();
        this.server = new Server(serviceQueue, pendingOrders);
        this.cashRegisters = new ArrayList<>();

        this.workersPool = Executors.newCachedThreadPool();

        for (int i = 0; i < numCooks; i++) {
            Cook cook = new Cook(i + 1, kitchenQueue, serviceQueue, cookingTimeSec);
            this.cooks.add(cook);
            this.workersPool.submit(cook);
        }

        this.workersPool.submit(server);

        for (int i = 0; i < numRegisters; i++) {
            CashRegister register = new CashRegister(i + 1, orderTaker, registerTimeSec);
            this.cashRegisters.add(register);
            this.workersPool.submit(register);
        }

        this.customerExecutor = Executors.newSingleThreadExecutor();
        this.customerExecutor.submit(() -> {
            try {
                while (isRunning) {
                    waitingCustomersToOrder.incrementAndGet();
                    Customer customer = new Customer();
                    waitingCustomersToOrder.decrementAndGet();

                    CashRegister bestRegister = cashRegisters.get(0);
                    for (CashRegister cr : cashRegisters) {
                        if (cr.getQueueSize() < bestRegister.getQueueSize()) {
                            bestRegister = cr;
                        }
                    }
                    bestRegister.addCustomer(customer);

                    Thread.sleep(customerArrivalIntervalSec * 1000L);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void stopSimulation() {
        this.isRunning = false;
        if (cooks != null) {
            for (Cook cook : cooks) {
                cook.stop();
            }
            cooks.clear();
        }
        if (server != null) server.stop();
        if (cashRegisters != null) {
            for (CashRegister cr : cashRegisters) {
                cr.stop();
                cr.getQueue().clear();
            }
            cashRegisters.clear();
        }
        if (customerExecutor != null) customerExecutor.shutdownNow();
        if (workersPool != null) workersPool.shutdownNow();

        kitchenQueue.clear();
        serviceQueue.clear();
        pendingOrders.clear();
        waitingCustomersToOrder.set(0);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public List<Cook> getCooks() {
        return cooks;
    }

    public Server getServer() {
        return server;
    }

    public BlockingQueue<Order> getKitchenQueue() {
        return kitchenQueue;
    }

    public BlockingQueue<Order> getServiceQueue() {
        return serviceQueue;
    }

    public List<CashRegister> getCashRegisters() {
        return cashRegisters;
    }
}