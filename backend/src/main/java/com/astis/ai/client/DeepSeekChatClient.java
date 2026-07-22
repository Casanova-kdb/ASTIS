package com.astis.ai.client;

import com.astis.ai.config.DeepSeekProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class DeepSeekChatClient {

    private final DeepSeekProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public DeepSeekChatClient(DeepSeekProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public boolean isConfigured() {
        return properties.isConfigured();
    }

    public String model() {
        return properties.model();
    }

    public int handbookMaxTokens() {
        return properties.handbookMaxTokens();
    }

    public String generateAdvice(String prompt) {
        return generateCompletion(
                "You are a study planning assistant. Give concise, practical advice based only on the provided ranked tasks.",
                prompt,
                properties.maxTokens()
        );
    }

    public String generateCompletion(String systemPrompt, String prompt, int maxTokens) {
        if (!isConfigured()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "DeepSeek API key is not configured");
        }

        try {
            DeepSeekChatRequest requestBody = new DeepSeekChatRequest(
                    properties.model(),
                    List.of(
                            new ChatMessage("system", systemPrompt),
                            new ChatMessage("user", prompt)
                    ),
                    properties.temperature(),
                    maxTokens,
                    false
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(properties.baseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(properties.requestTimeoutSeconds()))
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "DeepSeek request failed with status " + response.statusCode()
                );
            }

            DeepSeekChatResponse chatResponse = objectMapper.readValue(response.body(), DeepSeekChatResponse.class);
            if (chatResponse.choices() == null || chatResponse.choices().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "DeepSeek returned no completion");
            }

            String content = chatResponse.choices().get(0).message().content();
            if (content == null || content.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "DeepSeek returned empty completion");
            }
            return content.strip();
        } catch (HttpTimeoutException exception) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "DeepSeek request timed out");
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "DeepSeek response could not be processed");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "DeepSeek request was interrupted");
        }
    }

    private record DeepSeekChatRequest(
            String model,
            List<ChatMessage> messages,
            double temperature,
            @JsonProperty("max_tokens")
            int maxTokens,
            boolean stream
    ) {
    }

    private record ChatMessage(String role, String content) {
    }

    private record DeepSeekChatResponse(List<ChatChoice> choices) {
    }

    private record ChatChoice(ChatMessage message) {
    }
}
