package fetcher.services;

import fetcher.analytics.AnalyticsUtil;
import fetcher.models.LeaderboardRecord;
import fetcher.models.Movie;
import fetcher.repositories.MovieLeaderboardRepository;
import fetcher.repositories.MovieRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AnalyticsService {

    @Autowired
    private AnalyticsUtil analyticsUtil;
    @Autowired
    private MovieLeaderboardRepository leaderboardRepository;
    @Autowired
    private MovieRepository movieRepository;

    //@Scheduled(fixedRate = 100000)
    //@Scheduled(cron = "0 12 3 * * *")
    @Cacheable(cacheNames = "analytics-parse-report")
    public List<LeaderboardRecord> getUserFavoriteReport() {
        try {
            log.info("not hitting cache");
            List<LeaderboardRecord> reports = analyticsUtil.getUserFavoriteReport();

            Iterator<LeaderboardRecord> it = reports.iterator();

            while(it.hasNext()) {
                LeaderboardRecord record = it.next();
                List<Movie> movies = movieRepository.findMoviesByTitle(record.getMovieName());
                if (movies != null && !movies.isEmpty()) {
                    record.setMovieID(movies.get(0).getId());
                } else {
                    it.remove();
                }
            }

            System.out.println("Saving reports...");
            //leaderboardRepository.saveAll(reports);
            return reports;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }
}
