package com.charles.schwab.url.shortner.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.net.URISyntaxException;

public class SafeUrlValidator implements ConstraintValidator<SafeUrl, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                return false;
            }

            String host = uri.getHost();
            if (host == null) {
                return false;
            }

            // Convert to lower case to check
            host = host.toLowerCase();

            // Prevent localhost and basic local domains
            if (host.equals("localhost") || host.endsWith(".localhost") || host.endsWith(".local")) {
                return false;
            }

            // Prevent common internal IPs (IPv4)
            if (host.startsWith("127.") || 
                host.startsWith("10.") || 
                host.startsWith("192.168.") || 
                host.startsWith("169.254.") || 
                host.equals("0.0.0.0")) {
                return false;
            }

            // Note: A full production implementation would resolve the host to an IP to prevent DNS rebinding
            // and check against the 172.16.0.0/12 range and IPv6 loopbacks (::1).
            return true;

        } catch (URISyntaxException e) {
            return false;
        }
    }
}
