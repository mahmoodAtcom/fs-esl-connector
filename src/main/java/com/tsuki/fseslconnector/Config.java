package com.tsuki.fseslconnector;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public ClientHandler clientHandler() {
        return new ClientHandler();
    }

    @Bean
    public FsEslClient fsEslClient(ClientHandler clientHandler) {
        return new FsEslClient(clientHandler);
    }
}
