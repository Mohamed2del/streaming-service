package javaservices.streamingservice.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SubtitleService {

    private final Path fileStorageLocation = Paths.get("C:\\Users\\moham\\Videos\\sub\\");

    @Value("${original.videos.directory.path}")
    private String subtitlesDirPath;

    private String path = "C:\\Users\\moham\\Videos\\sub\\";


    public Flux<DataBuffer> convertSrtToVtt(String subtitlePath, DataBufferFactory bufferFactory) {
        Path path = Path.of(subtitlePath.replace("\\","\\\\"));

        Flux<DataBuffer> header = Flux.just(bufferFactory.wrap("WEBVTT\n\n".getBytes(StandardCharsets.UTF_8)));

        Flux<DataBuffer> content = Flux.using(() -> Files.lines(path, StandardCharsets.UTF_8),
                lines -> Flux.fromStream(lines.map(line -> {
                    String correctedLine = line.replaceAll("(\\d{2}:\\d{2}:\\d{2}),(\\d{3})", "$1.$2") + "\n";
                    return bufferFactory.wrap(correctedLine.getBytes(StandardCharsets.UTF_8));
                })),
                stream -> stream.close() // Closing the stream after use
        );

        // Concatenate the header and content, so the header is emitted first
        return Flux.concat(header, content);
    }

    public String determineSubtitlePath(String videoId, String languageCode) {
        return path + videoId + "_" + languageCode + ".srt";
    }


    public List<String> listAvailableLanguages(String videoId) {
        List<String> languages = new ArrayList<>();
        // Define the pattern to match files "{videoId}_{language_code}.vtt"
        Pattern pattern = Pattern.compile(Pattern.quote(videoId) + "_(\\w+)\\.srt");

        try {
            // Convert string path to Path object
            Path subtitlesDir = Paths.get(subtitlesDirPath);
            // Ensure the directory exists and is readable
            if (Files.exists(subtitlesDir) && Files.isDirectory(subtitlesDir)) {
                // Stream over the files in the directory
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(subtitlesDir)) {
                    for (Path entry : stream) {
                        Matcher matcher = pattern.matcher(entry.getFileName().toString());
                        if (matcher.matches()) {
                            // If a file matches, add the language code to the list
                            languages.add(matcher.group(1));
                        }
                    }
                } catch (DirectoryIteratorException ex) {
                    // I/O error encountered during the iteration, the cause is an IOException
                    throw ex.getCause();
                }
            }
        } catch (IOException e) {
            // Proper exception handling goes here
            e.printStackTrace();
        }

        return languages;
    }
}
