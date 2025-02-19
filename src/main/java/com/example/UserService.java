package com.example;

import com.example.domains.Role;
import com.example.domains.User;
import com.example.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;



    public String getUserLanguage(String userId) {
      return userRepository.findByChatId(userId).getLang();
   }

   public User updateLanguageUser(String userId, String language){
        User user=userRepository.findByChatId(userId);
        user.setLang(language);
        return userRepository.save(user);
   }

    public boolean isAdmin(String chatId) {
        User byChatId = userRepository.findByChatId(chatId);
        return byChatId.getRole().equals(Role.ADMIN);
    }
}
