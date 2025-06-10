package com.example.demo.controller;


import com.google.cloud.firestore.Firestore;
import com.google.cloud.Timestamp;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;


@Controller
@RequestMapping("/reports")
public class MarkedReportController {

    @Value("${firebase.storage.bucket}")
    private String firebaseBucket;

    @PostMapping("/{id}/upload-marked")
    @ResponseBody
    public ResponseEntity<String> uploadMarkedImage(
            @PathVariable("id") String reportId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("markedBy") String markedBy,
            @RequestParam(value = "comment", required = false) String comment
    ) {
        try {
            // 1️⃣ 上傳至 Firebase Storage
            String fileName = "marked_images/" + reportId + ".png";
            InputStream stream = file.getInputStream();

            StorageClient.getInstance().bucket(firebaseBucket).create(fileName, stream, "image/png");

            // 2️⃣ 取得下載 URL（透過 public URL + token 自動生成）
            String downloadUrl = String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    firebaseBucket,
                    fileName.replace("/", "%2F")  // 轉義路徑
            );

            // 3️⃣ 寫入 Firestore 新 collection: marked_reports
            Firestore db = FirestoreClient.getFirestore();
            Map<String, Object> data = new HashMap<>();
            data.put("markedImageUrl", downloadUrl);
            data.put("markedBy", markedBy);
            data.put("markedAt", Timestamp.now());
            data.put("comment", StringUtils.hasText(comment) ? comment : "");

            db.collection("marked_reports").document(reportId).set(data);

            return ResponseEntity.ok("Marked image uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}

