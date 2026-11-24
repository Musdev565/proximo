package cd.ucc.proximo.controller;

import cd.ucc.proximo.domain.entity.Product;
import cd.ucc.proximo.domain.entity.Shop;
import cd.ucc.proximo.dto.CreateProductRequest;
import cd.ucc.proximo.dto.ProductResponse;
import cd.ucc.proximo.exception.ResourceNotFoundException;
import cd.ucc.proximo.exception.UnauthorizedOperationException;
import cd.ucc.proximo.repository.ProductRepository;
import cd.ucc.proximo.repository.ShopRepository;
import cd.ucc.proximo.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    public ProductController(ProductRepository productRepository, ShopRepository shopRepository) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<List<ProductResponse>> getShopProducts(@PathVariable Long shopId) {
        List<Product> products = productRepository.findByShopIdAndActiveTrue(shopId);
        return ResponseEntity.ok(products.stream().map(this::mapToResponse).toList());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ProductResponse> createProduct(@AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody CreateProductRequest req) {
        Shop shop = shopRepository.findByOwnerId(principal.getId())
                .orElseThrow(() -> new UnauthorizedOperationException("Vous devez avoir une boutique pour créer un produit."));

        Product product = new Product(shop, req.name(), req.price(), req.photoUrl());
        Product saved = productRepository.save(product);

        return ResponseEntity.ok(mapToResponse(saved));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deactivateProduct(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé."));

        if (!product.getShop().getOwner().getId().equals(principal.getId())) {
            throw new UnauthorizedOperationException("Vous n'êtes pas propriétaire de ce produit.");
        }

        product.setActive(false);
        productRepository.save(product);

        return ResponseEntity.noContent().build();
    }

    private ProductResponse mapToResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getShop().getId(),
                p.getName(),
                p.getPrice(),
                p.getPhotoUrl(),
                p.isActive(),
                0,
                0,
                0
        );
    }
}
