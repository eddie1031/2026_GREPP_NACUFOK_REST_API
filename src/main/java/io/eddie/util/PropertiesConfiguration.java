package io.eddie.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class PropertiesConfiguration {

    private static final String PROPERTIES_FILE = "ai.properties";
    private static final Properties PROPS = loadFromClasspath();

    public static String provider() {
        String provider = get("ai.provider", "openai").toLowerCase();

        if (!provider.equals("openai") && !provider.equals("gemini") && !provider.equals("ollama")) {
            throw new IllegalStateException("provider를 확인하여 주시기 바랍니다.");
        }

        return provider;
    }

    public static boolean isGemini() {
        return "gemini".equals(provider());
    }

    public static boolean isOllama() {
        return "ollama".equals(provider());
    }

    public static String openAiKey() {
        return required("openai.api.key");
    }

    public static String openAiModel() {
        return get("openai.model", "gpt-4.1-mini");
    }

    public static String geminiKey() {
        return required("gemini.api.key");
    }

    public static String geminiModel() {
        return get("gemini.model", "gemini-2.5-flash");
    }

    public static String ollamaBaseUrl() {
        return get("ollama.base.url", "http://localhost:11434");
    }

    public static String ollamaModel() {
        return get("ollama.model", "qwen2.5:7b");
    }

    public static String model() {
        switch (provider()) {
            case "gemini":
                return geminiModel();
            case "ollama":
                return ollamaModel();
            default:
                return openAiModel();
        }
    }

    public static String get(String key, String defaultValue) {
        String value = System.getProperty(key);

        if (isBlank(value)) {
            value = System.getenv(key.toUpperCase().replace('.', '_'));
        }

        if (isBlank(value)) {
            value = PROPS.getProperty(key);
        }

        return isBlank(value)
                ? defaultValue
                : value.trim()
                ;
    }

    private static String required(String key) {
        String value = get(key, null);

        if (value == null) {
            throw new IllegalStateException(
                    "설정 '" + key + "' 가 없습니다. src/main/resources/" + PROPERTIES_FILE
                            + " 또는 환경변수 " + key.toUpperCase().replace('.', '_') + " 를 설정하세요.");
        }

        return value;
    }

    private static Properties loadFromClasspath() {
        Properties props = new Properties();

        try (InputStream in = PropertiesConfiguration.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (in != null) {
                props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new IllegalStateException(PROPERTIES_FILE + " 읽기 실패", e);
        }
        return props;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

}
