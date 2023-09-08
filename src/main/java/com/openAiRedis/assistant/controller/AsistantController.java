package com.openAiRedis.assistant.controller;
import com.openAiRedis.assistant.entity.AssistantRequestDto;
import com.openAiRedis.assistant.entity.Message;
import com.openAiRedis.assistant.entity.OpenAiModel;
import com.openAiRedis.assistant.service.AssistansService;
import com.openAiRedis.assistant.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;


@RestController
@RequestMapping("/api/v1/asistant")
@Slf4j
public class AsistantController {
    private final AssistansService assistansService;
    private final OpenAiService openAiService;
    public AsistantController(AssistansService assistansService, OpenAiService openAiService) {
        this.assistansService = assistansService;
        this.openAiService = openAiService;
    }
    @PostMapping("/sendMessage")
    public ResponseEntity<Message> sendMessage(@RequestBody AssistantRequestDto assistantRequestDto) throws IOException, InterruptedException {
        OpenAiModel openAiModel = assistansService.findChatSession(assistantRequestDto.getId(), assistantRequestDto.getTopicId());
        openAiModel.addMessage(assistantRequestDto.getMessage());
        Message message = openAiService.sendRequest(openAiModel);
        openAiModel.addMessage(message);
        assistansService.createChatSession(openAiModel,assistantRequestDto.getId(), assistantRequestDto.getTopicId());
        return ResponseEntity.ok(message);
    }
    @GetMapping("/get/{id}/{topicId}")
    public OpenAiModel getChatHistory(@PathVariable int id, @PathVariable int topicId){
        return assistansService.findChatSession(id, topicId);
    }
}
