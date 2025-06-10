package com.example.demo.controller;

import com.example.demo.model.Report;
import com.example.demo.service.MockAnalyzerService;
import com.example.demo.service.ReportService;
import com.example.demo.service.MockAnalyzerService.AnalysisResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/analyze")
public class ImageAnalysisController {

	private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";


    @Autowired
    private MockAnalyzerService analyzerService;

    @Autowired
    private ReportService reportService;

    @PostMapping
    public ResponseEntity<Report> analyzeImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("site") String site,
            @RequestParam("batchId") String batchId,
            @RequestParam("workerName") String workerName,
            @RequestParam("weather") String weather
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // 確保資料夾存在
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 儲存圖片
            File dest = new File(UPLOAD_DIR, file.getOriginalFilename());
            file.transferTo(dest);

            // 模擬分析
            AnalysisResult result = analyzerService.analyze(file.getOriginalFilename());

            // 儲存報告
            Report saved = reportService.saveReport(
                    file.getOriginalFilename(),
                    site,
                    batchId,
                    workerName,
                    weather,
                    result.getConfidence(),
                    result.getGrade()
            );

            // 回傳整份報告（JSON）
            return ResponseEntity.ok(saved);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}