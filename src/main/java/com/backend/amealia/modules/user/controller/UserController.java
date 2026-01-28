package com.backend.amealia.modules.user.controller;

import com.backend.amealia.modules.user.dto.UserDTO;
import com.backend.amealia.modules.user.entity.User;
import com.backend.amealia.modules.user.service.UserService;
import com.backend.amealia.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("")
    public ResponseEntity<?> getUserDetails() {
        ApiResponse<UserDTO> response = userService.getUserDetails();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
