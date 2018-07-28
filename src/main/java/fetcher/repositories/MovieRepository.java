package fetcher.repositories;

import fetcher.models.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor {
    public Movie findMovieByTitle(String title);

    public List<Movie> findMoviesByTitle(String title);
}
