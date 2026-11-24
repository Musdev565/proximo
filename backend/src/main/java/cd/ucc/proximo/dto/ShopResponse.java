package cd.ucc.proximo.dto;

import java.util.List;

public record ShopResponse(
        Long id,
        Long ownerId,
        String ownerName,
        String name,
        String description,
        String imageUrl,
        boolean active,
        boolean presentToday,
        List<ProductResponse> products
) {
}
