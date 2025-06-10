// UploadPageController.java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UploadPageController {

    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload";
    }
}
