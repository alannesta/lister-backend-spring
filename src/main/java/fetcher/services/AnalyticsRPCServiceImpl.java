package fetcher.services;

import fetcher.analytics.AnalyticsUtil;
import fetcher.models.Movie;
import fetcher.repositories.MovieRepository;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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

    @Override
    public void getUserFavoriteReport(Empty request, StreamObserver<UserWatchRecords> responseObserver) {
        try {
            List<UserWatchRecord> records;

            records = cast(redisTemplate.opsForValue().get("parse-report-cache"));
            if (records != null ) {
                log.debug("cache hit: ", records);
                responseObserver.onNext(UserWatchRecords.newBuilder().addAllRecord(records).build());
                responseObserver.onCompleted();
            } else {
                log.debug("cache does not exist, query analytics API");
                records = analyticsUtil.getUserFavoriteReportRPC();
                List<UserWatchRecord> results = new ArrayList<>();

                Iterator<UserWatchRecord> it = records.iterator();

                while(it.hasNext()) {
                    UserWatchRecord record = it.next();
                    List<Movie> movies = movieRepository.findMoviesByTitle(record.getMovieName());
                    if (movies != null && !movies.isEmpty()) {
                        results.add(record.toBuilder().setMovieId(movies.get(0).getId()).build());
                    }
                }

                responseObserver.onNext(UserWatchRecords.newBuilder().addAllRecord(results).build());
                responseObserver.onCompleted();

                // cache in redis
                redisTemplate.opsForValue().set("parse-report-cache", results);
                redisTemplate.expire("parse-report-cache", 30, TimeUnit.SECONDS);
            }



            //try {
            //    FileOutputStream fileOut =
            //            new FileOutputStream("/Users/alancao/git/fetcher-spring-boot/test.ser");
            //    ObjectOutputStream out = new ObjectOutputStream(fileOut);
            //    out.writeObject(records);
            //    out.close();
            //    fileOut.close();
            //    System.out.println("Serialized data is saved in /tmp/employee.ser");
            //} catch (IOException i) {
            //    i.printStackTrace();
            //}

            //try {
            //    log.info("reading from file cache");
            //    FileInputStream fileIn = new FileInputStream("/Users/alancao/git/fetcher-spring-boot/test.ser");
            //    ObjectInputStream in = new ObjectInputStream(fileIn);
            //    records = (List<UserWatchRecord>) in.readObject();
            //    in.close();
            //    fileIn.close();
            //} catch (IOException i) {
            //    i.printStackTrace();
            //    return;
            //} catch (ClassNotFoundException c) {
            //    System.out.println("class not found exception");
            //    c.printStackTrace();
            //    return;
            //}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends List<?>> T cast(Object obj) {
        return (T) obj;
    }
}
