package fetcher.controllers;

import fetcher.models.Movie;
import fetcher.services.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieService movieService;

    private static final String END_EPOCH = Long.toString(Instant.now().getEpochSecond());

    @GetMapping("")
    public List<Movie> getAllMovies(@RequestParam(name = "since", defaultValue = "0") Long startEpoch,
                                    @RequestParam(name = "to", defaultValue = "2299999999") Long endEpoch,
                                    @RequestParam(name = "liked", defaultValue = "false") Boolean likedFilter,
                                    @RequestParam(name = "count", defaultValue = "20") Integer count,
                                    @RequestParam(name = "query", required = false) String query
    ) {
        return movieService.findAllMovies(startEpoch, endEpoch, likedFilter, count);
    }

    @GetMapping("/quick-test")
    public void test() {
        movieService.quickTest();
    }
}
