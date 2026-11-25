package io.eddie.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.eddie.util.HttpClient;
import io.eddie.util.PropertiesConfiguration;

import java.util.HashMap;
import java.util.Map;

public class GoogleGenAiChatClient implements AiChatClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    @Override
    public HttpClient.Response send(JsonNode body) {
        String url = BASE_URL + PropertiesConfiguration.geminiModel() + ":generateContent";
        Map<String, String> headers = new HashMap<>();
        headers.put("x-goog-api-key", PropertiesConfiguration.geminiKey());
        return HttpClient.post(url, headers, body);

    }

    public JsonNode chat(JsonNode body) {
        HttpClient.Response res = send(body);
        if (!res.isOk()) {
            throw new RuntimeException("Gemini API 오류 " + res.status + ": " + res.body);
        }
        return res.json();
    }

    @Override
    public String text(JsonNode response) {
        // gemini-2.5-flash처럼 thinking을 쓰는 모델에서 생각 요약이 켜져 있으면, parts[0]에 답변 대신 생각 과정이 들어 있을 수 있습니다. 답변이 여러 파트로 나뉘어 오면 뒷부분도 빠집니다.
        StringBuilder sb = new StringBuilder();
        for (JsonNode part : response.path("candidates").path(0).path("content").path("parts")) {
            if (part.has("text") && !part.path("thought").asBoolean()) {
                sb.append(part.get("text").asText());
            }
        }
        return sb.toString();
    }

}
