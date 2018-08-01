package fetcher.controllers;

import fetcher.services.AnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Slf4j
@RestController
@RequestMapping("/analytics")
public class AnalyticsControlelr {
    @Autowired
    private AnalyticsService analyticsService;

    @Value( "${spring.datasource.url}" )
    private String jdbcUrl;

    @Autowired
    private Environment env;

    @GetMapping("/debug-info")
    public void debug() {
        log.info("env: " + Arrays.asList(env.getActiveProfiles()).toString());
        log.info("db url: " + jdbcUrl);
        //analyticsService.getUserFavoriteReport();
    }

    @GetMapping("/fetch")
    public void fetch() {
        analyticsService.getUserFavoriteReport();
    }
}
