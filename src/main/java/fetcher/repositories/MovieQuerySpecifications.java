package fetcher.repositories;


import fetcher.models.Movie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public class MovieQuerySpecifications {
    public static Specification<Movie> filterSpec(Date startDate, Date endDate, Boolean likedFilter) {
        return new Specification<Movie>() {
            public Predicate toPredicate(Root<Movie> root, CriteriaQuery<?> query,
                                         CriteriaBuilder builder) {

                List<Predicate> predicates = new ArrayList<Predicate>();

                if (startDate != null) {
                    predicates.add(builder.greaterThanOrEqualTo(root.get("dateCreated"), startDate));
                }

                if (endDate != null) {
                    predicates.add(builder.lessThanOrEqualTo(root.get("dateCreated"), endDate));
                }

                return builder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
