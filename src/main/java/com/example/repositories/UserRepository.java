package com.example.repositories;

import com.example.domains.Role;
import com.example.domains.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    @Query("select (count(u) > 0) from User u where u.chatId = ?1")
    boolean existsByUserId(String chatId);
    @Query("select u from User u where u.chatId = ?1")
    User findByChatId(String chatId);
    @Query("select count(*) from User u")
    long countUsers();
    @Query("select u.chatId from User u")
    List<String> findUsersChatId();
    @Transactional
    @Modifying
    @Query("update User u set u.role='ADMIN' where u.chatId = ?1")
    void setAdmin(String chatId);

    @Transactional
    @Modifying
    @Query("update User u set u.role='USER' where u.chatId = ?1")
    void deleteAdmin(String chatId);

    @Query("select u from User u where u.role = ?1")
    List<User> findAllByRole(Role role);

}