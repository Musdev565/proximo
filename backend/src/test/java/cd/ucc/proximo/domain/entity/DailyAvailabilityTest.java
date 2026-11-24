package cd.ucc.proximo.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DailyAvailabilityTest {

    @Test
    @DisplayName("Le stock disponible doit être égal à stock - réserve")
    void shouldCalculateAvailableStockCorrectly() {
        Product product = new Product(null, "Sandwich", BigDecimal.valueOf(3500), null);
        DailyAvailability availability = new DailyAvailability(product, LocalDate.now(), 10);

        assertEquals(10, availability.getAvailableQuantity());
        assertTrue(availability.canReserve(5));

        availability.reserve(5);
        assertEquals(5, availability.getAvailableQuantity());
        assertEquals(10, availability.getStockQuantity());
        assertEquals(5, availability.getReservedQuantity());

        // Confirmation du paiement : le stock physique ET la réserve diminuent
        availability.confirmPayment(3);
        assertEquals(7, availability.getStockQuantity());
        assertEquals(2, availability.getReservedQuantity());
        assertEquals(5, availability.getAvailableQuantity());

        // Annulation d'une réservation : seule la réserve diminue
        availability.releaseReservation(2);
        assertEquals(7, availability.getStockQuantity());
        assertEquals(0, availability.getReservedQuantity());
        assertEquals(7, availability.getAvailableQuantity());
    }

    @Test
    @DisplayName("Doit refuser une réservation si le stock disponible est insuffisant")
    void shouldThrowExceptionWhenReservingMoreThanAvailable() {
        Product product = new Product(null, "Jus", BigDecimal.valueOf(2000), null);
        DailyAvailability availability = new DailyAvailability(product, LocalDate.now(), 3);

        availability.reserve(3);
        assertEquals(0, availability.getAvailableQuantity());

        assertThrows(IllegalStateException.class, () -> availability.reserve(1));
    }
}
