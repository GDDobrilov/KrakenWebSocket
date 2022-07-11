package com.example.OrderBook;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;

@Service
public class Kraken {

    private static final Gson gson = new Gson();
    @Getter
    private WebSocketSession clientSession;

    @PostConstruct
    @SneakyThrows
    public void establishConnection() {
        Map<String, OrderBook> mapOrderBookByCurrency = Map.of("XBT", new OrderBook("BTC/USD"),
                "ETH", new OrderBook("ETH/USD"));

        var webSocketClient = new StandardWebSocketClient();
        this.clientSession = webSocketClient.doHandshake(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                try {
                    if (JsonParser.parseString(message.getPayload()).isJsonArray()) {
                        // get the array that the server returns
                        JsonArray jsonArray = JsonParser.parseString(message.getPayload()).getAsJsonArray();

                        // get the currency that the server is sending info about
                        String currency = Arrays.stream(jsonArray.get(3).toString().split("/")).toList().get(0).replace('"', ' ').trim();
                        var orderBook = mapOrderBookByCurrency.get(currency);

                        if (JsonParser.parseString(jsonArray.get(1).toString()).isJsonObject()) {
                            // get the object that contains the prices
                            String dataObject = jsonArray.get(1).toString();
                            JsonObject firstObject = JsonParser.parseString(dataObject).getAsJsonObject();
                            if (firstObject.get("a") != null) {
                                orderBook.processOrders(firstObject, "a");
                            } else if (firstObject.get("b") != null) {
                                orderBook.processOrders(firstObject, "b");
                            } else if(firstObject.get("as") != null) {
                                orderBook.processOrders(firstObject, "as");
                                if (firstObject.get("bs") != null) {
                                    orderBook.processOrders(firstObject, "bs");
                                }
                            } else if(firstObject.get("bs") != null) {
                                orderBook.processOrders(firstObject, "bs");
                            }
                        }

                        System.out.println(orderBook.toString());
                    }
                } catch (com.google.gson.JsonParseException exx) {
                }
            }
        }, new WebSocketHttpHeaders(), URI.create("wss://ws.kraken.com")).get();
        this.clientSession.sendMessage(new TextMessage("{\n" +
                "  \"event\": \"subscribe\",\n" +
                "  \"pair\": [\n" +
                "   \"BTC/USD\",\n" +
                "   \"ETH/USD\"\n" +
                "  ],\n" +
                "  \"subscription\": {\n" +
                "    \"name\": \"book\"\n" +
                "  }\n" +
                "}"));
    }
}
