package client;

import client.fallback.ResumeClientFallback;
import com.interview.DTO.ParsedResumeData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "resume-service",fallbackFactory = ResumeClientFallback.class)
public interface ResumeClient {

    @PutMapping(
            value = "/resumes/{resumeId}/updateStatus", // 👈 路径必须包含 {resumeId}
            consumes = "application/json"               // 👈 显式声明 JSON
    )
    void updateStatus(
            @PathVariable("resumeId") Long resumeId,
            @RequestParam("userId") Long userId,
            @RequestBody ParsedResumeData data
    );
}
