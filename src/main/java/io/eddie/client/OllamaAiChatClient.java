package io.eddie.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.eddie.util.HttpClient;
import io.eddie.util.PropertiesConfiguration;

import java.util.HashMap;
import java.util.Map;

public class OllamaAiChatClient implements AiChatClient {

    private static final String PATH = "/api/chat";

    @Override
    public HttpClient.Response send(JsonNode body) {

        Map<String, String> headers = new HashMap<>();

        ObjectNode copied = body.deepCopy();
        copied.put("stream", false);

        String url = PropertiesConfiguration.ollamaBaseUrl() + PATH;

        return HttpClient.post(url, headers, copied);
    }

    @Override
    public JsonNode chat(JsonNode body) {
        HttpClient.Response res = this.send(body);
        if (!res.isOk()) {
            throw new RuntimeException("Ollama 호출 실패: " + res.json());
        }
        return res.json();
    }

    @Override
    public String text(JsonNode response) {
        return response.path("message").path("content").asText();
    }

}
