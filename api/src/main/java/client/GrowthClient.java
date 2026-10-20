package client;

import com.interview.DTO.GrowthStatsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 用户成长服务 Feign 客户端（供 user-service 查询统计）。
 */
@FeignClient(value = "user-growth-service")
public interface GrowthClient {

    /** 当前用户成长统计（技能维度数 + 报告数） */
    @GetMapping("/growth/stats")
    GrowthStatsDTO getStats();
}
