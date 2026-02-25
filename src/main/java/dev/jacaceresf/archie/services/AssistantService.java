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

        // Depending on whether the question is technical or general, we select the appropriate system prompt resource.
        Resource promptResource = isTechnical ? technicalPromptResource : generalPromptResource;

        // A system prompt template is used to create a system message that sets the context for the assistant's response.
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(promptResource);
        SystemMessage systemMessage = (SystemMessage) systemPromptTemplate.createMessage();

        // A user prompt template is used to create a user message that includes the user's question and any relevant context (like the topic for technical questions).
        PromptTemplate userPrompt = new PromptTemplate(researchAssistantPromptResource);
        Message userMessage = userPrompt.createMessage(
                Map.of(
                        "topic", topic,
                        "userQuestion", userQuestion
                )
        );

        // A prompt is created by combining the system message and the user message. This prompt is then sent to the chat client, which generates a response based on the provided messages.
        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
