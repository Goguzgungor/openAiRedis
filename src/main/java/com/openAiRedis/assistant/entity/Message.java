package com.openAiRedis.assistant.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;



@NoArgsConstructor
@AllArgsConstructor
@RedisHash("Message")
@JsonSerialize
@Getter
@Setter
public class Message implements Serializable {
    private String content;
    private String role;
}
