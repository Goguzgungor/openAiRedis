package com.openAiRedis.assistant.entity;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@RedisHash("AssistantEntity")
@Data
@Getter
@Setter
@JsonSerialize
public class AssistantEntity implements Serializable {
    private OpenAiModel openAiModel;
    private int id;

    private int topicId;

    public AssistantEntity(OpenAiModel openAiModel, int id, int topicId) {
        this.openAiModel = openAiModel;
        this.id = id;
        this.topicId = topicId;
    }
}
