package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.example.USPS.enterItToDBandSendToBot;
import static org.example.USPS.getStates;

public class GurmanApiRequest {

    private static final String API_URL =
            "https://api.gurmanlogistics.com/api/loads/paginated?page_size=10&page=1&statuses=active";
    private static final String BEARER_TOKEN =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6InNhbGVzQGdvbGR0cmVldHJhbnNpbmMuY29tIiwiZXhwIjoxNzYyNTQ4MDEwLCJpYXQiOjE3NjI0NjE2MTAsInVzZXJfaWQiOiJjMzk5OGZlZC0zNDgwLTQ1MjUtODc0ZS1hNjhlOTBlMzI4MDYiLCJ1c2VyX3R5cGUiOiJ1c2VyIn0.K3W_YUPwsz-aDUO3XjLfpfuN07Ve0LZ3I1qtVv4ft20";


    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        Runnable task = () -> {
            System.out.println("\n⏱ Running Gurman API Request at " + LocalDateTime.now());
            fetchAndProcessLoads();
        };

        // run immediately, then every 10 seconds
        scheduler.scheduleAtFixedRate(task, 0, 10, TimeUnit.SECONDS);
    }

    private static void fetchAndProcessLoads() {
        try {
            // Step 1: Make HTTP request
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "application/json, text/plain, */*");
            conn.setRequestProperty("authorization", "Bearer " + BEARER_TOKEN);
            conn.setRequestProperty("user-agent", "Mozilla/5.0");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Request failed. HTTP code: " + responseCode);
                conn.disconnect();
                return;
            }

            // Step 2: Read JSON response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();

            // Step 3: Parse and process
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject item = dataArray.getJSONObject(i);

                int loadId = Integer.parseInt(item.optString("external_load_id"));
                double totalMiles = safeParseDouble(item.optString("total_distance"));
                String pickupRaw = item.optString("scheduled_pickup_date_time");
                String deliveryRaw = item.optString("scheduled_delivery_date_time");
                String pickupIso = toIsoInstantString(pickupRaw);
                String deliveryIso = toIsoInstantString(deliveryRaw);

                String fromCity = item.optString("origin_city_name");
                String fromState = item.optString("origin_state_code");
                String toCity = item.optString("destination_city_name");
                String toState = item.optString("destination_state_code");

                boolean exists = DatabaseConnection.checkLoadIdExists(loadId);

                long chatId1 = 1586002925L;
                long chatId2 = 1287858101L;
                long chatId3 = 7898072414L; // felix

                List<String> stateList = getStates();

                for (String state : stateList) {
                    if (fromState != null && fromState.equalsIgnoreCase(state)) {
                        int roundedMiles = (int) Math.round(totalMiles);
                        enterItToDBandSendToBot(exists, loadId, roundedMiles, pickupIso, deliveryIso,
                                fromCity, fromState, toCity, toState, chatId1);
                        enterItToDBandSendToBot(exists, loadId, roundedMiles, pickupIso, deliveryIso,
                                fromCity, fromState, toCity, toState, chatId2);
                        enterItToDBandSendToBot(exists, loadId, roundedMiles, pickupIso, deliveryIso,
                                fromCity, fromState, toCity, toState, chatId3);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error while processing loads: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ Safely parse double (handles commas and nulls)
    public static double safeParseDouble(String value) {
        try {
            if (value == null || value.isEmpty()) return 0.0;
            return Double.parseDouble(value.replace(",", ""));
        } catch (NumberFormatException e) {
            System.err.println("⚠️ Invalid number format: " + value);
            return 0.0;
        }
    }

    // ✅ Convert "11/5/2025, 10:00 AM" → "2025-11-05T10:00:00Z"
    public static String toIsoInstantString(String inputDateTime) {
        if (inputDateTime == null || inputDateTime.isEmpty()) {
            return null;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy, h:mm a", Locale.ENGLISH);
            LocalDateTime localDateTime = LocalDateTime.parse(inputDateTime, formatter);
            ZonedDateTime zoned = localDateTime.atZone(ZoneId.of("UTC"));
            return zoned.toInstant().toString();
        } catch (Exception e1) {
            try {
                // fallback for format like "11/5/25, 10:00 AM" or missing time
                DateTimeFormatter altFormatter = DateTimeFormatter.ofPattern("M/d/yy, h:mm a", Locale.ENGLISH);
                LocalDateTime localDateTime = LocalDateTime.parse(inputDateTime, altFormatter);
                ZonedDateTime zoned = localDateTime.atZone(ZoneId.of("UTC"));
                return zoned.toInstant().toString();
            } catch (Exception e2) {
                System.err.println("⚠️ Failed to parse date: " + inputDateTime + " → " + e2.getMessage());
                return null;
            }
        }
    }
}
