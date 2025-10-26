package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Arrive {


    private static final String API_URL = "https://carrier.arrivelogistics.com/graphql";

    // ... (AUTH_TOKEN and COOKIE_HEADER remain the same) ...
    private static final String AUTH_TOKEN = "eyJraWQiOiJLa1dfWWd4TGtHX3N4cXVUNFB5SEV3Y2xqd2szRUVNT3pPZnM4T005clVBIiwiYWxnIjoiUlMyNTYifQ.eyJ2ZXIiOjEsImp0aSI6IkFULnlFNTM5RDJsd3VQZW1PMHJvNFB1aXFpRmRual9yRi1XeU5adFBPZHhMTjQiLCJpc3MiOiJodHRwczovL2Fycml2ZWxvZ2lzdGljc2FwcC5va3RhLmNvbS9vYXV0aDIvYXVzcXNzNjN6YXpsaWgxSjQ1ZDYiLCJhdWQiOiJhcnJpdmUtcHJvZCIsImlhdCI6MTc2MTM5Mjk4MiwiZXhwIjoxNzYxNDM2MTgyLCJjaWQiOiIwb2Fxc29yYjFBQm41N1R2ZzVkNiIsInVpZCI6IjAwdW82ZzJwN2NQSkJhTUE4NWQ3Iiwic2NwIjpbInByb2ZpbGUiLCJvcGVuaWQiLCJlbWFpbCJdLCJhdXRoX3RpbWUiOjE3NjEzOTI5NzAsImxhc3ROYW1lIjoiRFAiLCJmaXJzdE5hbWUiOiJEaXNwYXRjaCIsInN1YiI6InNhbGVzQGdvbGR0cmVldHJhbnNpbmMuY29tIiwiYXJyaXZlTm93Um9sZXMiOlsiUl9DQVJSSUVSIl0sImFycml2ZU5vd1VzZXJJZCI6MTQ4NzUwLCJjYXJyaWVySWQiOjI0MTY5OX0.L_LAw_C4r__IdCuCAM_wDys4_vJWvSbtJOwgdppc1Swug0Ewpt3t9aP-GKGDPlC6bt8wX5uyxa8TRqvW1eCLBJcM2qmViwEXnw-SoXKz9ajomYX19pjpYbplcB8DVCC2p3Y4AbBuRWslN7Q53uUpSvRjVvexXuWxdx4TD0dwnVXxCFa92GW1AxNswudid-dBHD5yTAljhLrs3h7F0xxTtwQ-hLpDouv4VuYY9LgxS8rttJOnmEbvjfCuhsNUjh7bsiRqRZ5KtlmA08Sdw1EBE4E7AgS21LH-5Xa0MrKOX5EI-ti__WWYsiuUEYqo1FXNN-JU6buBkl3insIU4vOExg";
    private static final String COOKIE_HEADER = "_mkto_trk=...; _ga=...; cf_clearance=...; _hjSessionUser_1017621=...; _ga_Q7ZCWTXH17=...; _ga_2ECPGGQ9LV=...";

    // 🌟 REVISED JSON_BODY using Java Text Blocks (Java 15+)
    private static final String JSON_BODY = """
        {
          "operationName": "getLoads",
          "variables": {
            "loadFilter": {
              "PickupApptEarliest": null,
              "PickupApptLatest": null,
              "PickupLocation": null,
              "DeliveryApptEarliest": null,
              "DeliveryApptLatest": null,
              "DropoffLocation": null,
              "LoadNumbers": null,
              "MaxStops": null,
              "EquipmentSizes": null,
              "Weight": null,
              "Miles": null,
              "EquipmentTypes": null,
              "PickupRadius": 2,
              "DropoffRadius": 2,
              "Speciality": null,
              "Price": null,
              "RPM": null
            },
            "loadSort": {
              "ColumnToSort": "PickupApptEarliest",
              "SortDirection": "ASC"
            },
            "pagination": {
              "pageNumber": 1,
              "pageSize": 50
            }
          },
          "query": "query getLoads($loadFilter: LoadFilter!, $loadSort: SortBy!, $pagination: PaginationInput!) { getLoads(loadFilter: $loadFilter, loadSort: $loadSort, pagination: $pagination) { data { LoadBoardId LoadStatus CreatedOn ...deliveryLocation EquipmentType EquipmentTypeId Miles NumberOfPickups NumberOfDeliveries ...pickupLocation Weight NumberOfStops IsBookItNow TopSpend IsHighValue IsHighRisk CargoValue CustomerTrackingFrequencyTypeId CustomerRepEmailAddress PickupApptTypeId DeliveryApptTypeId ...specialtyFields __typename } totalRecords __typename } } fragment deliveryLocation on Load { DeliveryLateCity DeliveryLateStateCode DeliveryApptEarliest DeliveryApptLatest DeliveryDeadheadInMiles DeliveryLocationIANACode DeliveryApptStatusConfirmed __typename } fragment pickupLocation on Load { PickupEarlyCity PickupEarlyStateCode PickupApptEarliest PickupApptLatest PickupDeadheadInMiles PickupLocationIANACode PickupApptStatusConfirmed __typename } fragment specialtyFields on Load { IsHazmat IsTeam IsOverSizeLoad IsBlind TwicNeeded DropTrailer TankerEndorsement __typename }"
        }
        """;

    public static void main(String[] args) {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String authorizationHeader = "Bearer " + AUTH_TOKEN;

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "*/*")
                    .header("Accept-Language", "en-GB,en-US;q=0.9,en;q=0.8,uz;q=0.7")
                    .header("Content-Type", "application/json")
                    .header("Authorization", authorizationHeader)
                    .header("Cookie", COOKIE_HEADER)
                    .header("Origin", "https://carrier.arrivelogistics.com")
                    .header("Referer", "https://carrier.arrivelogistics.com/find-loads")
                    .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36")
                    .POST(HttpRequest.BodyPublishers.ofString(JSON_BODY))
                    .build();

            System.out.println("Sending request to: " + API_URL);

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

/*
            System.out.println("--- Response ---");
            System.out.println("Status Code: " + response.statusCode());
             System.out.println("Response Body:\n" + response.body()); // Comment out or remove this to stop printing raw JSON
*/

            // 🌟 NEW: JSON Parsing using Jackson 🌟
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.body());

            // Navigate to the array of load data
            JsonNode loadsArray = rootNode
                    .path("data")
                    .path("getLoads")
                    .path("data");

            if (loadsArray.isArray()) {
//                System.out.println("\n--- Parsed Load Data ---");
                for (JsonNode load : loadsArray) {
                    int loadId = load.path("LoadBoardId").asInt();
//                    String loadStatus = load.path("LoadStatus").asText();
                    int totalMiles = load.path("Miles").asInt();

                    String pickupApptEarliest = load.path("PickupApptEarliest").asText();
                    String pickupApptLatest = load.path("PickupApptLatest").asText();
                    String deliveryApptLatest = load.path("DeliveryApptLatest").asText();
                    String deliveryApptEarliest = load.path("DeliveryApptEarliest").asText();
                    String equipmentType = load.path("EquipmentType").asText();
                    String isHazmat = load.path("IsHazmat").asText();
                    String dropTrailer = load.path("DropTrailer").asText();

                    String fromCity = load.path("PickupEarlyCity").asText();
                    String fromState = load.path("PickupEarlyStateCode").asText();
                    String toCity = load.path("DeliveryLateCity").asText();
                    String toState = load.path("DeliveryLateStateCode").asText();
                    int weight = load.path("Weight").asInt();


/*                    System.out.printf(
                            "Load ID: %s | Status: %s | Dist: %d mi | Equip: %s | Hazmat: %s | Drop: %s | Weight: %d lbs | PickUp: %s (%s) [%s - %s] | DropOff: %s (%s) [%s - %s]\n",
                            loadId, loadStatus, distance, equipmentType, isHazmat, dropTrailer, weight,
                            pickupCity, pickupState, pickupApptEarliest, pickupApptLatest,
                            deliveryCity, deliveryState, deliveryApptEarliest, deliveryApptLatest
                    );*/

                    boolean exists = DatabaseConnection.checkLoadIdExists(loadId);

                    long chatId1 = 1586002925L; //max

                    if (CurlToJava.getStates().contains(fromState) & equipmentType.startsWith("V") & isHazmat.equals("false") & dropTrailer.equals("false") & weight<40000){
                            CurlToJava.enterItToDBandSendToBot(exists, loadId, totalMiles, pickupApptEarliest,deliveryApptLatest, fromCity, fromState, toCity, toState, chatId1);
                    }

                }
            }
            // ------------------------------------

        } catch (Exception e) {
            System.err.println("An error occurred during the HTTP request/parsing:");
            e.printStackTrace();
        }
    }
}