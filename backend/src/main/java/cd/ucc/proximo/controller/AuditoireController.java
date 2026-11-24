package cd.ucc.proximo.controller;

import cd.ucc.proximo.domain.entity.Auditoire;
import cd.ucc.proximo.repository.AuditoireRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auditoires")
public class AuditoireController {

    private final AuditoireRepository auditoireRepository;

    public AuditoireController(AuditoireRepository auditoireRepository) {
        this.auditoireRepository = auditoireRepository;
    }

    @GetMapping
    public ResponseEntity<List<Auditoire>> getAllAuditoires() {
        return ResponseEntity.ok(auditoireRepository.findAllByOrderByLevelAscPositionOrderAsc());
    }
}
