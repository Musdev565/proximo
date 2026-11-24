package cd.ucc.proximo.config;

import cd.ucc.proximo.domain.entity.Auditoire;
import cd.ucc.proximo.repository.AuditoireRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final AuditoireRepository auditoireRepository;

    public DataInitializer(AuditoireRepository auditoireRepository) {
        this.auditoireRepository = auditoireRepository;
    }

    @Override
    public void run(String... args) {
        if (auditoireRepository.count() == 0) {
            log.info("Initialisation des auditoires UCC par défaut...");
            List<Auditoire> auditoires = List.of(
                    new Auditoire("N1-A01", "Auditoire Jean-Paul II", "L1 Sciences Informatiques", 1, 1),
                    new Auditoire("N1-A02", "Auditoire Cardinal Malula", "L1 Droit", 1, 2),
                    new Auditoire("N1-A03", "Auditoire Mgr Tshibangu", "L1 Sciences Économiques", 1, 3),
                    new Auditoire("N2-A01", "Auditoire Thomas d'Aquin", "L2 Génie Logiciel", 2, 1),
                    new Auditoire("N2-A02", "Auditoire Saint Augustin", "L2 Réseaux & Télécoms", 2, 2),
                    new Auditoire("N2-A03", "Auditoire Mère Teresa", "L2 Droit Privé", 2, 3),
                    new Auditoire("N3-A01", "Auditoire Benoît XVI", "L3 Génie Logiciel", 3, 1),
                    new Auditoire("N3-A02", "Auditoire François", "Master 1 IA & Big Data", 3, 2)
            );
            auditoireRepository.saveAll(auditoires);
            log.info("{} auditoires UCC insérés avec succès.", auditoires.size());
        }
    }
}
