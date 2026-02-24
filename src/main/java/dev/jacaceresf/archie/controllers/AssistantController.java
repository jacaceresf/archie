package dev.jacaceresf.archie.controllers;

import dev.jacaceresf.archie.services.AssistantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ask")
public class AssistantController {

    private final AssistantService assistantService;

    @Autowired
    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @GetMapping("/technical")
    public String technicalQuestion(
            @RequestParam("topic") String topic,
            @RequestParam("question") String userQuestion
    ) {
        return assistantService.ask(topic, userQuestion, true);
    }

    @GetMapping("/general")
    public String generalQuestion(@RequestParam("question") String userQuestion) {
        return assistantService.ask("", userQuestion, false);
    }
}
