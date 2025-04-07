package com.zhsaidk.service;

import com.zhsaidk.database.entity.Role;
import com.zhsaidk.database.entity.Token;
import com.zhsaidk.database.entity.User;
import com.zhsaidk.database.repo.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final TokenService tokenService;

    public User getUserByEmail(String email){
        return userRepository.findUserByEmail(email)
                .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostConstruct
    public void init(){

        if (userRepository.findUserByEmail("zhavokhir02@gmail.com").isEmpty()){
            User user = User.builder()
                    .firstName("Zhavokhir")
                    .lastName("Saitkulov")
                    .email("zhavokhir02@gmail.com")
                    .password("{noop}123")
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(user);
        }
        else {
            log.info("Данные уже существует");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Set.of(user.getRole())
        );
    }
}
