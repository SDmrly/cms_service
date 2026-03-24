package com.cms.repository;

import com.cms.domain.entity.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, UUID> {

    Optional<Translation> findByCmsKeyIdAndLocale(UUID cmsKeyId, String locale);

    Optional<Translation> findByCmsKeyProjectCodeAndCmsKeyKeyAndLocale(String projectCode, String key, String locale);

    List<Translation> findByCmsKeyId(UUID cmsKeyId);
    
    List<Translation> findByCmsKeyProjectIdAndLocale(UUID projectId, String locale);

    List<Translation> findByCmsKeyProjectCodeAndLocale(String projectCode, String locale);
}
