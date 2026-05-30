package com.ascend.social.repository;

import com.ascend.social.model.AccountabilityPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountabilityPartnerRepository extends JpaRepository<AccountabilityPartner, UUID> {

    /**
     * Finds an active accountability partnership where the given user is either
     * the user or the partner.
     */
    @Query("SELECT ap FROM AccountabilityPartner ap WHERE " +
            "(ap.user.id = :userId OR ap.partner.id = :userId) " +
            "AND ap.active = true")
    Optional<AccountabilityPartner> findActivePartnershipByUserId(@Param("userId") UUID userId);

    /**
     * Checks if a partnership already exists between two users (in either direction).
     */
    @Query("SELECT ap FROM AccountabilityPartner ap WHERE " +
            "(ap.user.id = :userId AND ap.partner.id = :partnerId) OR " +
            "(ap.user.id = :partnerId AND ap.partner.id = :userId)")
    Optional<AccountabilityPartner> findPartnershipBetween(@Param("userId") UUID userId,
                                                           @Param("partnerId") UUID partnerId);
}
