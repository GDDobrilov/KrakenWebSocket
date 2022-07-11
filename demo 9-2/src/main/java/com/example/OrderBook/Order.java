package com.example.OrderBook;

import java.math.BigDecimal;

public class Order {
    private BigDecimal price;
    private BigDecimal volume;
    private Double timestamp;

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "[ " + price +
                ", " + volume + " ]";
    }

    public Order(BigDecimal price, BigDecimal volume, Double timestamp) {
        this.price = price;
        this.volume = volume;
        this.timestamp = timestamp;
    }
}
