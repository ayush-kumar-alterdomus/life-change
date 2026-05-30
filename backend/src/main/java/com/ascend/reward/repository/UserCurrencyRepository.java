package com.ascend.reward.repository;

import com.ascend.reward.entity.UserCurrency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserCurrencyRepository extends JpaRepository<UserCurrency, UUID> {
    Optional<UserCurrency> findByUserId(UUID userId);
}
