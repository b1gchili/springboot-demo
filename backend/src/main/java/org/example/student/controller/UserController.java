package org.example.student.controller;

import org.example.student.model.Result;
import org.example.student.model.PageResult;
import org.example.student.model.UserListItem;
import org.example.student.model.UserRequest;
import org.example.student.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<PageResult<UserListItem>> listUsers(@RequestParam(required = false) Integer pageNum,
                                                      @RequestParam(required = false) Integer pageSize) {
        return Result.success(userService.listUsers(pageNum, pageSize));
    }

    @PostMapping
    public Result<String> addUser(@RequestBody UserRequest request) {
        return Result.success(userService.addUser(request));
    }

    @PutMapping("/{userId}")
    public Result<Void> updateUser(@PathVariable String userId, @RequestBody UserRequest request) {
        userService.updateUser(userId, request);
        return Result.success();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return Result.success();
    }
}
