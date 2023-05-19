package com.example.base.controller;

import com.example.base.entity.RefreshToken;
import com.example.base.entity.User;
import com.example.base.error.UserServiceException;
import com.example.base.request.SignInUpRequest;
import com.example.base.request.UpdateProfileRequest;
import com.example.base.security.UserDetailsImpl;
import com.example.base.service.RefreshTokenService;
import com.example.base.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;

import static org.mockito.ArgumentMatchers.any;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthAppControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private RefreshTokenService refreshTokenService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void successfulLocalLogin() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        var mockLoginRequest = buildMockLoginRequest("LOCAL");
        var mockUser = buildMockUser(mockLoginRequest.getEmail(), "LOCAL");
        var mockAuthenticationObject = buildMockAuthenticationObject(mockUser);

        // I can mock DB also...using embedded h2 set in application-test.yml file
        Mockito.when(userService.getUserDetails(mockLoginRequest.getEmail()))
                .thenReturn(mockUser);
        Mockito.when(authenticationManager
                .authenticate(any())).thenReturn(mockAuthenticationObject);
        Mockito.when(refreshTokenService.createRefreshToken(any())).thenReturn(buildMockRefreshToken(mockUser));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockLoginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(mockLoginRequest.getEmail()));

    }

    @Test
    void invalidCredentialsLocalLogin() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        var mockLoginRequest = buildMockLoginRequest("LOCAL");

        Mockito.when(userService.getUserDetails(mockLoginRequest.getEmail()))
                .thenThrow(new UserServiceException("User Details Not Found"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login", mockLoginRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(mockLoginRequest)))

                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

    }

    @Test
    void invalidProviderLogin() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        var mockLoginRequest = buildMockLoginRequest("GOOGLE");
        var mockUser = buildMockUser(mockLoginRequest.getEmail(), "GOOGLE");

        Mockito.when(userService.getUserDetails(mockLoginRequest.getEmail()))
                .thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/login", mockLoginRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockLoginRequest)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

    }

    @Test
    void successfulOauthLogin() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        var mockLoginRequest = buildMockLoginRequest("GOOGLE");
        var mockUser = buildMockUser(mockLoginRequest.getEmail(), "GOOGLE");
        var mockAuthenticationObject = buildMockAuthenticationObject(mockUser);

        Mockito.when(userService.getUserDetails(mockLoginRequest.getEmail()))
                .thenReturn(mockUser);
        Mockito.when(authenticationManager
                .authenticate(any())).thenReturn(mockAuthenticationObject);
        Mockito.when(refreshTokenService.createRefreshToken(any())).thenReturn(buildMockRefreshToken(mockUser));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/oauthLogin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockLoginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(mockLoginRequest.getEmail()));

    }

    @Test
    void invalidProviderOauthLogin() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        var mockLoginRequest = buildMockLoginRequest("LOCAL");
        var mockUser = buildMockUser(mockLoginRequest.getEmail(), "LOCAL");


        Mockito.when(userService.getUserDetails(mockLoginRequest.getEmail()))
                .thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/oauthLogin", mockLoginRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockLoginRequest)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

    }

    @Test
    void successfulLocalSignup() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();

        var mockLoginRequest = buildMockLoginRequest("LOCAL");
        var mockUser = buildMockUser(mockLoginRequest.getEmail(), "LOCAL");
        var mockAuthenticationObject = buildMockAuthenticationObject(mockUser);

        Mockito.when(userService.createUser(mockLoginRequest))
                .thenReturn(mockUser);
        Mockito.when(authenticationManager
                .authenticate(any())).thenReturn(mockAuthenticationObject);
        Mockito.when(refreshTokenService.createRefreshToken(any())).thenReturn(buildMockRefreshToken(mockUser));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockLoginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(mockLoginRequest.getEmail()));

    }

    @Test
    void successfulProviderSignup() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();

        var mockLoginRequest = buildMockLoginRequest("GOOGLE");
        var mockUser = buildMockUser(mockLoginRequest.getEmail(), "GOOGLE");
        var mockAuthenticationObject = buildMockAuthenticationObject(mockUser);

        Mockito.when(userService.createUser(mockLoginRequest))
                .thenReturn(mockUser);
        Mockito.when(authenticationManager
                .authenticate(any())).thenReturn(mockAuthenticationObject);
        Mockito.when(refreshTokenService.createRefreshToken(any())).thenReturn(buildMockRefreshToken(mockUser));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockLoginRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.authToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(mockLoginRequest.getEmail()));

    }

    @Test
    void duplicateUserSignup() throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();

        var mockLoginRequest = buildMockLoginRequest("LOCAL");

        Mockito.when(userService.createUser(mockLoginRequest))
                .thenThrow(new UserServiceException("Email Already Registered"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockLoginRequest)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

    }

    @Test
    void getProfileAuthenticated() throws Exception {
        var mockUser = buildMockUser("qwerty@gmail", "LOCAL");

        Mockito.when(userService.getUserDetails(any())).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user(UserDetailsImpl.build(mockUser)))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(mockUser.getEmail()));

    }

    @Test
    void getProfileUnAuthenticated() throws Exception {
        var mockUser = buildMockUser("qwerty@gmail", "LOCAL");

        Mockito.when(userService.getUserDetails(any())).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/user/profile")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));

    }

    @Test
    void editProfileAuthenticated() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        var mockUpdateRequest = buildMockUpdateRequest();
        var mockUser = buildMockUser("qwerty@gmail", "LOCAL");

        Mockito.when(userService.updateUserDetails(mockUser.getEmail(), mockUpdateRequest)).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/editProfile")
                        .with(SecurityMockMvcRequestPostProcessors.user(UserDetailsImpl.build(mockUser)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUpdateRequest)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.email").value(mockUser.getEmail()));
    }

    @Test
    void editProfileUnauthenticated() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        var mockUpdateRequest = buildMockUpdateRequest();
        var mockUser = buildMockUser("qwerty@gmail", "LOCAL");

        Mockito.when(userService.getUserDetails(any())).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/v1/user/editProfile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockUpdateRequest)))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.success").value(false));
    }

    private SignInUpRequest buildMockLoginRequest(String provider) {
        return SignInUpRequest.builder()
                .email("qwerty@gmail.com")
                .password("qwerty")
                .provider(provider)
                .build();
    }

    private User buildMockUser(String email, String provider) {
        return User.builder()
                .id(1L)
                .email(email)
                .provider(provider)
                .build();
    }

    private Authentication buildMockAuthenticationObject(User user) {
        return new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return UserDetailsImpl.build(user);
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return null;
            }
        };
    }

    private RefreshToken buildMockRefreshToken(User user) {
        return RefreshToken.builder()
                .id(user.getId())
                .token("klil-sdfsf-wewew")
                .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
    }

    private UpdateProfileRequest buildMockUpdateRequest() {
        return UpdateProfileRequest.builder()
                .name("John Doe")
                .bio("An Industrialist")
                .build();
    }
}
