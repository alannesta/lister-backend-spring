package fetcher.services;

import fetcher.analytics.AnalyticsUtil;
import fetcher.models.Movie;
import fetcher.repositories.MovieRepository;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AnalyticsRPCServiceImpl extends AnalyticsRPCServiceGrpc.AnalyticsRPCServiceImplBase {
    @Autowired
    private AnalyticsUtil analyticsUtil;
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final int TOP_K = 5;
    private static final int PARSE_COEFFICIENT = 1;
    private static final int VIEW_COEFFICIENT = 2;

    @Override
    public void getUserFavoriteReport(Empty request, StreamObserver<UserWatchRecords> responseObserver) {
        Query requestV2 = Query.newBuilder().setDateSpan(30L).build();
        getUserFavoriteReportV2(requestV2, responseObserver);
    }


    @Override
    public void getUserFavoriteReportV2(Query request, StreamObserver<UserWatchRecords> responseObserver) {
        try {
            List<UserWatchRecord> records;
            Long dateSpan = request.getDateSpan();
            String cacheKey = "parse-report-cache::" + dateSpan.toString();

            records = cast(redisTemplate.opsForValue().get(cacheKey));
            if (records != null) {
                log.debug("cache hit: {}", records);
                responseObserver.onNext(UserWatchRecords.newBuilder().addAllRecord(records).build());
                responseObserver.onCompleted();
            } else {
                log.debug("cache does not exist, query analytics API");
                records = analyticsUtil.getUserFavoriteReportRPC(request.getDateSpan());
                List<UserWatchRecord> results = new ArrayList<>();

                Iterator<UserWatchRecord> it = records.iterator();

                while (it.hasNext()) {
                    UserWatchRecord record = it.next();
                    List<Movie> movies = movieRepository.findMoviesByTitle(record.getMovieName());
                    if (movies != null && !movies.isEmpty()) {
                        results.add(record.toBuilder().setMovieId(movies.get(0).getId())
                                .setThumbnail(movies.get(0).getThumbnail()).build());
                    }
                }
                log.debug("results : {}", results);

                // sort the result in descending order and return top 5
                results.sort(new Comparator<UserWatchRecord>() {
                    @Override
                    public int compare(UserWatchRecord o1, UserWatchRecord o2) {
                        return o2.getParseCount() * PARSE_COEFFICIENT + o2.getViewCount() * VIEW_COEFFICIENT -
                                o1.getParseCount() * PARSE_COEFFICIENT + o1.getViewCount() * VIEW_COEFFICIENT;
                    }
                });

                int limit = results.size() > TOP_K ? TOP_K : results.size();

                responseObserver.onNext(UserWatchRecords.newBuilder().addAllRecord(results.subList(0, limit)).build());
                responseObserver.onCompleted();

                // cache in redis
                redisTemplate.opsForValue().set(cacheKey, results);
                redisTemplate.expire(cacheKey, 30, TimeUnit.SECONDS);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    private static <T extends List<?>> T cast(Object obj) {
        return (T) obj;
    }
}
