package javaservices.streamingservice.Controller;

import javaservices.streamingservice.Services.PreProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.springframework.core.io.buffer.DataBufferUtils;

@CrossOrigin(origins = "http://localhost:4200") // Replace with your Angular app's domain
@RestController
public class PreProcessingController {

    @Autowired
    private PreProcessingService preProcessingService;
    @Value("${original.videos.directory.path}")
    private String originalVideosPath;



    @PostMapping("/upload")
    public Mono<String> uploadPlus(@RequestPart("file") FilePart filePart) {
        String filePath = originalVideosPath + filePart.filename();
        Path path = Paths.get(filePath);

        // Log and release each DataBuffer after writing
        Flux<DataBuffer> content = filePart.content()
                .doOnNext(dataBuffer -> {
                    System.out.println("Received chunk size: " + dataBuffer.readableByteCount() + " bytes");
                    // It's important not to release the dataBuffer here since it's being used in write operation
                });

        // Write the dataBuffer flux to the file
        return DataBufferUtils.write(content, path, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
                .then() // Completes the Mono<Void> from write operation to Mono<String>
                .then(Mono.fromRunnable(() -> {
                    try {
                        preProcessingService.Preprocess(filePath);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }))
                .thenReturn("Upload and conversion complete")
                .doFinally(signal -> System.out.println("Upload complete. File saved to: " + filePath));
    }



}
