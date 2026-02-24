package dev.jacaceresf.archie.controllers;

import dev.jacaceresf.archie.services.SpeakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/speak")
public class SpeakController {

    private final SpeakService speakService;

    @Autowired
    public SpeakController(SpeakService speakService) {
        this.speakService = speakService;
    }

    @GetMapping("/ask")
    public String speak(@RequestParam("question") String userQuestion) {
        return speakService.speak(userQuestion);
    }
}
