package com.example.demo.controller;

import com.example.demo.model.Report;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("/reports")
public class ReportExportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/export/txt/{id}")

    public ResponseEntity<InputStreamResource> exportTxt(@PathVariable String id) {
        Report report = reportService.getReportById(id);
        if (report == null) return ResponseEntity.notFound().build();

        StringBuilder sb = new StringBuilder();
        sb.append("==========================\n");
        sb.append("       REPORT SUMMARY     \n");
        sb.append("==========================\n\n");

        sb.append("🖼️  Image Name  : ").append(report.getImageName()).append("\n");
        sb.append("📍  Site        : ").append(report.getSite()).append("\n");
        sb.append("👷  Worker ID   : ").append(report.getWorkerId()).append("\n");
        sb.append("🌦️  Weather     : ").append(report.getWeather()).append("\n");
        sb.append("📆  Timestamp   : ").append(report.getTimestamp() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(report.getTimestamp().toDate()) : "").append("\n");
        sb.append("📝  Status      : ").append(report.getStatus()).append("\n");
        sb.append("📊  Confidence  : ").append(report.getConfidence()).append("\n");
        sb.append("💬  Comment     : ").append(report.getComment() != null ? report.getComment() : "").append("\n");
        sb.append("🔗  Image URL   : ").append(report.getImageUrl()).append("\n");

        sb.append("\n==========================\n");


        ByteArrayInputStream bis = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=report_" + id + ".txt");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bis.available())
                .contentType(MediaType.TEXT_PLAIN)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/export/csv/{id}")
    public ResponseEntity<InputStreamResource> exportCsv(@PathVariable String id) {
        Report report = reportService.getReportById(id);
        if (report == null) return ResponseEntity.notFound().build();

        StringBuilder sb = new StringBuilder();
        // Header
        sb.append("imageName,site,workerId,weather,timestamp,status,confidence,comment,imageUrl\n");

        // Body
        sb.append("\"").append(report.getImageName()).append("\",")
          .append("\"").append(report.getSite() != null ? report.getSite() : "").append("\",")
          .append("\"").append(report.getWorkerId() != null ? report.getWorkerId() : "").append("\",")
          .append("\"").append(report.getWeather() != null ? report.getWeather() : "").append("\",")
          .append("\"").append(report.getTimestamp() != null
                         ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(report.getTimestamp().toDate())
                         : "").append("\",")
          .append("\"").append(report.getStatus() != null ? report.getStatus() : "").append("\",")
          .append(report.getConfidence()).append(",")  // numeric, 無需加雙引號
          .append("\"").append(report.getComment() != null ? report.getComment().replaceAll("\"", "'") : "").append("\",")
          .append("\"").append(report.getImageUrl() != null ? report.getImageUrl() : "").append("\"\n");

        ByteArrayInputStream bis = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=report_" + id + ".csv");

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(bis.available())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new InputStreamResource(bis));
    }

}
