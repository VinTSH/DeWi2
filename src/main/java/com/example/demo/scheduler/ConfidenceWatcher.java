package com.example.demo.scheduler;

import com.example.demo.service.EmailService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ConfidenceWatcher {

    private final EmailService emailService;
    private final Firestore db = FirestoreClient.getFirestore();
    private static final String EMAIL_TO = "kwokhoitung8@gmail.com"; // ✅ 改成你自己 email

    public ConfidenceWatcher(EmailService emailService) {
        this.emailService = emailService;
    }

    @Scheduled(fixedRate = 5000) // 每 5 秒
    public void checkNewConfidenceReports() {
        System.out.println("🔍 Scanning Firestore for confidence updates...");

        CollectionReference reportsRef = db.collection("reports");
        ApiFuture<QuerySnapshot> future = reportsRef.get();

        try {
            QuerySnapshot snapshot = future.get();
            for (DocumentSnapshot doc : snapshot.getDocuments()) {

                Object confidenceObj = doc.get("confidence");
                Double confidence = null;

                try {
                    if (confidenceObj instanceof Number) {
                        confidence = ((Number) confidenceObj).doubleValue();
                    } else if (confidenceObj instanceof String) {
                        String confStr = ((String) confidenceObj).replace("%", "").trim();
                        confidence = Double.parseDouble(confStr);
                    } else if (confidenceObj == null) {
                        System.out.println("⚠️ confidence 欄位缺失: documentId = " + doc.getId());
                        continue;
                    } else {
                        System.out.println("⚠️ 無法識別 confidence 類型：" + confidenceObj.getClass().getSimpleName() + " in " + doc.getId());
                        continue;
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ confidence parsing error in " + doc.getId() + ": " + e.getMessage());
                    continue;
                }

                // ✅ 再安全 check 一次：null or unreasonable values
                if (confidence == null || confidence < 0 || confidence > 100) {
                    System.out.println("⚠️ 無效 confidence 數值: " + confidence + " in documentId = " + doc.getId());
                    continue;
                }

                Boolean notified = doc.getBoolean("notified");
                if (Boolean.TRUE.equals(notified)) continue;

                String imageName = doc.getString("imageName");
                String subject = "📢 New Report Ready for Review";
                String body = "Report ID: " + doc.getId() +
                        "\nImage: " + (imageName != null ? imageName : "(not specified)") +
                        "\nConfidence: " + String.format("%.2f%%", confidence) +
                        "\n\nPlease review it in the manager dashboard.";

                emailService.sendNotification(EMAIL_TO, subject, body);
                System.out.println("📧 Email sent to manager for report: " + doc.getId());

                // 更新 Firestore 狀態
                reportsRef.document(doc.getId()).update("notified", true);
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to check confidence reports: " + e.getMessage());
        }
    }
}
