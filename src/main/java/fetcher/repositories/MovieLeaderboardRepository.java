package fetcher.repositories;

import fetcher.models.LeaderboardRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieLeaderboardRepository extends JpaRepository<LeaderboardRecord, Long> {}
