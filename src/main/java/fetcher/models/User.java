package fetcher.models;

import fetcher.models.enums.Role;
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
@Table(name="users")
public class User {
    @GeneratedValue
    @Id
    private int id;

    private String username;
    private String password;
    private String salt;
    private String uuid;    // intially generated manually, no user registration process

    @Column(name="date_created")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreated;

    @Column(columnDefinition = "enum('ADMIM','GUEST', 'PAID')")
    @Enumerated(EnumType.STRING)
    private Role role;
}
