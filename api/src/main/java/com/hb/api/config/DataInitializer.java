package com.hb.api.config;

import com.hb.api.model.User;
import com.hb.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(new User("funcionario", "123", "FUNCIONARIO"));
                userRepository.save(new User("dev", "123", "DEV"));
                userRepository.save(new User("chefe", "123", "CHEFE_TI"));
                System.out.println("Usuários padrão criados: funcionario, dev, chefe (Senha: 123)");
            }
        };
    }
}
