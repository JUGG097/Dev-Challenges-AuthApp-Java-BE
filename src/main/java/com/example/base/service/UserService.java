package com.example.base.service;

import com.example.base.entity.User;
import com.example.base.error.UserServiceException;
import com.example.base.repository.UserRepository;
import com.example.base.request.SignInUpRequest;
import com.example.base.request.UpdateProfileRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    public User createUser (SignInUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserServiceException("Email Already Registered");
        }

        User user;
        // Create User Entity and Save to DB
        if (request.getProvider().equals("LOCAL")) {
            user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(encoder.encode(request.getPassword()))
                    .provider(request.getProvider())
                    .build();
        } else  {
            user = User.builder()
                    .name(request.getName())
                    .email(request.getEmail())
                    .password(encoder.encode(request.getEmail() + request.getProvider()))
                    .provider(request.getProvider())
                    .image(request.getImage())
                    .build();
        }

        return userRepository.save(user);
    }

    public User getUserDetails (String email) {
        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            throw new UserServiceException("User Details Not Found");
        }
        return maybeUser.get();
    }

    public User updateUserDetails (String email, UpdateProfileRequest request) {
        Optional<User> maybeUser = userRepository.findByEmail(email);
        if (maybeUser.isEmpty()) {
            throw new UserServiceException("User Details Not Found");
        }
        User user = maybeUser.get();

        user.setName(request.getName());
        user.setBio(request.getBio());
        user.setImage(request.getImage());
        user.setPhoneNumber(request.getPhoneNumber());

        return userRepository.save(user);
    }

}
