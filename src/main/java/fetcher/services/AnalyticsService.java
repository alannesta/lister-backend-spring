package fetcher.services;

import fetcher.analytics.AnalyticsUtil;
import fetcher.models.UserFavorite;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class AnalyticsService {

    @Autowired
    AnalyticsUtil analyticsUtil;

    // @Scheduled(fixedRate = 5000)
    @Scheduled(cron = "0 12 3 */3 * *")
    public List<UserFavorite> getUserFavoriteReport() {
        try {

            List<UserFavorite> reports = analyticsUtil.getUserFavoriteReport();
            //printResponse(response);
            for (UserFavorite userFavorite : reports) {
                System.out.println(userFavorite.getMovieName());
            }
            return reports;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
