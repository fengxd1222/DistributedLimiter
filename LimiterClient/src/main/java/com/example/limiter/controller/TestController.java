package com.example.limiter.controller;

import com.example.limiter.limiter.DistributedQPSLimiter;
import com.example.limiter.netty.ClientChannel;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author feng xud
 */
@RestController
public class TestController {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(TestController.class);


    @DistributedQPSLimiter(qps = 200, limit = 10)
    @GetMapping("/test")
    public Map<String,Object> test(@RequestParam String token) {
        System.out.println(token);
        //for test
        if (ClientChannel.tryAccess(token)) {
            log.info("限流通过");
            return new HashMap<String,Object>(1){{
                put("Message","限流通过");
            }};
        }
        log.warn("限流未通过");
        return new HashMap<String,Object>(1){{
            put("Message","限流未通过");
        }};
    }
}
