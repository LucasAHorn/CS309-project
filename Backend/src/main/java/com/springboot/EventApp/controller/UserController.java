package com.springboot.EventApp.controller;

import com.springboot.EventApp.model.dto.SimpleJSONResponse;
import com.springboot.EventApp.model.entities.UserInfo;
import com.springboot.EventApp.repository.UserInfoRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import com.springboot.EventApp.model.dto.LoginRequest;
import com.springboot.EventApp.model.dto.ResetPasswordRequest;

import static com.springboot.EventApp.util.HashingUtil.getHashCode;

/**
 * This class manages user CRUD operations, creating a user, logging in, editing info, deleting user
 *
 * @author aaryamann dev sharma - partial completion of two endpoints
 * @author Lucas Horn - completion of all endpoints
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserInfoRepository userInfoRepository;

    /**
     * @param email - the users email
     * @return true - valid email, false - invalid email
     */
    private static boolean isValidEmail(String email) {
        if (email == null || email.contains(" ")) {
            return false;
        }
        int atIndex = email.indexOf('@');
        int dotIndex = email.indexOf('.', atIndex);

        return atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < email.length() - 1;
    }


    @Operation(summary = "User Login", description = "take username and password, return the user ID (allows the user to access other parts of the app)")
    @PostMapping("/login")
    public SimpleJSONResponse login(@RequestBody LoginRequest loginRequest) {
        Optional<UserInfo> userOpt = userInfoRepository.findByUsername(loginRequest.getName());

        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "User not found, please try again!!", null);
        }

        UserInfo user = userOpt.get();

        try {
            String hashedPassword = getHashCode(user.getUsername(), loginRequest.getPassword());

            if (!hashedPassword.equals(user.getPassword())) {
                return new SimpleJSONResponse(0, "Invalid credentials", null);
            }

            return new SimpleJSONResponse(1, "Successfully logged in!! :)", user.getUserId()); // Return user ID
        } catch (NoSuchAlgorithmException e) {
            return new SimpleJSONResponse(0, "Server error during login", null);
        }
    }


    @Operation(summary = "Reset User Password", description = "Reset user password by providing their first pet name and birth city.")
    @PutMapping("/reset-password")
    public SimpleJSONResponse resetPassword(@RequestBody ResetPasswordRequest changePasswordReq) {
        Optional<UserInfo> userOpt = userInfoRepository.findByUsername(changePasswordReq.getUsername());

        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "User not found :((");
//            Shoutout Aarryaman for putting character into the return messages
        }

        UserInfo user = userOpt.get();

        try {

            String hashedBirthCity = getHashCode(changePasswordReq.getUsername(), changePasswordReq.getBirthCity());
            String hashedPetName = getHashCode(changePasswordReq.getUsername(), changePasswordReq.getFirstPetName());

            if (hashedBirthCity.equals(user.getBirthCity()) && hashedPetName.equals(user.getFirstPetName())) {
                if (user.getPassword().length() < 8) {
                    return new SimpleJSONResponse(0, "Password length required to be 8 or more.");
                }
                user.setPassword(getHashCode(user.getUsername(), changePasswordReq.getNewPassword()));
                userInfoRepository.save(user);
                return new SimpleJSONResponse(1, "Password successfully updated");
            }

            return new SimpleJSONResponse(0, "Incorrect credentials");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm is missing from UserController.resetPassword()");
        }
    }


    @Operation(summary = "Get user information", description = "Allows for user information to be accessed (only by user)")
    @GetMapping("/{userID}")
    public UserInfo getAllUserInfo(@PathVariable Long userID) {
        Optional<UserInfo> userOpt = userInfoRepository.findById(userID);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("user with id: " + userID + " not found.");
        }
        // remove important user information (even though it is hashed)
        UserInfo user = userOpt.get();
        user.setFirstPetName("");
        user.setBirthCity("");
        user.setPassword("");

        return user;
    }


    @Operation(summary = "Find if username is taken", description = "Allows for a username to be checked if it is available or in use")
    @GetMapping("/checkName/{username}")
    public SimpleJSONResponse doesNameExist(@PathVariable String username) {
        Optional<UserInfo> optUserName = userInfoRepository.findByUsername(username);
        if (optUserName.isPresent()) {
            return new SimpleJSONResponse(1, "Username already exists.");
        }
        return new SimpleJSONResponse(0, "Username is available.");
    }


    @Operation(summary = "Create a user", description = "Allow for the creation of a user and encryption of the sensitive information.")
    @PostMapping("/create")
    public SimpleJSONResponse createUser(@RequestBody UserInfo user) {

        try {

            if (!isValidEmail(user.getEmail())) {
                return new SimpleJSONResponse(0, "Please use proper email format.");
            }

            if (user.getUsername() == null) {
                throw new Exception();
            }
            Optional<UserInfo> optUserName = userInfoRepository.findByUsername(user.getUsername());
            if (optUserName.isPresent()) {
                return new SimpleJSONResponse(0, "Username already exists.");
            }

            if (user.getPassword().length() < 8) {
                return new SimpleJSONResponse(0, "Password length required to be 8 or more.");
            }
            user.setPassword(getHashCode(user.getUsername(), user.getPassword()));

            if (user.getBirthCity().isEmpty()) {
                return new SimpleJSONResponse(0, "Enter your place of birth.");
            }
            user.setBirthCity(getHashCode(user.getUsername(), user.getBirthCity()));

            if (user.getFirstPetName().isEmpty()) {
                return new SimpleJSONResponse(0, "Enter your first pets name.");
            }
            user.setFirstPetName(getHashCode(user.getUsername(), user.getFirstPetName()));

            userInfoRepository.save(user);

            return new SimpleJSONResponse(1, "User created successfully", user.getUserId());
        } catch (Exception e) {
            return new SimpleJSONResponse(0, "Server error in UserInfo.CreateUser()");
        }
    }


    @Operation(summary = "Delete a user", description = "Allow for the deletion of a user by their username and password")
    @DeleteMapping("/{username}/{userPassword}")
    public SimpleJSONResponse deleteUser(@PathVariable String username, @PathVariable String userPassword) {

        Optional<UserInfo> userOpt = userInfoRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return new SimpleJSONResponse(0, "User " + username + " not found");
        }
        UserInfo user = userOpt.get();

        try {
            String hashVal = getHashCode(username, userPassword);

            if (hashVal.equals(user.getPassword())) {
                userInfoRepository.deleteById(user.getUserId());
                return new SimpleJSONResponse(1, "User deleted successfully");

            } else {
                return new SimpleJSONResponse(0, "Invalid Credentials");
            }
        } catch (Exception e) {
            return new SimpleJSONResponse(0, "Server error in UserInfo.DeleteUser()");
        }
    }
}