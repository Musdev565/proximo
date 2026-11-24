package cd.ucc.proximo.repository;

import cd.ucc.proximo.domain.entity.Auditoire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuditoireRepository extends JpaRepository<Auditoire, Long> {
    Optional<Auditoire> findByCode(String code);
    List<Auditoire> findAllByOrderByLevelAscPositionOrderAsc();
}
