package com.leonchyk.steam;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SteamApi implements Serializable {
    private static final long serialVersionUID = -1427494597678207263L;

    // API request from https://developer.valvesoftware.com/wiki/Steam_Web_API
    private static final String GET_INFO = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=%s&steamids=%s";

    // Your API key from https://steamcommunity.com/dev/apikey
    public static final String API_KEY="YOUR_API_KEY";

    private static String getGameExtraInfo(String steamId) {
        String request = String.format(GET_INFO, API_KEY, steamId);
        HttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(request);
        HttpResponse httpResponse;
        String result = null;

        try {
            httpResponse = client.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            String json = EntityUtils.toString(httpEntity);
            ObjectNode node = new ObjectMapper().readValue(json, ObjectNode.class);
            List<JsonNode> jsonNodeList = node.findValues("gameextrainfo");
            if (!jsonNodeList.isEmpty()) {
                result = jsonNodeList.get(0).textValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // check status changing every 20 seconds
    public static void startGamingActivity(String steamId) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        AtomicReference<String> cache = new AtomicReference<String>();
        cache.set("inactive");
        Runnable activity = () -> {
            String apiStatus = getGameExtraInfo(steamId);
            if (apiStatus != null && !cache.get().equals(apiStatus)) {
                cache.set(apiStatus);
                System.out.println(apiStatus);
            }
        };
        executor.scheduleAtFixedRate(activity, 0, 20, TimeUnit.SECONDS);
    }
}
