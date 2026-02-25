package com.dev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class DevApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        String mongoUri = dotenv.get("SPRING_DATA_MONGODB_URI");

        if (mongoUri != null && !mongoUri.isBlank()) {
            System.setProperty("SPRING_DATA_MONGODB_URI", mongoUri);
            System.out.println("SPRING_DATA_MONGODB_URI: " + mongoUri);

        } else {
            System.err.println("⚠️ SPRING_DATA_MONGODB_URI not found in .env or environment variables");
        }

        SpringApplication.run(DevApplication.class, args);
        System.out.println("✅ Server is running...");
    }
}
