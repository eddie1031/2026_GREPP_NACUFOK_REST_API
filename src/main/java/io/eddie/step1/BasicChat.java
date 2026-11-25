package io.eddie.step1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.eddie.client.AiChatClient;
import io.eddie.client.OllamaAiChatClient;
import io.eddie.client.OpenAiChatClient;
import io.eddie.util.PropertiesConfiguration;

import static io.eddie.util.JsonUtil.*;

public class BasicChat {

    public static void main(String[] args) {

//        String q = "예금과 적금의 차이를 한 문장으로 알려줘.";
        String q = "Hello World!";

        System.out.println(
                "공급자 : " + PropertiesConfiguration.model() +
                " / " +
                "모델 : " + PropertiesConfiguration.model()
        );

        System.out.println("질문 프롬프트: " + q);

        switch ( PropertiesConfiguration.provider().toLowerCase() ) {
            case "openai":
                System.out.println("답변 : " + openai(q));
                break;
            case "ollama":
                System.out.println("답변 : " + ollama(q));
                break;
            case "gemini":
                break;
        }

    }


    static String openai(String prompt) {

        OpenAiChatClient client = new OpenAiChatClient();

        // {"model": "", "messages": [{"role": "user", "content": "질문"}]}
        ObjectNode body = obj(
                "model", PropertiesConfiguration.openAiModel(),
                "messages", arr(
                        obj(
                                "role", "user",
                                "content", prompt
                        )
                )
        );

        JsonNode response = client.chat(body);

//        System.out.println("response = " + response);

        return response.path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText();

        // return client.text(response);

    }

    static String gemini(String prompt) {
        throw new UnsupportedOperationException("제미나이를 이용한 답변 받아오는 코드");
    }

    /**
     * Ollama 모델과 통신하는 메서드
     *
     * 참고문서: https://docs.ollama.com/api/introduction
     */
    static String ollama(String prompt) {

        OllamaAiChatClient client = new OllamaAiChatClient();

        ObjectNode body = obj(
                "model", PropertiesConfiguration.ollamaModel(),
                "messages", arr(
                        obj("role", "user", "content", prompt)
                )
        );
        body.put("stream", false);

        JsonNode response = client.chat(body);

//        System.out.println("response = " + response);

        return response.path("message").path("content").asText();

    }

}
