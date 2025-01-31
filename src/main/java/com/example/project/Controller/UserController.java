package com.example.project.Controller;

import com.example.project.entity.UserInfo;
import com.example.project.repo.UserRepo;
import com.example.project.service.EmailService;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import utils.VerificationCodeUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    @Autowired
    private UserRepo userRepo;
    private final EmailService emailService;
    private final Map<String, String> verificationCodes = new HashMap<>(); // تخزين الأكواد مؤقتًا

    @Autowired
    public UserController(UserService userService,EmailService emailService,UserRepo userRepo) {
        this.userService = userService;
        this.emailService = emailService;
        this.userRepo = userRepo;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserInfo user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }

        // تعيين دور المستخدم الافتراضي
        if (user.getRole() == null) {
            user.setRole(UserInfo.Role.SALESMAN);
        }

        // التحقق من صحة البريد الإلكتروني وكلمة المرور ورقم الهاتف
        if (!isValidEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Invalid email format.");
        }

        if (!isValidPassword(user.getPassword())) {
            return ResponseEntity.badRequest().body("Password must contain at least 10 characters, including 1 uppercase letter, 1 lowercase letter, and 1 special character.");
        }

        if (!isValidPhoneNumber(user.getPhoneNumber())) {
            return ResponseEntity.badRequest().body("Phone number must be exactly 10 digits.");
        }

        try {
            // توليد كود التحقق وتخزينه في قاعدة البيانات
            String verificationCode = VerificationCodeUtil.generateCode();
            LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5); // صلاحية 5 دقائق

            user.setVerificationCode(verificationCode);
            user.setVerificationCodeExpiration(expirationTime);
            user.setVerified(false); // المستخدم غير مفعّل حتى يتم التحقق منه

            userRepo.save(user); // حفظ المستخدم في قاعدة البيانات

            // إرسال كود التحقق عبر البريد الإلكتروني
            emailService.sendVerificationEmail(user.getEmail(), verificationCode);

            return ResponseEntity.ok("User registered successfully. Please verify your email.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam String email, @RequestParam String code) {
        UserInfo user = userRepo.findByEmail(email);

        if (user != null) {
            // التحقق من صحة كود التحقق
            if (user.getVerificationCode() != null && user.getVerificationCode().equals(code)) {
                // التحقق مما إذا كان الكود قد انتهت صلاحيته
                if (user.getVerificationCodeExpiration().isAfter(LocalDateTime.now())) {
                    user.setVerified(true); // تحديث الحالة إلى مفعّل
                    user.setVerificationCode(null); // مسح الكود بعد التحقق
                    user.setVerificationCodeExpiration(null); // مسح تاريخ انتهاء الصلاحية

                    userRepo.save(user); // حفظ التغييرات في قاعدة البيانات

                    return ResponseEntity.ok("Email verified successfully. You can now log in.");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Verification code has expired.");
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid verification code.");
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
    }


    @PostMapping("/signin")
    public ResponseEntity<?> signinUser(@RequestBody UserInfo loginRequest) {
        try {
            UserInfo user = userService.signInUser(loginRequest.getEmail(), loginRequest.getPassword());

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password.");
            }

            if (!user.isVerified()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account not verified. Please check your email.");
            }

            return ResponseEntity.ok("User signed in successfully.");
        } catch (Exception e) {
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
