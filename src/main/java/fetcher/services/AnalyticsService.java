package fetcher.services;

import fetcher.analytics.AnalyticsUtil;
import fetcher.models.LeaderboardRecord;
import fetcher.models.Movie;
import fetcher.repositories.MovieLeaderboardRepository;
import fetcher.repositories.MovieRepository;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collections;
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

    @Scheduled(fixedRate = 100000)
    //@Scheduled(cron = "0 12 3 */3 * *")
    public List<LeaderboardRecord> getUserFavoriteReport() {
        try {

            List<LeaderboardRecord> reports = analyticsUtil.getUserFavoriteReport();
            //for (LeaderboardRecord leaderboardRecord : reports) {
            //    System.out.println(leaderboardRecord.getMovieName());
            //    Movie movie = movieRepository.findMovieByTitle(leaderboardRecord.getMovieName());
            //    if (movie != null) {
            //        System.out.println(movie.getId());
            //        leaderboardRecord.setId(movie.getId());
            //    }
            //}

            reports = reports.stream().map(leaderboardRecord -> {
                Movie movie = movieRepository.findMovieByTitle(leaderboardRecord.getMovieName());
                if (movie != null) {
                    leaderboardRecord.setId(movie.getId());
                    return leaderboardRecord;
                }
                return null;
            })
                    .filter(leaderboardRecord -> leaderboardRecord != null)
                    .collect(Collectors.toList());

            System.out.println("Saving reports...");
            leaderboardRepository.saveAll(reports);
            return reports;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    //@Scheduled(fixedRate = 10000)
    public void quickTest() {
        System.out.println("records: ");
        System.out.println(leaderboardRepository.findAll());
    }
}
