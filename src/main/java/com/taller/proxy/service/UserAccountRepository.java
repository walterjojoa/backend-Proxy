package com.taller.proxy.service;

import com.taller.proxy.model.UserAccount;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory storage of user accounts. A new account (FREE plan) is created
 * the first time a user id is seen.
 */
@Repository
public class UserAccountRepository {

    private final Map<String, UserAccount> accounts = new ConcurrentHashMap<>();

    public UserAccount getOrCreate(String userId) {
        return accounts.computeIfAbsent(userId, UserAccount::new);
    }

    public Collection<UserAccount> findAll() {
        return accounts.values();
    }
}
