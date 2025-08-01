package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.ResponseDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.UserEntity;
import com.example.demo.security.TokenProvider;
import com.example.demo.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

	private final UserService userService;
	private final TokenProvider tokenProvider;
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
		try {
			if (userDTO == null || userDTO.getUsername() == null || userDTO.getPassword() == null) {
				throw new RuntimeException("Username and password must be provided.");
			}

			// 유효성 검사
			if (!userDTO.getUsername().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
				throw new RuntimeException("Invalid email format.");
			}

			if (!userDTO.getPassword().matches("^(?=.*[a-zA-Z])(?=.*\\d).{8,}$")) {
				throw new RuntimeException("Password must be at least 8 characters and contain letters and numbers.");
			}

			// 중복 유저 확인
			if (userService.existsByUsername(userDTO.getUsername())) {
				throw new RuntimeException("Username already exists.");
			}

			// 사용자 생성
			UserEntity user = UserEntity.builder()
					.username(userDTO.getUsername())
					.password(passwordEncoder.encode(userDTO.getPassword()))
					.build();

			UserEntity registeredUser = userService.create(user);

			UserDTO responseUserDTO = UserDTO.builder()
					.id(registeredUser.getId())
					.username(registeredUser.getUsername())
					.build();

			return ResponseEntity.ok().body(responseUserDTO);
		} catch (Exception e) {
			ResponseDTO responseDTO = ResponseDTO.builder()
					.error(e.getMessage())
					.build();
			return ResponseEntity.badRequest().body(responseDTO);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO) {
		try {
			if (userDTO == null || userDTO.getUsername() == null || userDTO.getPassword() == null) {
				throw new RuntimeException("Username and password must be provided.");
			}

			UserEntity user = userService.getByCredentials(
					userDTO.getUsername(),
					userDTO.getPassword(),
					passwordEncoder);

			if (user != null) {
				final String token = tokenProvider.create(user);

				final UserDTO responseUserDTO = UserDTO.builder()
						.id(user.getId())
						.username(user.getUsername())
						.token(token)
						.build();

				return ResponseEntity.ok().body(responseUserDTO);
			} else {
				throw new RuntimeException("Invalid username or password.");
			}
		} catch (Exception e) {
			ResponseDTO responseDTO = ResponseDTO.builder()
					.error(e.getMessage())
					.build();
			return ResponseEntity.badRequest().body(responseDTO);
		}
	}
}
