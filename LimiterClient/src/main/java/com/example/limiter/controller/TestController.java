package com.example.limiter.controller;

import com.example.limiter.limiter.DistributedQPSLimiter;
import com.example.limiter.netty.ClientChannel;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * @author feng xud
 */
@RestController
public class TestController {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TestController.class);


    @DistributedQPSLimiter(qps = 200, limit = 10)
    @GetMapping("/test")
    public void test() {
        if (ClientChannel.tryAccess()) {
            log.info("限流通过");
            return;
        }
        log.warn("限流未通过");
    }
}
