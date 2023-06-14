package com.example.base.controller;

import com.example.base.entity.GitHubAccessToken;
import com.example.base.entity.GitHubUserDetails;
import com.example.base.entity.RefreshToken;
import com.example.base.entity.User;
import com.example.base.error.AuthAppException;
import com.example.base.error.TokenRefreshException;
import com.example.base.request.SignInUpRequest;
import com.example.base.request.TokenRefreshRequest;
import com.example.base.request.UpdateProfileRequest;
import com.example.base.response.ResponseHandler;
import com.example.base.security.UserDetailsImpl;
import com.example.base.security.jwt.JwtUtils;
import com.example.base.service.RefreshTokenService;
import com.example.base.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.Valid;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthAppController {

    private final String endpointAuthPrefix = "api/v1/auth";

    private final String endpointUserPrefix = "api/v1/user";

    @Value("${github.client_id}")
    private String githubClientID;

    @Value("${github.client_secret}")
    private String githubClientSecret;

    @Value("${client.url}")
    private String frontEndUrl;

    @Autowired
    private ResponseHandler responseHandler;

    @Autowired
    AuthenticationManager authenticationManager;

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
        // Check if OAuth was used to create account
        User user = userService.getUserDetails(request.getEmail());
        if (!user.getProvider().equals("LOCAL")) {
            return responseHandler.generateResponse(false,
                    HttpStatus.BAD_REQUEST, "Log In With " + user.getProvider());
        }

        UserDetailsImpl userDetails = authenticateRequest(request.getEmail(), request.getPassword());

        String jwt = jwtUtils.generateJwtToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        // Extract Info needed
        Map<String, Object> userObj = new ConcurrentHashMap<>();
        userObj.put("id", userDetails.getId());
        userObj.put("email", userDetails.getEmail());

        return responseHandler.generateJwtResponse(true, HttpStatus.OK, userObj, jwt, refreshToken.getToken());
    }

    @PostMapping(endpointAuthPrefix + "/oauthLogin")
    public ResponseEntity<Object> oauthLogin(@Valid @RequestBody SignInUpRequest request) {

        // Check if OAuth was used to create account
        User user = userService.getUserDetails(request.getEmail());
        if (user.getProvider().equals("LOCAL")) {
            return responseHandler.generateResponse(false,
                    HttpStatus.BAD_REQUEST, "Log In With Email and Password");
        }

        UserDetailsImpl userDetails = authenticateRequest(request.getEmail(),
                request.getEmail() + request.getProvider());

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

        UserDetailsImpl userDetails;
        if (user.getProvider().equals("LOCAL")) {
            userDetails = authenticateRequest(request.getEmail(), request.getPassword());
        } else {
            userDetails = authenticateRequest(request.getEmail(), request.getEmail() + request.getProvider());
        }


        String jwt = jwtUtils.generateJwtToken(userDetails);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        // Extract Info needed
        Map<String, Object> userObj = new ConcurrentHashMap<>();
        userObj.put("id", userDetails.getId());
        userObj.put("email", userDetails.getEmail());

        return responseHandler.generateJwtResponse(true, HttpStatus.OK, userObj, jwt, refreshToken.getToken());
    }

    @GetMapping(endpointAuthPrefix + "/githubOauth")
    public ResponseEntity<Void> githubOauth(@RequestParam("code") String code, @RequestParam("mode") String mode ) throws AuthAppException {
        try {
            var uri_1 = "https://github.com/login/oauth/access_token";
            var uri_2 = "https://api.github.com/user";

            Map<String, Object> reqBody = new ConcurrentHashMap<>();
            reqBody.put("client_id", githubClientID);
            reqBody.put("client_secret", githubClientSecret);
            reqBody.put("code", code);
            reqBody.put("accept", "json");

            Map<String, Object> respBody = new ConcurrentHashMap<>();

            WebClient client_1 = WebClient.builder()
                    .baseUrl(uri_1)
                    .build();

            GitHubAccessToken authTokenResponse =
                    client_1.post()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(reqBody).accept(MediaType.APPLICATION_JSON).retrieve().bodyToMono(GitHubAccessToken.class).log().block();
            assert authTokenResponse != null;

            WebClient client_2 = WebClient.builder()
                    .baseUrl(uri_2)
                    .defaultHeader("Authorization",
                            "Bearer " + authTokenResponse.getAccess_token())
                    .build();
            GitHubUserDetails gitHubUserDetails =
                    client_2.get().retrieve().bodyToMono(GitHubUserDetails.class).log().block();
            assert gitHubUserDetails != null;

            if (gitHubUserDetails.getEmail() == null) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(frontEndUrl + "/oauthRedirect/?authToken=empty&refreshToken=empty" +
                                "&mode=" + mode)).build();
            }

            SignInUpRequest request = new SignInUpRequest(gitHubUserDetails.getName(), gitHubUserDetails.getEmail(),
                    "", "GITHUB", gitHubUserDetails.getAvatar_url());

            if (mode.equals("signup")) {
                User user = userService.createUser(request);
            }

            UserDetailsImpl userDetails = authenticateRequest(request.getEmail(),
                    request.getEmail() + request.getProvider());

            String jwt = jwtUtils.generateJwtToken(userDetails);

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontEndUrl + "/oauthRedirect/?authToken=" +
                            jwt + "&refreshToken=" + refreshToken + "&mode=" + mode)).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontEndUrl + "/oauthRedirect/?authToken=empty&refreshToken=empty" +
                            "&mode=" + mode)).build();
        }
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
