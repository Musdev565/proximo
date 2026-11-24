package cd.ucc.proximo.dto;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String fullName,
        String email,
        String role,
        Long defaultAuditoireId,
        String defaultAuditoireCode,
        Long shopId,
        String shopName
) {
    public static AuthResponse of(String token, Long userId, String fullName, String email, String role, Long defaultAuditoireId, String defaultAuditoireCode, Long shopId, String shopName) {
        return new AuthResponse(token, "Bearer", userId, fullName, email, role, defaultAuditoireId, defaultAuditoireCode, shopId, shopName);
    }
}
