package io.eddie.step4;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.eddie.client.AiChatClient;
import io.eddie.client.GoogleGenAiChatClient;
import io.eddie.client.OpenAiChatClient;
import io.eddie.util.JsonUtil;
import io.eddie.util.PropertiesConfiguration;

import static io.eddie.util.JsonUtil.*;

/**
 * 흐름:
 *   1. 질문 + "쓸 수 있는 도구 목록"을 AI 에게 보낸다
 *   2. AI 가 "이 도구를 이 인자로 실행해줘" 라고 응답한다 (AI 가 직접 실행하지 않음)
 *   3. 우리 코드가 도구를 실행하고 결과를 대화에 붙여 다시 보낸다
 *   4. AI 가 결과를 보고 최종 답변을 만든다 (도구가 더 필요하면 2)로 반복)
 *
 * - 관련 문서: https://developers.openai.com/api/docs/guides/function-calling?api-mode=responses
 */
public class ToolCallingChat {

    private static final int MAX_COUNTS = 5;

    public static void main(String[] args) {

        String q = "내 계좌 123-456 잔액을 달러로 바꾸면 얼마야?";

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

    static String openai(String question) {

        AiChatClient client = new OpenAiChatClient();

        ArrayNode tools = BankTools.getTools();

        ArrayNode messages = arr(obj("role", "user", "content", question));

        for (int round = 1; round <= MAX_COUNTS; round++) {
            JsonNode response = client.chat(obj(
                    "model", PropertiesConfiguration.openAiModel(),
                    "messages", messages,
                    "tools", tools
            ));

            JsonNode message = response.path("choices").path(0).path("message");
            JsonNode toolCalls = message.path("tool_calls");

            // 도구 호출 요청이 없으면 = 최종 답변
            if (toolCalls.isEmpty()) {
                return message.path("content").asText();
            }

            messages.add(obj("role", "assistant", "tool_calls", toolCalls));

            for (JsonNode call : toolCalls) {
                String name = call.path("function").path("name").asText();
                JsonNode arguments = JsonUtil.parse(call.path("function").path("arguments").asText());

                ObjectNode result = BankTools.execute(name, arguments);
                System.out.println("도구 실행 --" + name + arguments + " → " + result);

                messages.add(obj(
                        "role", "tool",
                        "tool_call_id", call.path("id").asText(),
                        "content", JsonUtil.toJson(result)
                ));
            }
        }
        throw new IllegalStateException("도구 호출이 " + MAX_COUNTS + "번 넘게 반복되었습니다.");
    }

    static String gemini(String question) {

        AiChatClient client = new GoogleGenAiChatClient();

        ArrayNode tools = BankTools.getTools();

        ArrayNode contents = arr(obj("role", "user", "parts", arr(obj("text", question))));

        for (int round = 1; round <= MAX_COUNTS; round++) {
            JsonNode response = client.chat(obj("contents", contents, "tools", tools));
            JsonNode content = response.path("candidates").path(0).path("content");

            ArrayNode functionResponses = arr();
            for (JsonNode part : content.path("parts")) {
                if (!part.has("functionCall")) continue;

                JsonNode call = part.get("functionCall");
                String name = call.path("name").asText();
                JsonNode arguments = call.path("args");

                ObjectNode result = BankTools.execute(name, arguments);
                System.out.println("도구 실행 --" + name + arguments + " → " + result);

                ObjectNode functionResponse = obj("name", name, "response", result);
                if (call.has("id")) functionResponse.set("id", call.get("id"));
                functionResponses.add(obj("functionResponse", functionResponse));
            }

            if (functionResponses.isEmpty()) {
                return client.text(response);
            }

            contents.add(content);
            contents.add(obj("role", "user", "parts", functionResponses));
        }
        throw new IllegalStateException("도구 호출이 " + MAX_COUNTS + "번 넘게 반복되었습니다.");
    }

    /**
     * - 관련문서: https://docs.ollama.com/capabilities/tool-calling
     */
    static String ollama(String question) {

        AiChatClient client = new GoogleGenAiChatClient();

        ArrayNode tools = BankTools.getTools();

        ArrayNode messages = arr(obj("role", "user", "content", question));

        for (int round = 1; round <= MAX_COUNTS; round++) {
            JsonNode response = client.chat(obj(
                    "model", PropertiesConfiguration.ollamaModel(),
                    "messages", messages,
                    "tools", tools
            ));

            JsonNode message = response.path("message");
            JsonNode toolCalls = message.path("tool_calls");

            if (toolCalls.isEmpty()) {
                return message.path("content").asText();
            }

            messages.add(message);

            for (JsonNode call : toolCalls) {

                String name = call.path("function").path("name").asText();
                JsonNode arguments = call.path("function").path("arguments");
                if (arguments.isTextual()) {
                    arguments = JsonUtil.parse(arguments.asText());
                }

                ObjectNode result = BankTools.execute(name, arguments);
                System.out.println("도구 실행 --" + name + arguments + " → " + result);

                messages.add(obj(
                        "role", "tool",
                        "tool_name", name,
                        "content", JsonUtil.toJson(result)
                ));
            }
        }
        throw new IllegalStateException("도구 호출이 " + MAX_COUNTS + "번 넘게 반복되었습니다.");
    }

}
