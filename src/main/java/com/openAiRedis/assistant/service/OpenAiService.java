package com.openAiRedis.assistant.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.openAiRedis.assistant.constants.OpenAI;
import com.openAiRedis.assistant.constants.Role;
import com.openAiRedis.assistant.entity.Message;
import com.openAiRedis.assistant.entity.OpenAiModel;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;




@Service
public class OpenAiService {
    public Message sendRequest(OpenAiModel openAiModel) throws IOException, InterruptedException {
        Message message = new Message();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(OpenAI.url))
                .header(OpenAI.contentTypeKey, OpenAI.contentTypeValue)
                .header(OpenAI.auth, OpenAI.bearer + OpenAI.token)
                .POST(HttpRequest.BodyPublishers.ofString(this.makeJson(openAiModel)))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(response.body());
        String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
        message.setContent(content);
        message.setRole(Role.assistant);
        return message;
    }
    public String makeJson(OpenAiModel openAiModel)  {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            return mapper.writeValueAsString(openAiModel);
        }
        catch (JsonProcessingException e) {
            return e.getMessage();
        }
    }
}
