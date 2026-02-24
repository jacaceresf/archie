package dev.jacaceresf.archie.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class SpeakService {

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant that provides concise and accurate answers to user questions.
            
            Your responses should be clear and must not exceed 100 words. Always provide direct answers to the user's questions without unnecessary elaboration. 
            
            """;
    private final ChatClient chatClient;

    @Autowired
    public SpeakService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public String speak(String userQuestion) {
        Assert.hasText(userQuestion, "User question must not be empty");
        return chatClient.prompt(userQuestion)
                .call()
                .content();
    }
}
