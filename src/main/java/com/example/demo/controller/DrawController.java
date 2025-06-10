package com.example.demo.controller;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reports")
public class DrawController {

    @GetMapping("/draw/{id}")
    public String drawPage(@PathVariable("id") String reportId, Model model) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentSnapshot document = db.collection("reports").document(reportId).get().get();

            if (!document.exists()) {
                model.addAttribute("error", "報告不存在！");
                return "error";
            }

            String imageUrl = document.getString("imageUrl");
            if (imageUrl == null || imageUrl.isBlank()) {
                model.addAttribute("error", "圖片連結不存在！");
                return "error";
            }

            model.addAttribute("reportId", reportId);
            model.addAttribute("imageUrl", imageUrl);
            return "draw";

        } catch (Exception e) {
            model.addAttribute("error", "讀取報告錯誤：" + e.getMessage());
            return "error";
        }
    }
}

