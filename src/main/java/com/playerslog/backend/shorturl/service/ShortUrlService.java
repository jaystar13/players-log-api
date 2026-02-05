package com.playerslog.backend.shorturl.service;

import com.playerslog.backend.shorturl.domain.ShortUrl;
import com.playerslog.backend.shorturl.repository.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShortUrlService {

    private final ShortUrlRepository shortUrlRepository;
    private static final int SHORT_CODE_LENGTH = 8;
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String generateShortUrl(Long gollId) {
        String shortCode = generateUniqueShortCode();
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setGollId(gollId);
        shortUrlRepository.save(shortUrl);
        return shortCode;
    }

    public Long getGollIdByShortCode(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .map(ShortUrl::getGollId)
                .orElseThrow(() -> new IllegalArgumentException("Short URL not found for code: " + shortCode));
    }

    private String generateUniqueShortCode() {
        String shortCode;
        do {
            shortCode = generateRandomShortCode();
        } while (shortUrlRepository.existsByShortCode(shortCode));
        return shortCode;
    }

    private String generateRandomShortCode() {
        // Generate a random UUID, take a portion of it, and convert to base62
        UUID uuid = UUID.randomUUID();
        // Use a part of the UUID's bits to generate a short code
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();

        StringBuilder sb = new StringBuilder();
        long combinedBits = mostSigBits ^ leastSigBits; // Combine for more randomness

        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            int index = (int) (combinedBits % BASE62_CHARS.length());
            if (index < 0) {
                index += BASE62_CHARS.length(); // Ensure index is positive
            }
            sb.append(BASE62_CHARS.charAt(index));
            combinedBits /= BASE62_CHARS.length();
            if (combinedBits == 0 && i < SHORT_CODE_LENGTH - 1) { // If bits run out, replenish with more randomness
                combinedBits = secureRandom.nextLong();
            }
        }
        return sb.toString();
    }
}