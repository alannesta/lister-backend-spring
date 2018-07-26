package fetcher.services;

import fetcher.analytics.AnalyticsUtil;
import fetcher.models.UserFavorite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class AnalyticsService {

    @Autowired
    AnalyticsUtil analyticsUtil;

    public List<UserFavorite> getUserFavoriteReport() {
        try {

            List<UserFavorite> reports = analyticsUtil.getUserFavoriteReport();
            //printResponse(response);
            return reports;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
