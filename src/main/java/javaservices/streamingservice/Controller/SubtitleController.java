package javaservices.streamingservice.Controller;

import javaservices.streamingservice.Services.SubtitleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200") // Replace with your Angular app's domain
public class SubtitleController {

    @Autowired
    private SubtitleService subtitleService;


    @GetMapping("/subtitles/{videoId}/{languageCode}")
    public ResponseEntity<Flux<DataBuffer>> serveSubtitle(@PathVariable String videoId,
                                              @PathVariable String languageCode) {

        String subtitlePath = subtitleService.determineSubtitlePath(videoId, languageCode);
        Flux<DataBuffer> subtitleContent = subtitleService.convertSrtToVtt(subtitlePath, new DefaultDataBufferFactory());

        return ResponseEntity.ok()
                .contentType(new MediaType("text", "vtt", StandardCharsets.UTF_8))
                .body(subtitleContent);
    }

    @GetMapping("/subtitles/{videoId}/languages")
    public List<String> getAvailableLanguages(@PathVariable String videoId) {
        return subtitleService.listAvailableLanguages(videoId);
    }
}