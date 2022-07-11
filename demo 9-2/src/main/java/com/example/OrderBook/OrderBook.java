package com.example.OrderBook;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

public class OrderBook {
    private TreeMap<BigDecimal, Order> bidTree = new TreeMap<BigDecimal, Order>(Collections.reverseOrder());
    private TreeMap<BigDecimal, Order> askTree = new TreeMap<BigDecimal, Order>(Collections.reverseOrder());

    private String currency;

    public OrderBook(String currency) {
        this.currency = currency;
    }

    public void insertBid(Order bid) {
        this.bidTree.put(bid.getPrice(), bid);
    }

    public void insertAsk(Order ask) {
        this.askTree.put(ask.getPrice(), ask);
    }

    public void processOrders(JsonObject orders, String orderType) {
        JsonArray ordersArray = JsonParser.parseString(orders.get(orderType).toString()).getAsJsonArray();

        for (var element: ordersArray) {
            if (element.isJsonArray()) {
                JsonArray orderArray = JsonParser.parseString(element.toString()).getAsJsonArray();

                BigDecimal price = orderArray.get(0).getAsBigDecimal();
                BigDecimal volume = orderArray.get(1).getAsBigDecimal();
                Double timestamp = orderArray.get(2).getAsDouble();

                if (volume.compareTo(BigDecimal.ZERO) == 0) {
                    return;
                }

                Order order = new Order(price,volume,timestamp);

                if (orderType.equals("a") || orderType.equals("as")) {
                    this.insertAsk(order);
                } else {
                    this.insertBid(order);
                }
            }
        }
    }


    @Override
    public String toString() {
        String bids = this.getOrderString(this.getBids());
        String asks = getOrderString(this.getAsks());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String date = LocalDateTime.now().format(formatter).toString();

        return "<------------------------------------>" + "\n" +
                "asks:" + "\n" +
                asks + "\n" +
                "best bid: " + getBestBid() + "\n" +
                "best ask: " + getBestAsk() + "\n" +
                "bids:" + "\n" +
                bids + "\n" +
                date + "\n" +
                this.currency + "\n" +
                ">-------------------------------------<";
    }

    private Collection<Order> getBids() {
        return this.bidTree.values().stream().toList();
    }

    private Collection<Order> getAsks() {
        return this.askTree.values().stream().toList();
    }


    private String getOrderString(Collection<Order> orders) {
        StringBuilder bidsStringBuilder = new StringBuilder();
        bidsStringBuilder.append("[ ");
        orders.stream().toList()
                .forEach(i -> {
                    bidsStringBuilder.append(i.toString());
                    bidsStringBuilder.append(System.getProperty("line.separator"));
                    bidsStringBuilder.append("  ");
                });

        return bidsStringBuilder.toString().trim() + " ]";
    }

    public String getBestAsk() {
        if (this.askTree.size() > 0) {
            return this.askTree.get(this.askTree.lastKey()).toString();
        } else {
            return "No asks";
        }
    }

    public String getBestBid() {
        if (this.bidTree.size() > 0) {
            return this.bidTree.get(this.bidTree.firstKey()).toString();
        } else {
            return "No bids";
        }
    }
}
