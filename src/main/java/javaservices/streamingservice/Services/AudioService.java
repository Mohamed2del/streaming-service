package javaservices.streamingservice.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AudioService {
    @Value("${original.videos.directory.path}")
    private String path;

    public List<String> listAvailableLanguages(String videoId) {
        List<String> languages = new ArrayList<>();
        // Define the pattern to match files "{videoId}_{language_code}.mp4"
        Pattern pattern = Pattern.compile(Pattern.quote(videoId) + "_([a-z0-9]+)\\.mp4");

        try {
            // Convert string path to Path object
            Path subtitlesDir = Paths.get(path);
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
