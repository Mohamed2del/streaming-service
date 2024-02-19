package javaservices.streamingservice.Controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RestController
public class VideoStreamingController {

    @Value("${original.videos.directory.path}")
    private String videoPath;

    private static final long MAX_CHUNK_SIZE = 1024 * 1024; // 1MB

    @GetMapping("/videos/{filename}")
    public Mono<ResponseEntity<Flux<DataBuffer>>> streamVideo(@PathVariable String filename, ServerWebExchange exchange) {
        Path path = Paths.get(videoPath, filename + ".mp4");
        DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
        HttpHeaders headers = new HttpHeaders();
        long chunksServed = 0;
        return Mono.fromCallable(() -> {
            FileChannel channel = FileChannel.open(path, StandardOpenOption.READ);
            long contentLength = channel.size();
            HttpStatus status = HttpStatus.OK;
            headers.add(HttpHeaders.CONTENT_TYPE, "video/mp4");

            // Default range is the full video
            final long[] range = new long[]{0, contentLength - 1};

            String rangeHeader = exchange.getRequest().getHeaders().getFirst("Range");
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {

                String[] ranges = rangeHeader.substring("bytes=".length()).split("-");
                range[0] = Long.parseLong(ranges[0]); // Start of the range

                long rangeLength = range[1] - range[0] + 1;
                if (rangeLength > MAX_CHUNK_SIZE) {
                    range[1] = range[0] + MAX_CHUNK_SIZE - 1;
                }

                headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + range[0] + "-" + range[1] + "/" + contentLength);
                headers.setContentLength(range[1] - range[0] + 1);
                status = HttpStatus.PARTIAL_CONTENT;
                headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");

                channel.position(range[0]);
            }

            Flux<DataBuffer> dataBufferFlux = Flux.create(fluxSink -> {
                try {
                    ByteBuffer byteBuffer = ByteBuffer.allocate(8192); // Adjust the buffer size as needed
                    while (!fluxSink.isCancelled() && channel.position() <= range[1]) {
                        int read = channel.read(byteBuffer);
                        if (read <= 0) {
                            break;
                        }
                        byteBuffer.flip();
                        fluxSink.next(bufferFactory.wrap(byteBuffer.array()));
                        byteBuffer.clear();
                    }
                    fluxSink.complete();
                    channel.close();
                } catch (Exception e) {
                    fluxSink.error(e);
                }
            });

            return ResponseEntity.status(status)
                    .headers(headers)
                    .body(dataBufferFlux);
        });
    }
}
