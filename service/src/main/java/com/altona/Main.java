package com.altona;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.io.FileInputStream;
import java.io.IOException;

@SpringBootApplication
@EnableTransactionManagement
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(@Value("${firebase.key}") String firebaseKeyLocation) throws IOException {
        FileInputStream file = new FileInputStream(firebaseKeyLocation);
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(file))
                .build();
        return FirebaseMessaging.getInstance(FirebaseApp.initializeApp(options));
    }

}
