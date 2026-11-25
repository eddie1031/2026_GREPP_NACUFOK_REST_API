package io.eddie.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.eddie.util.HttpClient;

public interface AiChatClient {

    HttpClient.Response send(JsonNode body);
    JsonNode chat(JsonNode body);
    String text(JsonNode response);

}
