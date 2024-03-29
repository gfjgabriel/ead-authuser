package com.ead.authuser.controllers;

import com.ead.authuser.clients.CourseClient;
import com.ead.authuser.dtos.CourseDto;
import com.ead.authuser.dtos.UserCourseDto;
import com.ead.authuser.models.UserCourseModel;
import com.ead.authuser.models.UserModel;
import com.ead.authuser.services.UserCourseService;
import com.ead.authuser.services.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@Log4j2
@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserCourseController {

    private final CourseClient courseClient;
    private final UserService userService;
    private final UserCourseService userCourseService;

    @GetMapping("/users/{userId}/courses")
    public ResponseEntity<Page<CourseDto>> getAllCoursesByUser(
            @PageableDefault(sort = "courseId", direction = Sort.Direction.ASC) Pageable pageable,
            @PathVariable UUID userId
    ) {
        Page<CourseDto> page = courseClient.getAllCoursesByUser(userId, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(page);
    }

    @PostMapping("/users/{userId}/courses/subscription")
    public ResponseEntity<Object> saveSubscriptionUserInCourse(
            @PathVariable UUID userId,
            @RequestBody @Valid UserCourseDto userCourseDto
    ) {
        Optional<UserModel> userModelOptional = userService.findById(userId);
        if (userModelOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        UserModel userModel = userModelOptional.get();
        if (userCourseService.existsByUserAndCourseId(userModel, userCourseDto.getCourseId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Subscription already exists!");
        }
        UserCourseModel userCourseModel = userModel.convertToUserCourseModel(userCourseDto.getCourseId());
        userCourseModel = userCourseService.save(userCourseModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(userCourseModel);
    }

    @DeleteMapping("/users/courses/{courseId}")
    public ResponseEntity<Object> deleteUserCourseByCourse(
            @PathVariable UUID courseId
    ) {
        if (!userCourseService.existsByCourseId(courseId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserCourse not found");
        }
        userCourseService.deleteUserCourseByCourse(courseId);
        return ResponseEntity.status(HttpStatus.OK).body("UserCourse deleted successfully");
    }
}
