package com.example.base.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ControllerAdvice
@ResponseStatus
@ResponseBody
public class ResponseHandler {
    public ResponseEntity<Object> generateResponse(Boolean success, HttpStatus status, Object responseObj) {

        Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("success", success);
        map.put("data", responseObj);

        return new ResponseEntity<Object>(map, status);

    }

    public ResponseEntity<Object> generateResponse(Boolean success, HttpStatus status, String message) {

        Map<String, Object> map = new ConcurrentHashMap<>();
        map.put("success", success);
        map.put("message", message);

        return new ResponseEntity<Object>(map, status);
    }

    public ResponseEntity<Object> generateJwtResponse(Boolean success, HttpStatus status,
                                                      java.lang.Object responseObj, String jwtToken,
                                                      String refreshToken) {

        Map<String, java.lang.Object> map = new ConcurrentHashMap<>();
        map.put("success", success);
        map.put("data", responseObj);
        map.put("authToken", jwtToken);
        map.put("refreshToken", refreshToken);

        return new ResponseEntity<java.lang.Object>(map, status);

    }

}
