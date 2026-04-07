package com.kesn.dto;

public class AdminStatsDto {
    private long products;
    private long orders;
    private long users;

    public AdminStatsDto(long products, long orders, long users) {
        this.products = products;
        this.orders = orders;
        this.users = users;
    }
    public long getProducts() { return products; }
    public long getOrders() { return orders; }
    public long getUsers() { return users; }
}








