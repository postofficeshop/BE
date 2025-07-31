	package com.example.demo.controller;

	import org.springframework.http.ResponseEntity;
	import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
	import org.springframework.security.crypto.password.PasswordEncoder;
	import org.springframework.web.bind.annotation.PostMapping;
	import org.springframework.web.bind.annotation.RequestBody;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RestController;

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

		private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

		@PostMapping("/signup")
		public ResponseEntity<?> registerUser(@RequestBody UserDTO userDTO) {
			try {
				if (userDTO == null || userDTO.getPassword() == null) {
					throw new RuntimeException("Invalid Password value.");
				}

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
			}
			catch (Exception e) {
				ResponseDTO responseDTO = ResponseDTO.builder()
					.error(e.getMessage())
					.build();

				return ResponseEntity.badRequest().body(responseDTO);
			}
		}

		@PostMapping("/login")
		public ResponseEntity<?> authenticate(@RequestBody UserDTO userDTO) {
			UserEntity user = userService.getByCredentials(userDTO.getUsername(), userDTO.getPassword(), passwordEncoder);

			if (user != null) {
				final String token = tokenProvider.create(user);

				final UserDTO responseUserDTO = UserDTO.builder()

					.username(user.getUsername())
					.id(user.getId())
					.token(token)
					.build();

				return ResponseEntity.ok().body(responseUserDTO);
			}
			else {
				ResponseDTO responseDTO = ResponseDTO.builder()
					.error("Login failed.")
					.build();

				return ResponseEntity.badRequest().body(responseDTO);
			}
		}

	}
