package org.example.RestaurantBackend;

public class Order {
    private final int id;
    private final Customer customer;
    private final String orderInfo;

    public Order(int id, Customer customer, String orderInfo) {
        this.id = id;
        this.customer = customer;
        this.orderInfo = orderInfo;
    }

    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }
    @Override
    public String toString() {
        return "RestaurantUI.Order №" + id;
    }
}