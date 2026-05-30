package com.ascend.boss.repository;

import com.ascend.boss.entity.Boss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BossRepository extends JpaRepository<Boss, UUID> {

    List<Boss> findByGuildBossFalse();

    List<Boss> findByGuildBossTrue();
}
