package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MockAnalyzerService {

    private final Random random = new Random();

    public AnalysisResult analyze(String imageName) {
        // 模擬分析分數（信心分數 60-100）
        double confidence = 60 + random.nextDouble() * 40;

        // 根據分數比 Grade（A–D）
        String grade;
        if (confidence >= 85) {
            grade = "A";
        } else if (confidence >= 75) {
            grade = "B";
        } else if (confidence >= 65) {
            grade = "C";
        } else {
            grade = "D";
        }

        return new AnalysisResult(imageName, confidence, grade);
    }

    // 定義一個內部靜態類做分析結果容器
    public static class AnalysisResult {
        private String imageName;
        private double confidence;
        private String grade;

        public AnalysisResult(String imageName, double confidence, String grade) {
            this.imageName = imageName;
            this.confidence = confidence;
            this.grade = grade;
        }

        public String getImageName() {
            return imageName;
        }

        public double getConfidence() {
            return confidence;
        }

        public String getGrade() {
            return grade;
        }
    }
}
