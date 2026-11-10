package com.idamart.controller;

import com.idamart.dto.AdminStatsResponse;
import com.idamart.service.AdminStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET /api/admin/stats (ADMIN only, like everything under /api/admin). */
@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    @GetMapping
    public AdminStatsResponse stats() {
        return adminStatsService.getStats();
    }
}
