package javaservices.streamingservice.Services;

import javaservices.streamingservice.models.StreamInfo;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Service
public class PreProcessingService {


    public void convertToMp4(String sourcePath,String targetPath,StreamInfo stream) throws IOException, InterruptedException {
        targetPath = targetPath.replaceAll("\\.mkv$", ".mp4");

        ProcessBuilder builder = new ProcessBuilder();
        builder.command("ffmpeg", "-i", sourcePath, "-map", "0:v", "-map", "0:a:" + stream.getIndex(), "-c:v", "copy", "-c:a", "copy", targetPath);
        builder.redirectErrorStream(true);

        Process process = builder.start();

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        int exitValue = process.waitFor();
        if (exitValue != 0) {
            throw new RuntimeException("FFmpeg failed with exit code " + exitValue);
        }
    }

    public void executeFfmpegCommand(ProcessBuilder builder) throws IOException, InterruptedException {

        builder.redirectErrorStream(true);

        Process process = builder.start();

        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

    }

    public void Preprocess(String sourcePath) throws IOException, InterruptedException {
        List<StreamInfo> streamInfos = parseFFprobeOutput(executeFFprobeCommand(sourcePath));
        for (StreamInfo streamInfo : streamInfos) {
            extractOrConvertStream(sourcePath,streamInfo);
        }

    }

    private static List<StreamInfo> parseFFprobeOutput(List<String> ffprobeOutput) {
        List<StreamInfo> streams = new ArrayList<>();
        Map<String, String> currentStreamInfo = new HashMap<>();

        for (String line : ffprobeOutput) {
            if (line.startsWith("index=")) {
                // If currentStreamInfo is not empty, it means we've finished collecting info for a stream
                if (!currentStreamInfo.isEmpty()) {
                    StreamInfo stream = new StreamInfo(
                            Integer.parseInt(currentStreamInfo.getOrDefault("index", "0")),
                            currentStreamInfo.getOrDefault("codec_type", ""),
                            currentStreamInfo.getOrDefault("codec_name", ""),
                            currentStreamInfo.getOrDefault("TAG:language", "unknown")
                    );
                    streams.add(stream);
                    currentStreamInfo.clear();
                }
            }
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                currentStreamInfo.put(parts[0], parts[1]);
            }
        }

        // Add the last stream if currentStreamInfo is not empty
        if (!currentStreamInfo.isEmpty()) {
            StreamInfo stream = new StreamInfo(
                    Integer.parseInt(currentStreamInfo.getOrDefault("index", "0")),
                    currentStreamInfo.getOrDefault("codec_type", ""),
                    currentStreamInfo.getOrDefault("codec_name", ""),
                    currentStreamInfo.getOrDefault("TAG:language", "unknown")
            );
            streams.add(stream);
        }

        return streams;
    }


    private static List<String> executeFFprobeCommand(String filePath) {
        List<String> outputLines = new ArrayList<>();
        List<String> command = Arrays.asList(
                "ffprobe",
                "-v", "error",
                "-show_entries", "stream=index,codec_name,codec_type,channels:stream_tags=language",
                "-of", "default=noprint_wrappers=1",
                filePath
        );

        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            Process process = builder.start();

            // Capture and log the standard output
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        outputLines.add(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            outputThread.start();

            // Capture and log the error output
            Thread errorThread = new Thread(() -> logProcessOutput(process.getErrorStream()));
            errorThread.start();

            // Wait for the process to complete and threads to finish
            int exitCode = process.waitFor();
            outputThread.join();
            errorThread.join();

            System.out.println("FFprobe process exited with code " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return outputLines;
    }

    private void extractOrConvertStream(String filePath, StreamInfo stream) throws IOException, InterruptedException {
        String outputFilePath;
        List<String> command = new ArrayList<>();

        command.add("ffmpeg");
        command.add("-i");
        command.add(filePath);

        if ("audio".equals(stream.getCodecType() ) || "video".equals(stream.getCodecType())) {
            outputFilePath = String.format("%s_%s.mp4", filePath.substring(0, filePath.lastIndexOf('.')), stream.getCodecName());
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("ffmpeg", "-i", filePath, "-map", "0:v", "-map", "0:a:" + stream.getIndex(), "-c:v", "copy", "-c:a", "aac", outputFilePath);
            executeFfmpegCommand(builder);
        } else if ("subtitle".equals(stream.getCodecType())) {
            outputFilePath = String.format("%s_%s.srt", filePath.substring(0, filePath.lastIndexOf('.')), stream.getLanguage());
            command.add(outputFilePath);
            ProcessBuilder builder = new ProcessBuilder();
            builder.command("ffmpeg", "-i", filePath, "-map", "0:" + stream.getIndex(),"-c" , "copy", outputFilePath);
            executeFfmpegCommand(builder);
        } else {
            // If the stream is neither audio nor subtitle, do nothing.
            return;
        }

    }

    private static void executeProcess(List<String> command) {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command(command);
        try {
            Process process = builder.start();


            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            int exitValue = process.waitFor();
            if (exitValue != 0) {
                throw new RuntimeException("FFmpeg failed with exit code " + exitValue);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void logProcessOutput(InputStream inputStream) {
        new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(System.out::println);
    }
}
