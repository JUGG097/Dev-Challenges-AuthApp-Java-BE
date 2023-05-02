package com.example.base.controller;

import com.example.base.entity.RefreshToken;
import com.example.base.entity.User;
import com.example.base.error.TokenRefreshException;
import com.example.base.repository.UserRepository;
import com.example.base.request.SignInUpRequest;
import com.example.base.request.TokenRefreshRequest;
import com.example.base.request.UpdateProfileRequest;
import com.example.base.response.ResponseHandler;
import com.example.base.security.UserDetailsImpl;
import com.example.base.security.jwt.JwtUtils;
import com.example.base.service.RefreshTokenService;
import com.example.base.service.UserService;
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

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @GetMapping("/check")
    public ResponseEntity<Object> check() {
        return responseHandler.generateResponse(true,
                HttpStatus.OK,
                "Server is up and running");
    }

    @PostMapping(endpointAuthPrefix + "/login")
    public ResponseEntity<Object> login(@Valid @RequestBody SignInUpRequest request) {
        UserDetailsImpl userDetails = authenticateRequest(request.getEmail(), request.getPassword());

        String jwt = jwtUtils.generateJwtToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        // Extract Info needed
        Map<String, Object> userObj = new ConcurrentHashMap<>();
        userObj.put("id", userDetails.getId());
        userObj.put("email", userDetails.getEmail());

        return responseHandler.generateJwtResponse(true, HttpStatus.OK, userObj, jwt, refreshToken.getToken());
    }

    @PostMapping(endpointAuthPrefix + "/signup")
    public ResponseEntity<Object> signup(@Valid @RequestBody SignInUpRequest request) {
        User user = userService.createUser(request);

        UserDetailsImpl userDetails = authenticateRequest(request.getEmail(), request.getPassword());

        String jwt = jwtUtils.generateJwtToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        // Extract Info needed
        Map<String, Object> userObj = new ConcurrentHashMap<>();
        userObj.put("id", userDetails.getId());
        userObj.put("email", userDetails.getEmail());

        return responseHandler.generateJwtResponse(true, HttpStatus.OK, userObj, jwt, refreshToken.getToken());
    }

    @PostMapping(endpointAuthPrefix + "/refreshToken")
    public ResponseEntity<Object> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtUtils.generateTokenFromUsername(user.getEmail());
                    return responseHandler.generateJwtResponse(true,
                            HttpStatus.OK, user, token, requestRefreshToken);
                })
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PutMapping(endpointUserPrefix + "/editProfile")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Object> editProfile(@Valid @RequestBody UpdateProfileRequest request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        String userEmail = userDetails.getUsername();

        User user = userService.updateUserDetails(userEmail, request);

        return responseHandler.generateResponse(true, HttpStatus.OK, user);
    }

    @GetMapping(endpointUserPrefix + "/profile")
    @PreAuthorize("hasAuthority('user')")
    public ResponseEntity<Object> profile() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        String userEmail = userDetails.getUsername();

        User user = userService.getUserDetails(userEmail);

        return responseHandler.generateResponse(true, HttpStatus.OK, user);
    }

    private UserDetailsImpl authenticateRequest(String email, String password) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email,
                        password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return (UserDetailsImpl) authentication.getPrincipal();
    }
}
