package client.fallback;

import client.ResumeClient;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ResumeClientFallback implements FallbackFactory<ResumeClient> {
    @Override
    public ResumeClient create(Throwable cause) {
        return null;
    }
}
