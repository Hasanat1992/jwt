package com.jwt.springBoot.JWTEx.controller;


import com.jwt.springBoot.JWTEx.dto.*;
import com.jwt.springBoot.JWTEx.entity.Role;
import com.jwt.springBoot.JWTEx.entity.User;
import com.jwt.springBoot.JWTEx.repository.RoleRepository;
import com.jwt.springBoot.JWTEx.repository.UserRepository;
import com.jwt.springBoot.JWTEx.security.jwt.JwtUtils;
import com.jwt.springBoot.JWTEx.security.services.UserDetailsImpl;
import com.jwt.springBoot.JWTEx.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();


//    List<String> roles = userDetails.getAuthorities().stream()
//        .map(item -> item.getAuthority())
//        .collect(Collectors.toList());

        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

//    return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
//        .body(new UserInfoResponse(userDetails.getId(),
//                                   userDetails.getUsername(),
//                                   userDetails.getEmail(),
//                                   roles));
        String token = jwtUtils.generateJwtToken(userDetails);

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(new JwtResponse(token));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(userDto.getUsername(),
                userDto.getEmail(),
                encoder.encode(userDto.getPassword()),
                userDto.getUserFirstName(),
                userDto.getUserLastName());

        Set<String> strRoles = userDto.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findById("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else if (strRoles.isEmpty()) {
            Role userRole = roleRepository.findById("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findById("ROLE_ADMIN")
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "mod":
                        Role modRole = roleRepository.findById("ROLE_MODERATOR")
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findById("ROLE_USER")
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }
//        final List<Role> roles = roleRepository.findAllById(userDTO.getRoles() == null ? Collections.emptyList() : userDTO.getRoles());
//        if (roles.size() != (userDTO.getRoles() == null ? 0 : userDTO.getRoles().size())) {
//            throw new RuntimeException("one of role01s not found");
//        }
//        user.setRoles(new HashSet<>(roles));
        user.setRoles(roles);
//        user.setRoles(new HashSet<>(roles));
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User01 registered successfully!"));
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        ResponseCookie cookie = jwtUtils.getCleanJwtCookie();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new MessageResponse("You've been signed out!"));
    }
}
