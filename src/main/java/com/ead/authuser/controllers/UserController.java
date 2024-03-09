package com.ead.authuser.controllers;

import com.ead.authuser.dtos.UserDto;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserService;
import com.ead.authuser.specifications.SpecificationTemplate;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/users")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping
    public ResponseEntity<Page<UserModel>> getAllUsers(
            SpecificationTemplate.UserSpec spec,
            @PageableDefault(sort = "userId", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<UserModel> page = userService.findAll(spec, pageable);
        for (UserModel user : page.toList()) {
            user.add(
                    WebMvcLinkBuilder.linkTo(
                            WebMvcLinkBuilder.methodOn(UserController.class).getOneUser(user.getUserId())
                    ).withSelfRel()
            );
        }
        return ResponseEntity.status(HttpStatus.OK).body(page);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object> getOneUser(@PathVariable(value = "userId") UUID userId) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        return userModelOptional
                .<ResponseEntity<Object>>map(userModel -> ResponseEntity.status(HttpStatus.OK).body(userModel))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found"));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@PathVariable(value = "userId") UUID userId) {
        log.debug("DELETE deleteUser userId received {}", userId);
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        userService.delete(userModelOptional.get());
        log.debug("DELETE deleteUser userId deleted {}", userId);
        log.info("User deleted successfully userId {}", userId);
        return ResponseEntity.status(HttpStatus.OK).body("User successfully deleted");
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Object> updateUser(
            @PathVariable(value = "userId") UUID userId,
            @RequestBody @Validated({UserDto.UserView.UserPut.class}) @JsonView(UserDto.UserView.UserPut.class) UserDto dto
    ) {
        log.debug("PUT updateUser userDto received {}", dto);
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        var userModel = userModelOptional.get();
        userModel.setFullName(dto.getFullName());
        userModel.setPhoneNumber(dto.getPhoneNumber());
        userModel.setCpf(dto.getCpf());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));
        userService.save(userModel);
        log.debug("POST updateUser userDto userId {}", userModel.getUserId());
        log.info("User updated successfully userId {}", userModel.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

    @PutMapping("/{userId}/password")
    public ResponseEntity<Object> updatePassword(
            @PathVariable(value = "userId") UUID userId,
            @RequestBody @Validated({UserDto.UserView.PasswordPut.class}) @JsonView(UserDto.UserView.PasswordPut.class) UserDto dto
    ) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        var userModel = userModelOptional.get();
        if (!userModel.getPassword().equals(dto.getOldPassword())) {
            log.warn("Old password mismatch userId {}", userId);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Old password mismatch!");
        }

        userModel.setPassword(dto.getPassword());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        userService.save(userModel);
        log.debug("POST updateUser userDto userId {}", userModel.getUserId());
        log.info("User updated successfully userId {}", userModel.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body("Password updated successfully.");
    }

    @PutMapping("/{userId}/image")
    public ResponseEntity<Object> updateImage(
            @PathVariable(value = "userId") UUID userId,
            @RequestBody @Validated({UserDto.UserView.ImagePut.class}) @JsonView(UserDto.UserView.ImagePut.class) UserDto dto
    ) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
        var userModel = userModelOptional.get();

        userModel.setImageUrl(dto.getImageUrl());
        userModel.setLastUpdateDate(LocalDateTime.now(ZoneId.of("UTC")));

        userService.save(userModel);
        log.debug("POST updateUser userDto userId {}", userModel.getUserId());
        log.info("User updated successfully userId {}", userModel.getUserId());
        return ResponseEntity.status(HttpStatus.OK).body(userModel);
    }

}
