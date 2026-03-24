package com.cms.repository;

import com.cms.domain.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    
    Optional<Project> findByCode(String code);
    
    List<Project> findByActiveTrue();
    
    boolean existsByCode(String code);
}
