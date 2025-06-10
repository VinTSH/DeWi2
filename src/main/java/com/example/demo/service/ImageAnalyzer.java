package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.tensorflow.*;

import java.io.File;
import java.nio.file.Files;

@Service
public class ImageAnalyzer {

    private static final String MODEL_PATH = "/home/space/Documents/manualSpring/demo/tf-models/image_shape_model.pb";

    public String analyzeImage(File file) {
        try {
            byte[] graphDef = Files.readAllBytes(new File(MODEL_PATH).toPath());
            byte[] imageBytes = Files.readAllBytes(file.toPath());

            try (Graph graph = new Graph()) {
                graph.importGraphDef(graphDef);

                try (Session session = new Session(graph)) {
                    Tensor<String> inputTensor = Tensors.create(imageBytes);

                    Tensor<?> output = session.runner()
                        .feed("input_image", inputTensor)
                        .fetch("output_shape")
                        .run()
                        .get(0);

                    long[] shape = output.copyTo(new long[1][3])[0];
                    return "Width: " + shape[1] + ", Height: " + shape[0] + ", Channels: " + shape[2];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error during analysis";
        }
    }
}

