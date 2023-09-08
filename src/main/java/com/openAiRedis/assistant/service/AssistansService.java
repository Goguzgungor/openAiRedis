package com.openAiRedis.assistant.service;


import com.openAiRedis.assistant.constants.Role;
import com.openAiRedis.assistant.entity.AssistantEntity;
import com.openAiRedis.assistant.entity.OpenAiModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;






@Service
@Slf4j
public class AssistansService {
    private final String REDIS_KEY = Role.assistant;
    private final RedisTemplate<String, AssistantEntity> redisTemplate;
    @Autowired
    public AssistansService(RedisTemplate<String, AssistantEntity> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    private String generateKeyCode(int id, int topicId) {
        return id+"-"+topicId;
    }
    public OpenAiModel createChatSession(OpenAiModel openAiModel, int id, int topicId) {
        AssistantEntity assistantEntity = new AssistantEntity(openAiModel, id, topicId);
        String keyCode = generateKeyCode(id, topicId);
        redisTemplate.opsForHash().put(REDIS_KEY,keyCode, assistantEntity);
        return openAiModel;
    }
    public OpenAiModel findChatSession(int id, int topicId) {
        String keyCode = generateKeyCode(id, topicId);
        boolean hasKey = redisTemplate.opsForHash().hasKey(REDIS_KEY, keyCode);
        if (hasKey) {
            AssistantEntity assistantEntity = (AssistantEntity) redisTemplate.opsForHash().get(REDIS_KEY, keyCode);
            return assistantEntity.getOpenAiModel();
        } else {
            return createChatSession(new OpenAiModel(), id, topicId);
        }
    }
}
