package com.ascend.quest.repository;

import com.ascend.common.entity.Frequency;
import com.ascend.common.entity.StatType;
import com.ascend.quest.entity.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestRepository extends JpaRepository<Quest, UUID> {

    List<Quest> findByArcId(UUID arcId);

    List<Quest> findByCreatedBy_Id(UUID userId);

    List<Quest> findByRecurringTrue();

    long countByCreatedBy_IdAndCustomTrue(UUID userId);

    List<Quest> findByArcIdAndFrequency(UUID arcId, Frequency frequency);

    List<Quest> findByStatTypeInAndFrequencyAndCustomFalse(List<StatType> statTypes, Frequency frequency);

    List<Quest> findByFrequencyAndCustomFalse(Frequency frequency);
}
