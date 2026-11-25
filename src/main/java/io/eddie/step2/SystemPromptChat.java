package io.eddie.step2;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.eddie.client.AiChatClient;
import io.eddie.client.GoogleGenAiChatClient;
import io.eddie.client.OllamaAiChatClient;
import io.eddie.client.OpenAiChatClient;
import io.eddie.util.PropertiesConfiguration;

import static io.eddie.util.JsonUtil.*;

public class SystemPromptChat {

    private static String SYSTEM_PROMPT = "너는 '그렙은행'의 친절한 상담원이다. "
            + "항상 존댓말로 3문장 이내로 답한다. "
            + "특정 금융상품 가입이나 투자를 권유하지 않는다.";

    public static void main(String[] args) {

        String q = "돈을 모으고 싶은데 뭐부터 하면 돼?";

        System.out.println("시스템 프롬프트 없는 답변 ==");
        switch ( PropertiesConfiguration.provider().toLowerCase() ) {
            case "openai":
                System.out.println("답변 : " + openai(new OpenAiChatClient(), null, q));
                break;
            case "ollama":
                System.out.println("답변 : " + ollama(new OllamaAiChatClient(), null, q));
                break;
            case "gemini":
                System.out.println("답변 : " + gemini(new GoogleGenAiChatClient(), null, q));
                break;
        }

        System.out.println("시스템 프롬프트 없는 답변 ==");
        switch ( PropertiesConfiguration.provider().toLowerCase() ) {
            case "openai":
                System.out.println("답변 : " + openai(new OpenAiChatClient(), SYSTEM_PROMPT, q));
                break;
            case "ollama":
                System.out.println("답변 : " + ollama(new OllamaAiChatClient(), SYSTEM_PROMPT, q));
                break;
            case "gemini":
                System.out.println("답변 : " + gemini(new GoogleGenAiChatClient(), SYSTEM_PROMPT, q));
                break;
        }

    }

    static String openai(AiChatClient client, String systemPrompt, String prompt) {

        ArrayNode messages = arr();

        if (systemPrompt != null) {
            messages.add(obj(
                    "role", "system",
                    "content", systemPrompt)
            );
        }
        messages.add(obj(
                "role", "user",
                "content", prompt)
        );

        ObjectNode body = obj(
                "model", PropertiesConfiguration.openAiModel(),
                "messages", messages
        );

        return client.text(client.chat(body));
    }

    static String ollama(AiChatClient client, String systemPrompt, String prompt) {
        ArrayNode messages = arr();

        if (systemPrompt != null) {
            messages.add(obj(
                    "role", "system",
                    "content", systemPrompt)
            );
        }

        messages.add(
                obj(
                "role", "user",
                    "content", prompt
                )
        );

        ObjectNode body = obj("model", PropertiesConfiguration.ollamaModel(), "messages", messages);

        return client.text(client.chat(body));
    }

    static String gemini(AiChatClient client, String systemPrompt, String prompt) {

        ObjectNode body = obj(
                "contents", arr(
                        obj(
                                "role", "user",
                                "parts", arr(
                                        obj("text", prompt)
                                )
                        )
                )
        );

        if (systemPrompt != null) {
            body.set("systemInstruction", obj("parts", arr(obj("text", systemPrompt))));
        }

        return client.text(client.chat(body));
    }

}
