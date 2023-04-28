package com.example.base.controller;

import com.example.base.request.LoginRequest;
import com.example.base.response.ResponseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthAppController {

    private final String endpointPrefix = "api/v1";

    @Autowired
    private ResponseHandler responseHandler;

    @GetMapping("/check")
    public ResponseEntity<Object> check() {
        return responseHandler.generateResponse("Server is up and running", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        return responseHandler.generateResponse(true, HttpStatus.OK, request);
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@Valid @RequestBody LoginRequest request) {
        return responseHandler.generateResponse(true, HttpStatus.OK, request);
    }

    @PutMapping("/editProfile")
    public ResponseEntity<Object> editProfile(@Valid @RequestBody LoginRequest request) {
        return responseHandler.generateResponse(true, HttpStatus.OK, request);
    }

    @GetMapping("/profile")
    public ResponseEntity<Object> profile() {
        return responseHandler.generateResponse("Placeholder Response", HttpStatus.OK);
    }
}
