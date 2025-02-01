package com.example.project.service;

import com.example.project.entity.UserInfo;
import com.example.project.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import utils.VerificationCodeUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;
    @Autowired
    private EmailService emailService;
    @Autowired
    public UserService(UserRepo userRepo){
        this.userRepo = userRepo;

    }
    public UserInfo registerUser (UserInfo user )  throws Exception{
        if(userRepo.existsByEmail(user.getEmail())){
            throw new Exception("Email already exists");

        }
        return userRepo.save(user);
    }

    public UserInfo signInUser(String email, String password) {
        UserInfo user = userRepo.findByEmail(email);

        if (user != null && user.getPassword().equals(password)) {
            return user;
        } else {
            return null;
        }
    }


    public List<UserInfo> getAllUsers() {
        return userRepo.findAll();
    }

    public List<UserInfo> getUsersByName(String name) {
        return userRepo.findByName(name);
    }

    public UserInfo getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Optional<UserInfo> getUserById(int id) {
        return userRepo.findById(id);
    }

    public boolean checkIfEmailExists(String email) {
        return userRepo.existsByEmail(email);
    }

    public UserInfo updateUser(UserInfo userInfo) {
        return userRepo.save(userInfo);
    }

    public UserInfo getUserByPhoneNumber(String phoneNumber) {
        return userRepo.findByPhoneNumber(phoneNumber);
    }


    public void deleteUser(int id) {
        userRepo.deleteById(id);
    }

    public List<UserInfo> getSalesmen() {
        return userRepo.findByRole(UserInfo.Role.SALESMAN);
    }

    public List<UserInfo> getAccountants() {
        return userRepo.findByRole(UserInfo.Role.ACCOUNTANT);
    }
    public void verifyUser(String email) {
        UserInfo user = userRepo.findByEmail(email);
        if (user != null) {
            user.setVerified(true);  // ✅ **تحديث حالة الحساب إلى "مفعل"**
            userRepo.save(user);
        }
    }
    public void sendVerificationEmail(String email) {
        String verificationCode = VerificationCodeUtil.generateCode();  // توليد كود التحقق
        LocalDateTime expirationTime = LocalDateTime.now().plusMinutes(5);  // تعيين فترة صلاحية الكود (5 دقائق)

        UserInfo user = userRepo.findByEmail(email);  // البحث عن المستخدم بالبريد الإلكتروني
        if (user != null) {
            user.setVerificationCode(verificationCode);  // تخزين الكود في قاعدة البيانات
            user.setVerificationCodeExpiration(expirationTime);  // تخزين تاريخ انتهاء صلاحية الكود
            userRepo.save(user);  // حفظ التغييرات في قاعدة البيانات
        }

        emailService.sendVerificationEmail(email, verificationCode);  // إرسال الكود عبر البريد الإلكتروني
    }

    public void sendPasswordResetCode(String email) {
        UserInfo user = userRepo.findByEmail(email);
        if(user ==null){
            throw new RuntimeException("User not found");
        }
        String resetCode = VerificationCodeUtil.generateCode();
        user.setResetCode(resetCode);
        userRepo.save(user);
        emailService.sendVerificationEmail(email, "Your Password Reset code is : " + resetCode);
    }
}
