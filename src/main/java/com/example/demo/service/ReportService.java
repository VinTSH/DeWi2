package com.example.demo.service;

import com.example.demo.model.Report;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final List<Report> reportStore = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Report> getAllReportsFromFirestore() {
        List<Report> reports = new ArrayList<>();
        try {
            Firestore db = FirestoreClient.getFirestore();
            ApiFuture<QuerySnapshot> future = db.collection("reports").get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot doc : documents) {
                try {
                    Report report = new Report();

                    report.setId(doc.getId());
                    report.setUploader(doc.getString("uploader"));
                    report.setStatus(doc.getString("status"));
                    report.setImageName(doc.getString("imageName"));
                    report.setImageUrl(doc.getString("imageUrl"));
                    report.setResult(doc.getString("result"));
                    report.setNotified(Boolean.TRUE.equals(doc.getBoolean("notified")));
                    report.setWorkerId(doc.getString("workerId"));
                    report.setSite(doc.getString("site"));
                    report.setBatchId(doc.getString("batchId"));
                    report.setWeather(doc.getString("weather"));
                    report.setGrade(doc.getString("grade"));
                    report.setComment(doc.getString("comment"));

                    Object ts = doc.get("timestamp");
                    if (ts instanceof Timestamp) {
                        report.setTimestamp((Timestamp) ts);
                    } else {
                        report.setTimestamp(null);
                    }

                    Object confidenceRaw = doc.get("confidence");
                    if (confidenceRaw instanceof Number) {
                        report.setConfidence(((Number) confidenceRaw).doubleValue());
                    } else if (confidenceRaw instanceof String) {
                        try {
                            report.setConfidence(Double.parseDouble((String) confidenceRaw));
                        } catch (NumberFormatException e) {
                            report.setConfidence(0.0);
                        }
                    } else {
                        report.setConfidence(0.0);
                    }

                    // 🔍 Check if marked report exists
                    DocumentSnapshot markedDoc = db.collection("marked_reports").document(doc.getId()).get().get();
                    report.setHasMarkedImage(markedDoc.exists());

                    reports.add(report);
                } catch (Exception e) {
                    System.err.println("⚠️ Error converting document to Report: " + doc.getId());
                    e.printStackTrace();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            System.err.println("❌ Failed to fetch reports from Firestore.");
            e.printStackTrace();
        }
        return reports;
    }

    public Report saveReport(String imageName, String site, String batchId, String workerName, String weather,
                             double confidence, String grade) {
        Firestore db = FirestoreClient.getFirestore();
        Report report = new Report();
        report.setImageName(imageName);
        report.setSite(site);
        report.setBatchId(batchId);
        report.setTimestamp(Timestamp.now());
        report.setWorkerId(workerName);
        report.setWeather(weather);
        report.setConfidence(confidence);
        report.setGrade(grade);
        report.setStatus("pending");
        report.setComment(null);

        db.collection("reports").add(report);
        return report;
    }

    public List<Report> getAllReports() {
        return reportStore;
    }

    public List<Report> getReportsByWorker(String workerName) {
        return reportStore.stream()
                .filter(r -> workerName.equals(r.getWorkerId()))
                .toList();
    }

    public Report getReportById(String id) {
        try {
            Firestore db = FirestoreClient.getFirestore();
            DocumentReference docRef = db.collection("reports").document(id);
            DocumentSnapshot doc = docRef.get().get();

            if (!doc.exists()) return null;

            Report report = new Report();

            report.setId(doc.getId());
            report.setUploader(doc.getString("uploader"));
            report.setStatus(doc.getString("status"));
            report.setImageName(doc.getString("imageName"));
            report.setImageUrl(doc.getString("imageUrl"));
            report.setResult(doc.getString("result"));
            report.setNotified(Boolean.TRUE.equals(doc.getBoolean("notified")));
            report.setWorkerId(doc.getString("workerId"));
            report.setSite(doc.getString("site"));
            report.setBatchId(doc.getString("batchId"));
            report.setWeather(doc.getString("weather"));
            report.setGrade(doc.getString("grade"));
            report.setComment(doc.getString("comment"));

            Object ts = doc.get("timestamp");
            if (ts instanceof Timestamp) {
                report.setTimestamp((Timestamp) ts);
            } else {
                report.setTimestamp(null);
            }

            Object confidenceRaw = doc.get("confidence");
            if (confidenceRaw instanceof Number) {
                report.setConfidence(((Number) confidenceRaw).doubleValue());
            } else if (confidenceRaw instanceof String) {
                try {
                    report.setConfidence(Double.parseDouble((String) confidenceRaw));
                } catch (NumberFormatException e) {
                    report.setConfidence(0.0);
                }
            } else {
                report.setConfidence(0.0);
            }

            // 🔍 Check if marked report exists
            DocumentSnapshot markedDoc = db.collection("marked_reports").document(id).get().get();
            report.setHasMarkedImage(markedDoc.exists());

            return report;

        } catch (Exception e) {
            System.err.println("❌ Error getting report by ID: " + id);
            e.printStackTrace();
            return null;
        }
    }

    public void updateReportStatus(String id, String status, String comment) {
        Firestore db = FirestoreClient.getFirestore();
        DocumentReference docRef = db.collection("reports").document(id);
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("comment", comment);
        docRef.update(updates);
    }

    public List<Report> searchReports(String worker, String status, List<String> siteList,
                                      LocalDate startDate, LocalDate endDate) {

        List<Report> allReports = getAllReportsFromFirestore();
        List<Report> filtered = new ArrayList<>();

        for (Report r : allReports) {
            boolean match = true;

            String rWorker = r.getWorkerId() != null ? r.getWorkerId().trim().toLowerCase() : "";
            String rStatus = r.getStatus() != null ? r.getStatus().trim().toLowerCase() : "";
            String rSite = r.getSite() != null ? r.getSite().trim().toLowerCase() : "";

            if (worker != null && !worker.isBlank() && !rWorker.equals(worker.trim().toLowerCase()))
                match = false;

            if (status != null && !status.isBlank() && !rStatus.equals(status.trim().toLowerCase()))
                match = false;

            if (siteList != null && !siteList.isEmpty()) {
                List<String> normalizedSites = siteList.stream()
                        .map(s -> s.trim().toLowerCase())
                        .collect(Collectors.toList());

                boolean foundMatch = false;
                for (String keyword : normalizedSites) {
                    if (rSite.contains(keyword)) {
                        foundMatch = true;
                        break;
                    }
                }

                if (!foundMatch) match = false;
            }

            if (startDate != null && r.getTimestamp() != null &&
                    r.getTimestamp().toSqlTimestamp().toInstant().isBefore(startDate.atStartOfDay().toInstant(ZoneOffset.UTC)))
                match = false;

            if (endDate != null && r.getTimestamp() != null &&
                    r.getTimestamp().toSqlTimestamp().toInstant().isAfter(endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)))
                match = false;

            if (match) filtered.add(r);
        }

        return filtered;
    }
}
