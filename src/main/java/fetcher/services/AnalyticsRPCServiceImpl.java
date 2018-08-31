package fetcher.services;

import fetcher.analytics.AnalyticsUtil;
import fetcher.models.Movie;
import fetcher.repositories.MovieRepository;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class AnalyticsRPCServiceImpl extends AnalyticsRPCServiceGrpc.AnalyticsRPCServiceImplBase {
    @Autowired
    private AnalyticsUtil analyticsUtil;
    @Autowired
    private MovieRepository movieRepository;

    @Override
    //@Cacheable(cacheNames = "analytics-parse-report-lite", key="#request.hashCode()")
    public void getUserFavoriteReport(Empty request, StreamObserver<UserWatchRecords> responseObserver) {
        try {
            log.info("not hitting cache for analytics report");
            List<UserWatchRecord> records = analyticsUtil.getUserFavoriteReportRPC();
            List<UserWatchRecord> results = new ArrayList<>();

            Iterator<UserWatchRecord> it = records.iterator();

            while(it.hasNext()) {
                UserWatchRecord record = it.next();
                List<Movie> movies = movieRepository.findMoviesByTitle(record.getMovieName());
                if (movies != null && !movies.isEmpty()) {
                    results.add(record.toBuilder().setMovieId(movies.get(0).getId()).build());
                }
            }

            System.out.println("Sending response...");

            responseObserver.onNext(UserWatchRecords.newBuilder().addAllRecord(results).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
