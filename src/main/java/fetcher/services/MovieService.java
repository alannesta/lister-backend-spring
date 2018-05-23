package fetcher.services;

import fetcher.models.Movie;
import fetcher.repositories.MovieQuerySpecifications;
import fetcher.repositories.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class MovieService {

    @Autowired
    private MovieRepository movieRepo;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public List<Movie> findAllMovies(Long startEpoch, Long endEpoch, Boolean likedFilter, Integer count) {
        Pageable page = PageRequest.of(0, count);
        Date startDate = new Date(startEpoch);
        Date endDate = new Date(endEpoch);

        return movieRepo.findAll(MovieQuerySpecifications.filterSpec(startDate, endDate, likedFilter), page).getContent();
    }

    public void quickTest() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        jdbcTemplate.query("SELECT date_created FROM videos WHERE id = 1026", new RowCallbackHandler() {
            @Override
            public void processRow(java.sql.ResultSet res) throws SQLException {
                try {
                    Date date = formatter.parse(res.getString("date_created"));
                    log.info(date.toString());
                } catch(Exception e) {

                }
            }
        });
    }
}
