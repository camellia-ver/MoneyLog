package com.MoneyLog.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 15;

    private final Map<String, Integer> attemptCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lockedUntil = new ConcurrentHashMap<>();

    public void loginFailed(String email){
        int attempts = attemptCounts.getOrDefault(email, 0) + 1;
        attemptCounts.put(email, attempts);

        if (attempts >= MAX_ATTEMPTS){
            lockedUntil.put(email, LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        }
    }

    public void loginSucceeded(String email){
        attemptCounts.remove(email);
        lockedUntil.remove(email);
    }

    public boolean isLocked(String email){
        LocalDateTime lockTime = lockedUntil.get(email);
        if (lockTime == null){
            return false;
        }

        if (LocalDateTime.now().isAfter(lockTime)){
            loginSucceeded(email);
            return false;
        }

        return true;
    }
}
