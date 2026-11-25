package io.eddie.step4;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static io.eddie.util.JsonUtil.*;

public class BankTools {

    /**
     * 도구의 이름으로 도구를 찾아서 실행하는 함수
     *
     * @param name {@code String} 도구의 이름(메서드 이름)
     * @param args {@link JsonNode} 도구 실행에 필요한 인자가 포함된 요청 본문
     * @return {@link ObjectNode} 도구 실행결과의 Json 객체
     */
    public static ObjectNode execute(String name, JsonNode args) {
        switch (name) {
            case "get_balance":
                return getBalance(args.path("account_number").asText());
            case "get_exchange_rate":
                return getExchangeRate(args.path("currency").asText());
            default:
                return obj("error", "알 수 없는 도구: " + name);
        }
    }

    /**
     * 계좌 잔액을 호출하는 도구, 계좌번호로 현재 잔액(원)을 조회
     *
     * @param accountNumber {@code String} `xxx-xxx`로 구성되어있는 계좌번호
     * @return {@link ObjectNode} 존재할 경우 계좌번호(account_number), 잔액(balance), 통화(currency)를 포함한 데이터를 반환하고, 존재하지 않을 경우에는 오류를 포함한 답변을 반환
     */
    static ObjectNode getBalance(String accountNumber) {
        if ("123-456".equals(accountNumber)) {
            return obj("account_number", accountNumber, "balance", 1_350_000, "currency", "KRW");
        }
        return obj("error", "존재하지 않는 계좌: " + accountNumber);
    }

    /**
     * 원화를 기준으로 환율을 구하는 도구, 외화 1단위가 몇 원인지 환율을 조회한다
     *
     * @param currency {@code String} 환율을 구하고자 하는 대상 통화코드(Currency Code)
     * @return {@link ObjectNode} 대상 통화코드(currency)와 원화(1000원)당 환율
     */
    static ObjectNode getExchangeRate(String currency) {
        switch (currency.toUpperCase()) {
            case "USD":
                return obj("currency", "USD", "krw_per_unit", 1200);
            case "JPY":
                return obj("currency", "JPY", "krw_per_unit", 850);
            default:
                return obj("error", "지원하지 않는 통화: " + currency);
        }
    }

    /**
     * 사용할 수 있는 도구의 목록을 반환
     *
     * @return {@link ArrayNode} 사용할 수 있는 노드의 목록 Json
     */
    public static ArrayNode getTools() {
        return arr(
                obj("type", "function", "function", obj(
                        "name", "get_balance",
                        "description", "계좌 잔액을 호출하는 도구, 계좌번호로 현재 잔액(원)을 조회",
                        "parameters", obj(
                                "type", "object",
                                "properties", obj("account_number", obj("type", "string", "description", "계좌번호. 예: 123-456")),
                                "required", arr("account_number")
                        )
                )),
                obj("type", "function", "function", obj(
                        "name", "get_exchange_rate",
                        "description", "원화를 기준으로 환율을 구하는 도구, 외화 1단위가 몇 원인지 환율을 조회한다",
                        "parameters", obj(
                                "type", "object",
                                "properties", obj("currency", obj("type", "string", "description", "통화 코드. 예: USD, JPY")),
                                "required", arr("currency")
                        )
                ))
        );
    }
}
