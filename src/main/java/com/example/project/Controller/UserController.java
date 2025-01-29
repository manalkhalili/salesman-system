package com.example.project.Controller;

import com.example.project.entity.UserInfo;
import com.example.project.service.EmailService;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final EmailService emailService;
    private final Map<String, String> verificationCodes = new HashMap<>(); // تخزين الأكواد مؤقتًا

    @Autowired
    public UserController(UserService userService,EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;

    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserInfo user, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        if (user.getRole() == null) {
            user.setRole(UserInfo.Role.SALESMAN);  // Set default role if not provided
        }

        // Validate email format
        if(!isValidEmail(user.getEmail())){
            return ResponseEntity.badRequest().body("invalid email format");
        }

        if (!isValidPassword(user.getPassword())) {
            return ResponseEntity.badRequest().body("Password must contain at least 10 characters, including 1 uppercase letter, 1 lowercase letter, and 1 special character.");
        }


        // Validate phone number (must be exactly 10 digits)
        if (!isValidPhoneNumber(user.getPhoneNumber())) {
            return ResponseEntity.badRequest().body("Phone number must be exactly 10 digits.");
        }

        try {
            userService.registerUser(user);
            return ResponseEntity.ok("User registered successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }


    }

    @PostMapping("/signin")
    public ResponseEntity<?> signinUser(@RequestBody UserInfo loginRequest ){
        try{
            UserInfo user = userService.signInUser(loginRequest.getEmail(), loginRequest.getPassword());
            if(user != null){
                return ResponseEntity.ok("User signed in successfully.");
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");

            }

        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<UserInfo>> getAllUsers() {
        try {
            List<UserInfo> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/SalesMan")
    public ResponseEntity<List<UserInfo>> getSalesmen() {
        try {
            List<UserInfo> users = userService.getSalesmen();
            return ResponseEntity.ok(users);

        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }


    @GetMapping("/Accountent")
    public ResponseEntity<List<UserInfo>> getAccountent() {
        try{
            List<UserInfo>users=userService.getAccountants();
            return ResponseEntity.ok(users);
        }
        catch(Exception e){
            return ResponseEntity.badRequest().body(null);
        }
    }


    @GetMapping("/search")
    public ResponseEntity<List<UserInfo>> getUsersByName(@RequestParam String name) {
        try {
            List<UserInfo> users = userService.getUsersByName(name);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/byPhoneNumber")
    public ResponseEntity<UserInfo> getUserByPhoneNumber(@RequestParam String phoneNumber) {
        try {
            UserInfo user = userService.getUserByPhoneNumber(phoneNumber);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/byEmail")
    public ResponseEntity<UserInfo> getUserByEmail(@RequestParam String email) {
        try {
            UserInfo user = userService.getUserByEmail(email);
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean exists = userService.checkIfEmailExists(email);

        if (exists) {

            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Email not found");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private boolean isValidPassword(String password) {
        // Password should be at least 10 characters long, contain at least one uppercase letter, one lowercase letter, and one special character.
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@!#%]).{10,}$";
        return password.matches(passwordRegex);
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Phone number should be exactly 10 digits
        String phoneNumberRegex = "^\\d{10}$";
        return phoneNumber.matches(phoneNumberRegex);
    }
}
