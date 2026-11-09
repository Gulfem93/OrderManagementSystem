package com.ordermanagementsystem.dev.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.ordermanagementsystem.dev.data.UsersData;
import com.ordermanagementsystem.dev.service.ReportService;
import com.ordermanagementsystem.dev.service.UserService;

@Controller
public class ReportsController {
    private static final String ROLE_OWNER = "ROLE_OWNER";
    private static final long LOW_STOCK_THRESHOLD = 5; // Products sayfasıyla aynı

    private final ReportService reportService;
    private final UserService userService;

    public ReportsController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }

    // Customer: satın alma raporu. Owner: şirketinin satış raporu.
    @GetMapping("/reports")
    public String getReportsPage(Authentication auth, Model model) {
        boolean owner = auth.getAuthorities().stream()
                .anyMatch(a -> ROLE_OWNER.equals(a.getAuthority()));
        UsersData userData = userService.getUserData(auth.getName());

        model.addAttribute("userData", userData);
        model.addAttribute("isOwner", owner);
        if (owner) {
            String company = userData.getCompanyName();
            model.addAttribute("report", reportService.ownerReport(company));
            model.addAttribute("insights", reportService.ownerInsights(company, LOW_STOCK_THRESHOLD));
            model.addAttribute("orderStats", reportService.orderStats(company));
        } else {
            model.addAttribute("report", reportService.customerReport(auth.getName()));
        }
        return "reports";
    }
}
