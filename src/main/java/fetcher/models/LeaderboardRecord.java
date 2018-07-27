package fetcher.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Setter
@Table(name="moovie_leaderboard")
public class LeaderboardRecord {

    public LeaderboardRecord() {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "movie_name", nullable = false)
    private String movieName;

    @Column(name = "movie_id")
    private int movieID;

    @Column(name = "parse_count", nullable = false)
    private int parseCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "date_updated", nullable = false)
    private Date dateUpdated;

    @Column(name = "date_span", nullable = false)
    private long dateSpan;

    @Override
    public String toString() {
        return movieName + ":" + parseCount;
    }
}
