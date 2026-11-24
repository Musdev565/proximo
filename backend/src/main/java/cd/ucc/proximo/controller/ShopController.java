package cd.ucc.proximo.controller;

import cd.ucc.proximo.domain.entity.*;
import cd.ucc.proximo.domain.enums.UserRole;
import cd.ucc.proximo.dto.CreateShopRequest;
import cd.ucc.proximo.dto.DailyStockDeclarationRequest;
import cd.ucc.proximo.dto.ProductResponse;
import cd.ucc.proximo.dto.ShopResponse;
import cd.ucc.proximo.exception.ResourceNotFoundException;
import cd.ucc.proximo.exception.UnauthorizedOperationException;
import cd.ucc.proximo.repository.*;
import cd.ucc.proximo.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final DailyPresenceRepository presenceRepository;
    private final DailyAvailabilityRepository availabilityRepository;

    @Value("${proximo.fuseau:Africa/Kinshasa}")
    private String timezone;

    public ShopController(ShopRepository shopRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository,
                          DailyPresenceRepository presenceRepository,
                          DailyAvailabilityRepository availabilityRepository) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.presenceRepository = presenceRepository;
        this.availabilityRepository = availabilityRepository;
    }

    private LocalDate getToday() {
        return LocalDate.now(ZoneId.of(timezone));
    }

    @GetMapping("/today")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ShopResponse>> getPresentShopsToday() {
        LocalDate today = getToday();
        List<DailyPresence> presences = presenceRepository.findByDateAndPresentTrue(today);

        List<ShopResponse> result = new ArrayList<>();
        for (DailyPresence presence : presences) {
            Shop shop = presence.getShop();
            if (shop.isActive()) {
                result.add(mapToShopResponse(shop, true, today));
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/my-shop")
    @Transactional(readOnly = true)
    public ResponseEntity<ShopResponse> getMyShop(@AuthenticationPrincipal UserPrincipal principal) {
        Shop shop = shopRepository.findByOwnerId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vous n'avez pas encore créé de boutique."));

        LocalDate today = getToday();
        boolean presentToday = presenceRepository.findByShopIdAndDate(shop.getId(), today)
                .map(DailyPresence::isPresent)
                .orElse(false);

        return ResponseEntity.ok(mapToShopResponse(shop, presentToday, today));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ShopResponse> createShop(@AuthenticationPrincipal UserPrincipal principal,
                                                   @Valid @RequestBody CreateShopRequest req) {
        if (shopRepository.findByOwnerId(principal.getId()).isPresent()) {
            throw new IllegalStateException("Vous possédez déjà une boutique enregistrée.");
        }

        User owner = userRepository.findById(principal.getId()).orElseThrow();
        owner.setRole(UserRole.VENDOR);
        userRepository.save(owner);

        Shop shop = new Shop(owner, req.name(), req.description(), req.imageUrl());
        Shop saved = shopRepository.save(shop);

        return ResponseEntity.ok(mapToShopResponse(saved, false, getToday()));
    }

    @PostMapping("/presence")
    @Transactional
    public ResponseEntity<Boolean> togglePresenceToday(@AuthenticationPrincipal UserPrincipal principal,
                                                       @RequestParam(defaultValue = "true") boolean present) {
        Shop shop = shopRepository.findByOwnerId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Boutique introuvable."));

        LocalDate today = getToday();
        DailyPresence presence = presenceRepository.findByShopIdAndDate(shop.getId(), today)
                .orElse(new DailyPresence(shop, today, present));

        presence.setPresent(present);
        presenceRepository.save(presence);

        return ResponseEntity.ok(present);
    }

    @PostMapping("/stock")
    @Transactional
    public ResponseEntity<List<ProductResponse>> declareStock(@AuthenticationPrincipal UserPrincipal principal,
                                                             @Valid @RequestBody List<DailyStockDeclarationRequest> declarations) {
        Shop shop = shopRepository.findByOwnerId(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Boutique introuvable."));

        LocalDate today = getToday();
        List<ProductResponse> updatedList = new ArrayList<>();

        for (DailyStockDeclarationRequest dec : declarations) {
            Product product = productRepository.findById(dec.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit " + dec.productId() + " introuvable."));

            if (!product.getShop().getId().equals(shop.getId())) {
                throw new UnauthorizedOperationException("Le produit n'appartient pas à votre boutique.");
            }

            DailyAvailability availability = availabilityRepository.findByProductIdAndDate(product.getId(), today)
                    .orElse(new DailyAvailability(product, today, dec.stockQuantity()));

            availability.setStockQuantity(dec.stockQuantity());
            availabilityRepository.save(availability);

            updatedList.add(mapToProductResponse(product, availability));
        }

        return ResponseEntity.ok(updatedList);
    }

    private ShopResponse mapToShopResponse(Shop shop, boolean presentToday, LocalDate date) {
        List<Product> products = productRepository.findByShopIdAndActiveTrue(shop.getId());
        List<ProductResponse> productResponses = products.stream()
                .map(p -> {
                    DailyAvailability avail = availabilityRepository.findByProductIdAndDate(p.getId(), date).orElse(null);
                    return mapToProductResponse(p, avail);
                })
                .toList();

        return new ShopResponse(
                shop.getId(),
                shop.getOwner().getId(),
                shop.getOwner().getFullName(),
                shop.getName(),
                shop.getDescription(),
                shop.getImageUrl(),
                shop.isActive(),
                presentToday,
                productResponses
        );
    }

    private ProductResponse mapToProductResponse(Product product, DailyAvailability avail) {
        int available = avail != null ? avail.getAvailableQuantity() : 0;
        int stock = avail != null ? avail.getStockQuantity() : 0;
        int reserved = avail != null ? avail.getReservedQuantity() : 0;

        return new ProductResponse(
                product.getId(),
                product.getShop().getId(),
                product.getName(),
                product.getPrice(),
                product.getPhotoUrl(),
                product.isActive(),
                available,
                stock,
                reserved
        );
    }
}
