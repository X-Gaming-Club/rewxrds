package org.xgaming.rewxrds.External;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.xgaming.rewxrds.Rewxrds;

import java.util.UUID;
import java.util.logging.Level;

public class Server {
    private static Server instance;

    private Server() {
        // Initialize Unirest here
    }

    public static synchronized Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public static String serverRequest(String message, String url,boolean printReq, boolean printRes) {
        try {
            if(printReq) {
                Rewxrds.getPlugin().getLogger().info("Sent : " + message);
            }
            // Attempt to send the request
            HttpResponse<JsonNode> response = Unirest.post(url)
                    .header("Content-Type", "application/json")
                    .body(message).asJson();

            // Check the response status
            if (response.getStatus() == 200) {
                String reply = response.getBody().toString();
                if(printRes) {
                    Rewxrds.getPlugin().getLogger().info("Received :" + reply);
                }
                return reply;
            } else {
                // Log non-200 responses
                Bukkit.getLogger().log(Level.SEVERE, "Non-200 Response: " + response.getStatus());
                return "{}";
            }

        }  catch (UnirestException e) {
            if (e.getCause() instanceof java.net.SocketTimeoutException){
                Bukkit.getLogger().log(Level.SEVERE, "Socket timeout: " );
            }
            else {
                Bukkit.getLogger().log(Level.SEVERE, "HTTP request failed: " + e.getMessage());
            }
            return "{}";
        }
    }
}
