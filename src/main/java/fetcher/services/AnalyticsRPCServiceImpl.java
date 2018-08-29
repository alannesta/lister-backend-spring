package fetcher.services;

import fetcher.analytics.AnalyticsUtil;
import fetcher.models.Movie;
import fetcher.repositories.MovieRepository;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class AnalyticsRPCServiceImpl extends AnalyticsRPCServiceGrpc.AnalyticsRPCServiceImplBase {
    @Autowired
    private AnalyticsUtil analyticsUtil;
    @Autowired
    private MovieRepository movieRepository;

    public void getUserFavoriteReport(Empty request, StreamObserver<UserWatchRecords> responseObserver) {
        try {
            List<UserWatchRecord> records = analyticsUtil.getUserFavoriteReportRPC();

            Iterator<UserWatchRecord> it = records.iterator();

            while(it.hasNext()) {
                UserWatchRecord record = it.next();
                List<Movie> movies = movieRepository.findMoviesByTitle(record.getMovieName());
                if (movies != null && !movies.isEmpty()) {
                    record.newBuilderForType().setMovieId(movies.get(0).getId());
                } else {
                    it.remove();
                }
            }

            System.out.println("Sending response...");

            responseObserver.onNext( UserWatchRecords.newBuilder().addAllRecord(records).build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
