package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// import java.util.List;
// import java.util.stream.Collectors;

@RestController
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Docker + MySQL!";
    }

    @PostMapping("/users")
    public UserDto saveUser(@RequestBody UserDto userDto) {
        User user = new User();
        user.setId(userDto.getId());
        user.setName(userDto.getName());
        user = userRepository.save(user);
        UserDto response = new UserDto();
        response.setId(user.getId());
        response.setName(user.getName());
        return response;
    }

    // @GetMapping("/users")
    // public List<UserDto> getUsers() {
    //     return userRepository.findAll().stream()
    //             .map(user -> {
    //                 UserDto dto = new UserDto();
    //                 dto.setId(user.getId());
    //                 dto.setName(user.getName());
    //                 return dto;
    //             })
    //             .collect(Collectors.toList());
    // }

    @GetMapping("/users")
    public Page<UserDto> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setName(user.getName());
            return dto;
        });
    }

    @GetMapping("/users/{id}")
    public UserDto getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        return dto;
    }

    @PutMapping("/users/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        User user = new User();
        user.setId(id);
        user.setName(userDto.getName());
        user = userRepository.save(user);
        UserDto response = new UserDto();
        response.setId(user.getId());
        response.setName(user.getName());
        return response;
    }

    @DeleteMapping("/users/{id}")
    public void deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }
}