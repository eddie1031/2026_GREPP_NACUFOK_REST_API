package io.eddie.client;

import com.fasterxml.jackson.databind.JsonNode;
import io.eddie.util.HttpClient;
import io.eddie.util.PropertiesConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * 문서 주소
 * - OpenAI Docs: https://developers.openai.com/ko-KR
 * - OpenAI Request/Response Docs: https://developers.openai.com/ko-KR/api/docs/guides/text
 */
public class OpenAiChatClient implements AiChatClient{

    private static final String URL = "https://api.openai.com/v1/chat/completions";

    @Override
    public HttpClient.Response send(JsonNode body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + PropertiesConfiguration.openAiKey());
        return HttpClient.post(URL, headers, body);
    }

    @Override
    public JsonNode chat(JsonNode body) {
        HttpClient.Response res = this.send(body);
        if (!res.isOk()) {
            throw new RuntimeException(res.json().toPrettyString());
        }
        return res.json();
    }

    @Override
    public String text(JsonNode response) {
        return response.path("choices").path(0).path("message").path("content").asText();
    }

}
