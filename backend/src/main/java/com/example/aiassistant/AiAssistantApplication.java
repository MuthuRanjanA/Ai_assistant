package com.example.aiassistant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@SpringBootApplication
public class AiAssistantApplication {

    private static final Logger log = LoggerFactory.getLogger(AiAssistantApplication.class);

    public static void main(String[] args) {
        loadDotEnvIfPresent();
        SpringApplication.run(AiAssistantApplication.class, args);
    }

    /**
     * Automatically loads .env file if present in the working directory or parent directory.
     */
    private static void loadDotEnvIfPresent() {
        String[] possiblePaths = {".env", "backend/.env", "../.env", "../backend/.env"};
        for (String path : possiblePaths) {
            File envFile = new File(path);
            if (envFile.exists() && envFile.isFile()) {
                try {
                    log.info("Loading environment variables from: {}", envFile.getAbsolutePath());
                    List<String> lines = Files.readAllLines(envFile.toPath());
                    for (String line : lines) {
                        String trimmed = line.trim();
                        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                            continue;
                        }
                        int eqIdx = trimmed.indexOf('=');
                        if (eqIdx > 0) {
                            String key = trimmed.substring(0, eqIdx).trim();
                            String value = trimmed.substring(eqIdx + 1).trim();
                            // Only set if not already present in System properties or env
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                    return;
                } catch (Exception e) {
                    log.warn("Could not read .env file at {}: {}", path, e.getMessage());
                }
            }
        }
    }
}
