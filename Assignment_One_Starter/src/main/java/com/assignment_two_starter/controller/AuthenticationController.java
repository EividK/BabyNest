package com.assignment_two_starter.controller;

import com.assignment_two_starter.Authentication.AuthenticationRequest;
import com.assignment_two_starter.Authentication.AuthenticationResponse;
import com.assignment_two_starter.config.JwtUtil;
import com.assignment_two_starter.model.Customer;
import com.assignment_two_starter.service.CustomerService;
import com.assignment_two_starter.service.RefreshTokenService;
import com.assignment_two_starter.service.TokenBlacklistService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
/**
 * Controller for handling authentication, token issuance, and logout operations.
 *
 * <p>This controller provides endpoints for:</p>
 * <ul>
 *     <li>User authentication and JWT token generation.</li>
 *     <li>Refreshing access tokens using a valid refresh token.</li>
 *     <li>Logging out by revoking refresh tokens and blacklisting access tokens.</li>
 * </ul>
 *
 * <p>Authentication is handled via JWT tokens, ensuring secure, stateless user authentication.</p>
 *
 * <h3>Endpoints:</h3>
 * <ul>
 *     <li><strong>POST /authenticate</strong> - Authenticates a user and returns an access token & refresh token.</li>
 *     <li><strong>POST /refresh-token</strong> - Generates a new access token using a valid refresh token.</li>
 *     <li><strong>POST /logout</strong> - Revokes the refresh token and blacklists the access token.</li>
 * </ul>
 *
 * @author Alan Ryan
 * @version 1.0
 * @since 2024-02-18
 */
@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private CustomerService customerService;

    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

    /**
     * Authenticates a user using their email, password, and a One-Time Password (OTP).
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *     <li>Authenticates the user using their email and password.</li>
     *     <li>Validates the provided OTP against the stored secret key.</li>
     *     <li>Blacklists all previously issued tokens for the user.</li>
     *     <li>Generates a new access token and refresh token.</li>
     *     <li>Stores the newly issued access token.</li>
     * </ul>
     *
     * <p>If authentication or OTP validation fails, the method returns an appropriate
     * error response.</p>
     *
     * @param authenticationRequest The request containing the user's email, password, and OTP.
     * @return {@link ResponseEntity} containing a new {@link AuthenticationResponse} with the
     *         generated access and refresh tokens, or an error message if authentication fails.
     * @throws RuntimeException if the user is not found in the system.
     */
    @PostMapping("/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));

        String email = authenticationRequest.getUsername();
        Integer otp = authenticationRequest.getOtp();

        if (otp == null) {
            return ResponseEntity.badRequest().body("OTP is required.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());

        Customer customer = customerService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if (!googleAuthenticator.authorize(customer.getSecretKey(), otp)) {
            return ResponseEntity.status(401).body("Invalid OTP");
        }

        // ✅ Verify OTP using stored secret key
        if (!googleAuthenticator.authorize(customer.getSecretKey(), otp)) {
            return ResponseEntity.status(401).body("Invalid OTP");
        }
        //blacklist all previously issued tokens for this user
        tokenBlacklistService.blacklistPreviousTokens(userDetails.getUsername());

        //generate new access token
        String accessToken = jwtTokenUtil.generateToken(userDetails.getUsername());

        //store the newly issued token
        tokenBlacklistService.storeToken(userDetails.getUsername(), accessToken);

        //generate refresh token
        String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        return ResponseEntity.ok(new AuthenticationResponse(accessToken, refreshToken));
    }




    /**
     * Generates a new access token using a valid refresh token.
     *
     * <p>This method allows a user to obtain a new access token without re-authenticating
     * by providing a valid refresh token. The refresh token is validated, and if valid,
     * a new access token is generated and returned.</p>
     *
     * <p>Steps performed in this method:</p>
     * <ul>
     *     <li>Retrieves the refresh token from the request body.</li>
     *     <li>Validates the refresh token to ensure it has not expired or been revoked.</li>
     *     <li>Extracts the associated username from the refresh token.</li>
     *     <li>Generates a new access token for the user.</li>
     * </ul>
     *
     * @param request A map containing the refresh token as a key-value pair.
     * @return {@link ResponseEntity} containing a new access token if the refresh token is valid.
     *         Returns a {@code 401 Unauthorized} response if the refresh token is invalid or expired.
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (refreshToken == null || !refreshTokenService.isValidRefreshToken(refreshToken)) {
            return ResponseEntity.status(401).body("Invalid or expired refresh token");
        }

        String username = refreshTokenService.getUsernameFromToken(refreshToken);
        String newAccessToken = jwtTokenUtil.generateToken(username);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    /**
     * Logs out a user by revoking the refresh token and blacklisting the access token.
     *
     * <p>This method ensures that the user is fully logged out by:</p>
     * <ul>
     *     <li>Validating that the {@code Authorization} header is present.</li>
     *     <li>Revoking the associated refresh token from the database to prevent future refresh requests.</li>
     *     <li>Blacklisting the access token to prevent its use for further authentication.</li>
     * </ul>
     *
     * @param accessToken The {@code Authorization} header containing the Bearer access token.
     * @param request A map containing the refresh token that needs to be revoked.
     * @return {@link ResponseEntity} with a success message if the logout is successful.
     *         Returns a {@code 400 Bad Request} response if the {@code Authorization} header is missing.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String accessToken, @RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authorization header is required for logout");
        }


        //delete the refresh token from hte db
        if (refreshToken != null) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }

        //blacklist the token
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            tokenBlacklistService.blacklistToken(accessToken.substring(7)); //blacklist access token
        }

        return ResponseEntity.ok("Logged out successfully");
    }
}


