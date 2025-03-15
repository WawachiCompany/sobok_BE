package com.chihuahua.sobok.statistics.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/report")
    public ResponseEntity<?> getReport(@RequestParam String yearMonth) {
        return reportService.getReport(yearMonth);
    }
}
