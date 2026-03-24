package com.cms.repository;

import com.cms.domain.entity.CmsKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CmsKeyRepository extends JpaRepository<CmsKey, UUID> {

    List<CmsKey> findByProjectIdAndCategory(UUID projectId, String category);

    Optional<CmsKey> findByProjectCodeAndKey(String projectCode, String key);

    List<CmsKey> findByProjectId(UUID projectId);
    
    List<CmsKey> findByProjectCode(String projectCode);

    boolean existsByProjectIdAndKey(UUID projectId, String key);
}
