package dev.jacaceresf.archie.services;

import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class AssistantService {

    private final ChatClient chatClient;
    @Value("classpath:prompts/technical-system.st")
    private Resource technicalPromptResource;
    @Value("classpath:prompts/general-system.st")
    private Resource generalPromptResource;
    @Value("classpath:prompts/research-assistant.st")
    private Resource researchAssistantPromptResource;

    @Autowired
    public AssistantService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String ask(String topic, String userQuestion, boolean isTechnical) {
        Assert.hasText(userQuestion, "User question must not be empty");

        Resource promptResource = isTechnical ? technicalPromptResource : generalPromptResource;
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(promptResource);

        SystemMessage systemMessage = (SystemMessage) systemPromptTemplate.createMessage();
        PromptTemplate userPrompt = new PromptTemplate(researchAssistantPromptResource);

        Message userMessage = userPrompt.createMessage(
                Map.of(
                        "topic", topic,
                        "userQuestion", userQuestion
                )
        );

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));

        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
