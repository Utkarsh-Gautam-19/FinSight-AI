package com.finsight.ai.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FirebaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseAuthService.class);

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public FirebaseToken verifyToken(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            throw new RuntimeException("Authorization token is missing or empty");
        }
        try {
            if (!com.google.firebase.FirebaseApp.getApps().isEmpty()) {
                logger.debug("Verifying Firebase token via FirebaseApp...");
                return FirebaseAuth.getInstance().verifyIdToken(idToken);
            }
        } catch (Exception e) {
            logger.warn("Firebase Admin SDK token verification failed: {}. Falling back to token payload extraction for dev mode.", e.getMessage());
        }
        return null;
    }

    public String getUserIdFromToken(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            return "dev-user-default";
        }
        try {
            FirebaseToken token = verifyToken(idToken);
            if (token != null) {
                return token.getUid();
            }
        } catch (Exception ignored) {}

        // Dev mode fallback: decode JWT payload or use raw token
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length >= 2) {
                String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(payloadJson);
                if (node.has("user_id")) {
                    return node.get("user_id").asText();
                } else if (node.has("sub")) {
                    return node.get("sub").asText();
                } else if (node.has("user_id_fallback")) {
                    return node.get("user_id_fallback").asText();
                }
            }
        } catch (Exception e) {
            logger.debug("Could not parse JWT payload: {}", e.getMessage());
        }

        return idToken.length() > 128 ? idToken.substring(0, 128) : idToken;
    }

    public String getEmailFromToken(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            return "user@example.com";
        }
        try {
            FirebaseToken token = verifyToken(idToken);
            if (token != null) {
                return token.getEmail();
            }
        } catch (Exception ignored) {}

        // Dev mode fallback: decode JWT payload
        try {
            String[] parts = idToken.split("\\.");
            if (parts.length >= 2) {
                String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
                com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree(payloadJson);
                if (node.has("email")) {
                    return node.get("email").asText();
                }
            }
        } catch (Exception ignored) {}

        return "user@example.com";
    }
}
