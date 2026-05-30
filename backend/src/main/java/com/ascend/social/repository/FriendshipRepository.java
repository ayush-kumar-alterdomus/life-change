package com.ascend.social.repository;

import com.ascend.social.model.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("SELECT f FROM Friendship f WHERE f.user.id = :userId AND f.friend.id = :friendId")
    Optional<Friendship> findByUserIdAndFriendId(@Param("userId") UUID userId, @Param("friendId") UUID friendId);

    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :userId OR f.friend.id = :userId) " +
            "AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(@Param("userId") UUID userId);

    @Query("SELECT f FROM Friendship f WHERE f.friend.id = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingRequestsForUser(@Param("userId") UUID userId);

    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.user.id = :userId AND f.friend.id = :friendId) OR " +
            "(f.user.id = :friendId AND f.friend.id = :userId)")
    Optional<Friendship> findFriendshipBetween(@Param("userId") UUID userId, @Param("friendId") UUID friendId);

    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f " +
            "WHERE ((f.user.id = :userId AND f.friend.id = :friendId) OR " +
            "(f.user.id = :friendId AND f.friend.id = :userId)) " +
            "AND f.status = 'BLOCKED'")
    boolean isBlocked(@Param("userId") UUID userId, @Param("friendId") UUID friendId);
}
