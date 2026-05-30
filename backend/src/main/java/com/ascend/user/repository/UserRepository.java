package com.ascend.user.repository;

import com.ascend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findByTimezoneIn(List<String> timezones);

    List<User> findByHardModeTrue();

    Page<User> findByFlaggedTrue(Pageable pageable);

    long countByLastActiveAfter(LocalDateTime dateTime);

    long countByPremiumTrue();
}
