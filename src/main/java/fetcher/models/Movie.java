package fetcher.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="videos")
public class Movie {

    @Id
    @GeneratedValue
    private int id;

    private String title;
    private String url;
    private String author;
    private String thumbnail;

    private int comment;

    @Column(name="view_count")
    private int viewCount;

    private int favourite;

    @Column(name="date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(name="last_update")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdate;

    @Column(name="last_process")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastProcess;
}
