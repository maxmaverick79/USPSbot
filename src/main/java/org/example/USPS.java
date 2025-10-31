package org.example;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

public class USPS {

     static void main(String[] args) {

        MyAwesomeBot.startBot();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                sendRequest();
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.MINUTES);
    }

    public static void sendRequest() throws Exception {
        URL url = new URL("https://demo.swanautomations.store/webhook/7c584a73-0a69-45f4-8bca-c3066e5bec3a");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("accept", "*/*");
        conn.setRequestProperty("content-type", "application/json");

        String jsonInputString = "{\"query\":\"loads\"}";
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonInputString.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line.trim());
                }
            }

            String responseString = response.toString();

//            System.out.println(responseString);

            JSONArray loads = new JSONArray(responseString);

            for (int i = 0; i < loads.length(); i++) {
                JSONObject load = loads.getJSONObject(i);

                String loadIdStr = load.getString("load_id");
                int loadId = Integer.parseInt(loadIdStr);

                int totalMiles = load.getInt("total_miles");

                String pickupDatetime = null;
                if (!load.isNull("pick_up_datetime")) {
                    pickupDatetime = decreaseDatetime(load.getString("pick_up_datetime"));
                } else if (!load.isNull("pickup_start_datetime")) {
                    pickupDatetime = decreaseDatetime(load.getString("pickup_start_datetime"));
                }


                String deliveryDatetime = null;
                if (!load.isNull("delivery_datetime")) {
                    deliveryDatetime = decreaseDatetime(load.getString("delivery_datetime"));
                } else if (!load.isNull("delivery_start_datetime")) {
                    deliveryDatetime = decreaseDatetime(load.getString("delivery_start_datetime"));
                }


                JSONArray stops = load.getJSONArray("stops");

                String fromCity = null, fromState = null, toCity = null, toState = null;

                if (!stops.isEmpty()) {
                    JSONObject fromStop = stops.getJSONObject(0);
                    fromCity = fromStop.getString("city");
                    fromState = fromStop.getString("state");
                }

                if (stops.length() >= 2) {
                    JSONObject toStop = stops.getJSONObject(1);
                    toCity = toStop.getString("city");
                    toState = toStop.getString("state");
                }

                boolean exists = DatabaseConnection.checkLoadIdExists(loadId);
                long chatId1 = 1586002925L;
                long chatId2 = 1287858101L;
                long chatId3 = 7898072414L; // felix

                final List<String> stateList = getStates();

                for (String state : stateList) {
                    if (fromState != null && fromState.equalsIgnoreCase(state)) {
                        enterItToDBandSendToBot(exists, loadId, totalMiles, pickupDatetime, deliveryDatetime, fromCity, fromState, toCity, toState, chatId1);
                        enterItToDBandSendToBot(exists, loadId, totalMiles, pickupDatetime, deliveryDatetime, fromCity, fromState, toCity, toState, chatId2);
                        enterItToDBandSendToBot(exists, loadId, totalMiles, pickupDatetime, deliveryDatetime, fromCity, fromState, toCity, toState, chatId3);
                    }
                }
            }
        }
        conn.disconnect();
    }

    static List<String> getStates() {
        final List<String> stateList = List.of("KS","MO","PA","NJ","MD","CA");
        return stateList;
    }


    static void enterItToDBandSendToBot(boolean exists, int loadId, int totalMiles, String pickup,
                                                String delivery, String fromCity, String fromState,
                                                String toCity, String toState, long chatId) {
        if (!exists) {
            DatabaseConnection.insertLoad(loadId, totalMiles, pickup, delivery, fromCity, fromState, toCity, toState);
            MyAwesomeBot.sendToChat(chatId, loadId, totalMiles, pickup, delivery, fromCity, fromState, toCity, toState);
        }
    }

    public static String decreaseDatetime(String pickupDatetime) {

        Duration durationToSubtract = Duration.ofHours(5).plusMinutes(30);

        try {
            Instant originalInstant = Instant.parse(pickupDatetime);

            Instant newInstant = originalInstant.minus(durationToSubtract);

            return newInstant.toString();

        } catch (DateTimeParseException e) {
            // Handle case where the input string is not a valid date format
            throw new IllegalArgumentException("Invalid datetime format: " + pickupDatetime, e);
        }
    }
}
