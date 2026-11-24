package cd.ucc.proximo.repository;

import cd.ucc.proximo.domain.entity.DailyAvailability;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyAvailabilityRepository extends JpaRepository<DailyAvailability, Long> {

    Optional<DailyAvailability> findByProductIdAndDate(Long productId, LocalDate date);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DailyAvailability d WHERE d.product.id = :productId AND d.date = :date")
    Optional<DailyAvailability> findByProductIdAndDateForUpdate(@Param("productId") Long productId, @Param("date") LocalDate date);
}
