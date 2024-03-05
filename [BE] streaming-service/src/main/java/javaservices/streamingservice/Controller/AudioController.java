package javaservices.streamingservice.Controller;

import javaservices.streamingservice.Services.AudioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class AudioController {

    @Autowired
    AudioService audioService;
    @GetMapping("/audio/{videoId}/languages")
    public List<String> getAvailableLanguages(@PathVariable String videoId) {
        return audioService.listAvailableLanguages(videoId);
    }
}
