package io.pathops.controlplane.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Service;

import io.pathops.controlplane.repository.TenantRepository;
import io.pathops.controlplane.utils.TenantUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TenantSlugService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_SUFFIX_LENGTH = 4;

    private final TenantRepository tenantRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public String generateTenantSlug(String preferredUsername) {
        String base = TenantUtils.slugBase(preferredUsername);

        String firstCandidate = base;
        if (!tenantRepository.existsBySlug(firstCandidate)) {
            return firstCandidate;
        }

        String candidate;
        do {
            candidate = base + "-" + randomSuffix(RANDOM_SUFFIX_LENGTH);
        } while (tenantRepository.existsBySlug(candidate));

        return candidate;
    }

    private String randomSuffix(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }

        return sb.toString();
    }
}