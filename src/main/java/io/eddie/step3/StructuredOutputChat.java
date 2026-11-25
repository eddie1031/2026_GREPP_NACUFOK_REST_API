package io.eddie.step3;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.eddie.client.AiChatClient;
import io.eddie.client.GoogleGenAiChatClient;
import io.eddie.client.OllamaAiChatClient;
import io.eddie.client.OpenAiChatClient;
import io.eddie.util.PropertiesConfiguration;

import static io.eddie.util.JsonUtil.*;

/**
 * 관련 문서 수조: https://developers.openai.com/ko-KR/api/docs/guides/structured-outputs?api-mode=responses#how-to-use
 */
public class StructuredOutputChat {

    public static void main(String[] args) {

        String q = "어제 점심에 김밥천국에서 8,500원 카드로 결제했어.";

        switch ( PropertiesConfiguration.provider().toLowerCase() ) {
            case "openai":
                System.out.println("답변 : " + openai(q));
                break;
            case "ollama":
                System.out.println("답변 : " + ollama(q));
                break;
            case "gemini":
                System.out.println("답변 : " + gemini(q));
                break;
        }

    }

    static String openai(String text) {

        AiChatClient client = new OpenAiChatClient();

        ObjectNode schema = obj(
                "type", "object",
                "properties", obj(
                        "merchant", obj("type", "string"),
                        "amount", obj("type", "integer"),
                        "category", obj("type", "string", "enum", arr("식비", "교통", "쇼핑", "기타"))
                ),
                "required", arr("merchant", "amount", "category"),
                "additionalProperties", false
        );

        ObjectNode body = obj(
                "model", PropertiesConfiguration.openAiModel(),
                "messages", arr(
                        obj("role", "system", "content", "문장에서 결제 정보를 추출해."),
                        obj("role", "user", "content", text)
                ),
                "response_format", obj(
                        "type", "json_schema",
                        "json_schema", obj("name", "transaction", "strict", true, "schema", schema)
                )
        );
        return client.text(client.chat(body));
    }

    static String gemini(String text) {

        AiChatClient client = new GoogleGenAiChatClient();

        ObjectNode schema = obj(
                "type", "OBJECT",
                "properties", obj(
                        "merchant", obj("type", "STRING"),
                        "amount", obj("type", "INTEGER"),
                        "category", obj("type", "STRING", "enum", arr("식비", "교통", "쇼핑", "기타"))
                ),
                "required", arr("merchant", "amount", "category")
        );

        ObjectNode body = obj(
                "systemInstruction", obj("parts", arr(obj("text", "문장에서 결제 정보를 추출해."))),
                "contents", arr(obj("role", "user", "parts", arr(obj("text", text)))),
                "generationConfig", obj(
                        "responseMimeType", "application/json",
                        "responseSchema", schema
                )
        );
        return client.text(client.chat(body));
    }

    /**
     * ollama의 모델을 활용해서 구조화된 출력
     *
     * - 관련 문서: https://docs.ollama.com/capabilities/structured-outputs
     */
    static String ollama(String text) {

        AiChatClient client = new OllamaAiChatClient();

        ObjectNode schema = obj(
                "type", "object",
                "properties", obj(
                        "merchant", obj("type", "string"),
                        "amount", obj("type", "integer"),
                        "category", obj("type", "string", "enum", arr("식비", "교통", "쇼핑", "기타"))
                ),
                "required", arr("merchant", "amount", "category")
        );

        ObjectNode body = obj(
                "model", PropertiesConfiguration.ollamaModel(),
                "messages", arr(
                        obj("role", "system", "content", "문장에서 결제 정보를 추출해. 반드시 JSON 으로만 답해."),
                        obj("role", "user", "content", text)
                ),
                "format", schema,
                "options", obj("temperature", 0)
        );

        return client.text(client.chat(body));
    }

}
