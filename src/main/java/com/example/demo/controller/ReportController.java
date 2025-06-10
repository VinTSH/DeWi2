package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.model.Report;
import com.example.demo.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDate;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;


import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
@Controller
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/reports")
    public String viewReports(
            @RequestParam(required = false) String worker,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String site,  // 👈 自由輸入，多個 site 用逗號分隔
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpSession session,
            Model model) {

        List<String> siteList = null;
        if (site != null && !site.isBlank()) {
            // 轉換成 List<String> 並清除空白（e.g. Site A, Site B）
            siteList = Arrays.stream(site.split(","))
                             .map(String::trim)
                             .filter(s -> !s.isEmpty())
                             .collect(Collectors.toList());
        }

        // 🔧 傳入 List 或 null，請確保你的 reportService 會處理 null / empty case
        List<Report> reports = reportService.searchReports(worker, status, siteList, startDate, endDate);
        model.addAttribute("reports", reports);
        model.addAttribute("paramSite", site);  // 傳返原本 site 字串俾前端回填

        if (session.getAttribute("loggedInUser") == null) model.addAttribute("notLoggedIn", true);
        return "reports";
    }




    @GetMapping("/my-reports")
    public String showMyReports(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"worker".equals(user.getRole())) {
            return "error/403";
        }
        model.addAttribute("reports", reportService.getReportsByWorker(user.getUsername()));
        return "my-reports";
    }

    @GetMapping("/reports/add")
    public String showAddReportPage(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"worker".equals(user.getRole())) {
            return "error/403";
        }
        return "add-report";
    }

    

    
    @PostMapping("/reports/save")
    public String saveReport(@RequestParam("imageFile") MultipartFile imageFile,
                             @RequestParam String site,
                             @RequestParam String batchId,
                             @RequestParam String weather,
                             HttpSession session) throws IOException {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null || !"worker".equals(user.getRole())) {
            return "error/403";
        }

        String imageName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads";
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        File dest = new File(uploadDir, imageName);
        imageFile.transferTo(dest);
        System.out.println("✅ Image saved to: " + dest.getAbsolutePath());

        double confidence = 0.87;
        String grade = confidence >= 0.9 ? "A" : confidence >= 0.75 ? "B" : "C";

        reportService.saveReport(imageName, site, batchId, user.getUsername(), weather, confidence, grade);
        return "redirect:/my-reports";
    }

    @PostMapping("/reports/{id}/approve")
    public String approveReport(@PathVariable("id") String id,
                                @RequestParam("comment") String comment,
                                HttpSession session) {
        reportService.updateReportStatus(id, "approved", comment);
        return "redirect:/reports";
    }

    @PostMapping("/reports/{id}/reject")
    public String rejectReport(@PathVariable("id") String id,
                               @RequestParam("comment") String comment,
                               HttpSession session) {
        reportService.updateReportStatus(id, "rejected", comment);
        return "redirect:/reports";
    }
}
