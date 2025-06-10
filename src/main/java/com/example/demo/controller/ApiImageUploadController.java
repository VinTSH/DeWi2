package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/image")
public class ApiImageUploadController {

    private static final String UPLOAD_DIR = "uploads"; // 儲存目錄

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ No file selected.");
        }

        try {
            // 建立目錄（如果未存在）
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 儲存檔案
            File dest = new File(UPLOAD_DIR + "/" + file.getOriginalFilename());
            file.transferTo(dest);

            return ResponseEntity.ok("✅ Image uploaded successfully: " + dest.getName());
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Failed to upload image.");
        }
    }
}
