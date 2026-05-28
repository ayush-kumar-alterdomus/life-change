package com.ascend.arc.repository;

import com.ascend.arc.entity.Arc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArcRepository extends JpaRepository<Arc, UUID> {

    List<Arc> findByPrebuiltTrue();
}
