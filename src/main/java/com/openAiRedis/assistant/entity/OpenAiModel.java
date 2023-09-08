package com.openAiRedis.assistant.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.openAiRedis.assistant.constants.OpenAI;
import com.openAiRedis.assistant.constants.Role;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
@RedisHash("OpenAiModel")
@JsonSerialize
@Getter
@Setter
public class OpenAiModel implements Serializable {
    private  String model ;
    private double temperature;
    private  List<Message> messages;

    public OpenAiModel() {
        this.model = OpenAI.model;
        this.temperature = OpenAI.temperature;
        this.messages = new ArrayList<>();
        addSystemMessage();
    }

    private void addSystemMessage() {
        Message message = new Message(OpenAI.systemMessage, Role.systemRole);
        this.messages.add(message);
    }
    public void addMessage(Message message) {
        this.messages.add(message);
    }

}
