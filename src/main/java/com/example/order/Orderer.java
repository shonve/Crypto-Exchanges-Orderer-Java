package com.example.order;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.example.bybit.Bybit;
import com.example.bybit.BybitJson;
import com.example.exchange.Exchange;
import com.example.server.Server;
import com.example.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonArray;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonArrayBuilder;



public final class Orderer {
    private final Server server;
    private final Map<String, Exchange> exchanges;


    public Orderer(Map<String, Exchange> exchanges, int port) {
        server = new Server(port);
        this.exchanges = exchanges;
    }

    public void run() {
        List<Socket> conns = new ArrayList<>();
        while (true) {
            try {
                Socket newClient = server.acceptConnection(500);
                //System.out.println(newClient);
                if (newClient != null) {
                    conns.add(newClient);
                }
                for(Socket client: conns) {
                    handleData(client);
                }
            }
            catch(Exception e) {
                // ignore
                //System.out.println(e);
            }
        }
    }

    public void handleData(Socket socket) {
        try {
            socket.setSoTimeout(500);
            byte[] header = socket.getInputStream().readNBytes(4);
            //System.out.print(header.length);
            if (header.length == 4) {
                socket.setSoTimeout(500);
                int dataSize = ByteBuffer.wrap(header).getInt();
                //System.out.println(dataSize);
                String data = new String(socket.getInputStream().readNBytes(dataSize));
                //System.out.printf("%s\n", data);
                JsonObject jsonObject = Json.newJsonObject(data);
                String topic = jsonObject.getString("topic");
                //System.out.printf("topic: %s\n", topic);
                if (topic.equals("openOrder")) {
                    String exchange = jsonObject.getString("exchange");
                    String basecoin = jsonObject.getString("basecoin");
                    String quotecoin = jsonObject.getString("quotecoin");
                    String side = jsonObject.getString("side");
                    String price = jsonObject.getString("price");
                    String qty = jsonObject.getString("qty");
                    System.out.printf("[openOrder] exchange: %s, basecoin: %s, quotecoin: %s, side: %s, price: %s, qty: %s\n", exchange, basecoin, quotecoin, side, price, qty);
                    String response = openOrder(exchange, basecoin, quotecoin, side, price, qty);
                    if (response != null) {
                        response = formatResponse(exchange, response);
                        writeData(socket, response);
                    } else {
                        response = Json.newJsonObjectBuilder().add("success", "false").build().toString();
                        writeData(socket, response);
                    }
                    
                } else if (topic.equals("closeOrder")) {
                    String exchange = jsonObject.getString("exchange");
                    String basecoin = jsonObject.getString("basecoin");
                    String quotecoin = jsonObject.getString("quotecoin");
                    String orderId = jsonObject.getString("orderId");
                    System.out.printf("[closeOrder] exchange: %s, basecoin: %s, quotecoin: %s, orderId: %s\n", exchange, basecoin, quotecoin, orderId);
                    String response = closeOrder(exchange, basecoin, quotecoin, orderId);
                    if (response != null) {
                        response = formatResponse(exchange, response);
                        System.out.println(response);
                        writeData(socket, response);
                    } else {
                        response = Json.newJsonObjectBuilder().add("success", "false").build().toString();
                        writeData(socket, response);
                    }
                } else if (topic.equals("orderUpdates")) {
                    String exchange = jsonObject.getString("exchange");
                    String basecoin = jsonObject.getString("basecoin");
                    String quotecoin = jsonObject.getString("quotecoin");
                    JsonArray orderIds = jsonObject.getJsonArray("orderIds");
                    JsonArrayBuilder result = Json.newJsonArrayBuilder();
                    for (int i = 0; i < orderIds.size(); i ++) {
                        String orderId = orderIds.getString(i);
                        String response = getOrder(exchange, basecoin, quotecoin, orderId);
                        if (response != null) {
                            result.add(Json.newJsonObject(formatResponse(exchange, response)));
                        }
                    }
                    JsonArray resultBuild = result.build();
                    if (resultBuild.size() > 0) {
                        String response = Json.newJsonObjectBuilder().add("success", true).add("data", resultBuild).build().toString();
                        writeData(socket, response);
                    } else {
                        String response = Json.newJsonObjectBuilder().add("success", false).build().toString();
                        writeData(socket, response);
                    }
                }
            }
        }
        catch(Exception e)  {
            //System.out.print(e);
            // ignore
        }
    }

    private String openOrder(String exchange, String basecoin, String quotecoin, String side, String price, String qty) {
        if (!exchanges.containsKey(exchange)) {
            return null;
        }
        try {
            Exchange exchangeObj = exchanges.get(exchange);
            String response = exchangeObj.openOrder(basecoin, quotecoin, side, price, qty);
            if (response != null) {
                System.out.println(response);
                return response;
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
        return null;
        
    }

    public String closeOrder(String exchange, String basecoin, String quotecoin, String orderId) {
        if (!exchanges.containsKey(exchange)) {
            return null;
        }
        try {
            Exchange exchangeObj = exchanges.get(exchange);
            String response = exchangeObj.closeOrder(basecoin, quotecoin, orderId);
            if (response != null) {
                System.out.println(response);
                return response;
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
        return null;
        
    }

    public String getOrder(String exchange, String basecoin, String quotecoin, String orderId) {
        if (!exchanges.containsKey(exchange)) {
            return null;
        }
        try {
            Exchange exchangeObj = exchanges.get(exchange);
            String response = exchangeObj.getOrder(basecoin, quotecoin, orderId);
            if (response != null) {
                System.out.println(response);
                return response;
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }
        return null;
    }

    public String formatResponse(String exchange, String response) {
        if (exchange.equals("gateio")) {
            JsonObjectBuilder builder = Json.newJsonObjectBuilder();
            JsonObject responseJson = Json.newJsonObject(response);
            String status = responseJson.getString("status");
            if (status.equals("open")) {
                status = "unfilled";
            } else if (status.equals("closed")) {
                status = responseJson.getString("finish_as");
            }
            return builder.add("success", "true")
                   .add("id", responseJson.getString("id"))
                   .add("side", responseJson.getString("side"))
                   .add("status", status)
                   .add("amount", responseJson.getString("amount"))
                   .add("price", responseJson.getString("price"))
                   .add("left", responseJson.getString("left"))
                   .add("filled_total", responseJson.getString("filled_total"))
                   .add("fee", responseJson.getString("fee")).build().toString();
        } else if (exchange.equals("bybit")) {
            JsonObjectBuilder builder = Json.newJsonObjectBuilder();
            JsonObject responseJson = Json.newJsonObject(response);
            return builder.add("success", "true")
                   .add("id", responseJson.getString("orderId"))
                   .add("side", responseJson.getString("side").toLowerCase())
                   .add("status", responseJson.getString("orderStatus").toLowerCase())
                   .add("amount", responseJson.getString("qty"))
                   .add("price", responseJson.getString("price"))
                   .add("left", responseJson.getString("leavesQty"))
                   .add("value_left", responseJson.getString("leavesValue"))
                   .add("filled_total", responseJson.getString("cumExecValue"))
                   .add("fee", responseJson.getString("cumExecFee")).build().toString();
        } 
        return null;
    }

    public void writeData(Socket socket, String data) {
        try {
            byte[] header = ByteBuffer.allocate(4).putInt(data.length()).array();
            socket.getOutputStream().write(header);
            socket.getOutputStream().write(data.getBytes());
            // DataOutputStream to write data types directly
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

}