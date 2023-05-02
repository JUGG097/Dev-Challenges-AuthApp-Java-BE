package com.example.base.controller;

import com.example.base.entity.User;
import com.example.base.repository.UserRepository;
import com.example.base.request.SignInUpRequest;
import com.example.base.request.UpdateProfileRequest;
import com.example.base.response.ResponseHandler;
import com.example.base.security.UserDetailsImpl;
import com.example.base.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthAppController {

    private final String endpointAuthPrefix = "api/v1/auth";

    private final String endpointUserPrefix = "api/v1/user";

    @Autowired
    private ResponseHandler responseHandler;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/check")
    public ResponseEntity<Object> check() {
        return responseHandler.generateResponse(true,
                HttpStatus.OK,
                "Server is up and running");
    }

    @PostMapping(endpointAuthPrefix + "/login")
    public ResponseEntity<Object> login(@Valid @RequestBody SignInUpRequest request) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
                        request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Extract Info needed
        Map<String, Object> userObj = new ConcurrentHashMap<>();
        userObj.put("id", userDetails.getId());
        userObj.put("email", userDetails.getEmail());

        return responseHandler.generateJwtResponse(true, HttpStatus.OK, userObj, jwt);
    }

    @PostMapping(endpointAuthPrefix + "/signup")
    public ResponseEntity<Object> signup(@Valid @RequestBody SignInUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return responseHandler.generateResponse(false,
                    HttpStatus.BAD_REQUEST, "Email already taken");
        }
        // Create User Entity and Save to DB
        User user = User.builder()
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        // Authenticate and Generate Jwt
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),
                        request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Extract Info needed
        Map<String, Object> userObj = new ConcurrentHashMap<>();
        userObj.put("id", userDetails.getId());
        userObj.put("email", userDetails.getEmail());

        return responseHandler.generateJwtResponse(true, HttpStatus.OK, userObj, jwt);
    }

    @PutMapping(endpointUserPrefix + "/editProfile")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Object> editProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        String userEmail = userDetails.getUsername();
        Optional<User> maybeUser = userRepository.findByEmail(userEmail);

        if(maybeUser.isPresent()) {
            User user = maybeUser.get();
            user.setName(request.getName());
            user.setBio(request.getBio());
            user.setImage(request.getImage());
            user.setPhoneNumber(request.getPhoneNumber());

            userRepository.save(user);
            return responseHandler.generateResponse(true, HttpStatus.OK, user);
        }

        return responseHandler.generateResponse(false,
                HttpStatus.BAD_REQUEST,
                "User Details Not Found");
    }

    @GetMapping(endpointUserPrefix + "/profile")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Object> profile() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        String userEmail = userDetails.getUsername();
        Optional<User> maybeUser = userRepository.findByEmail(userEmail);

        if(maybeUser.isPresent()) {
            User user = maybeUser.get();
            return responseHandler.generateResponse(true, HttpStatus.OK, user);
        }

        return responseHandler.generateResponse(false,
                HttpStatus.BAD_REQUEST,
                "User Details Not Found");
    }
}
