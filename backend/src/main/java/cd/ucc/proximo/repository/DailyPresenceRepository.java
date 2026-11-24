package cd.ucc.proximo.repository;

import cd.ucc.proximo.domain.entity.DailyPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPresenceRepository extends JpaRepository<DailyPresence, Long> {
    Optional<DailyPresence> findByShopIdAndDate(Long shopId, LocalDate date);
    List<DailyPresence> findByDateAndPresentTrue(LocalDate date);
}
