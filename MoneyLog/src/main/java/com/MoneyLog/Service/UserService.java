package com.MoneyLog.Service;

import com.MoneyLog.DTO.UserDto;
import com.MoneyLog.Model.User;
import com.MoneyLog.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User signUp(UserDto inputData){
        userRepository.existsByEmail(inputData.getEmail());

        User user = User.builder()
                .email(inputData.getEmail())
                .userName(inputData.getUserName())
                .password(inputData.getPassword())
                .build();

        return userRepository.save(user);
    }
}
