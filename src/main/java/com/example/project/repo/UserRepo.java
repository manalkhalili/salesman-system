package com.example.project.repo;
import com.example.project.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserRepo extends JpaRepository<UserInfo, Integer> {
    boolean existsByEmail(String email);

    List<UserInfo> findByName(String name);


    UserInfo findByEmail(String email);


    List<UserInfo> findByRole(UserInfo.Role role);

    UserInfo findByPhoneNumber(String phoneNumber);

}
