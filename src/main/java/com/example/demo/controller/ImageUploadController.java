package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
public class ImageUploadController {

    // Path where images will be saved
    private final String uploadDir = "/home/space/Documents/manualSpring/demo/uploads";

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("message", "Upload an image to analyze.");
        return "index";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            model.addAttribute("message", "Please select a file to upload.");
            return "index";
        }

        try {
            // Create folder if it doesn’t exist
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            // Save the file
            File dest = new File(uploadDir, file.getOriginalFilename());
            file.transferTo(dest);

            model.addAttribute("message", "File uploaded successfully: " + file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("message", "Failed to upload file: " + e.getMessage());
        }

        return "index";
    }
}

