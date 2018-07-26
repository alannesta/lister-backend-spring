package fetcher.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Builder
@AllArgsConstructor
@Getter
public class UserFavorite {
    private int id;

    private String movieName;
    private int movieID;
    private int parseCount;
    private Date dateUpdated;
    private long dateSpan;
}
