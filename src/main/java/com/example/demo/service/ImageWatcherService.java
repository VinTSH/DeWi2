package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ImageWatcherService {

	private final Path uploadDir = Paths.get("C:/Users/khtwo/OneDrive/文件/GitHub/Springboot-TF/uploads");

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ImageAnalyzer imageAnalyzer;
    private final NotificationService notificationService;

    public ImageWatcherService(ImageAnalyzer imageAnalyzer, NotificationService notificationService) {
        this.imageAnalyzer = imageAnalyzer;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void watchUploads() {
        executor.submit(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                uploadDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path filePath = uploadDir.resolve((Path) event.context());
                        File file = filePath.toFile();

                        // Allow file to finish uploading
                        Thread.sleep(500);

                        // Analyze image
                        String result = imageAnalyzer.analyzeImage(file);
                        // Notify front end
                        notificationService.sendMessage("Image processed: " + file.getName() + " – Result: " + result);
                    }
                    key.reset();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}

