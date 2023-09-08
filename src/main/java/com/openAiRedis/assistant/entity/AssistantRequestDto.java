package com.openAiRedis.assistant.entity;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Data
@Getter
@Setter
public class AssistantRequestDto implements Serializable {
    private int id;
    private int topicId;
    private Message message;
}
