package com.workruit.us.application.repositories;

import com.workruit.us.application.models.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByApplicantId(Long applicantId);

    @Transactional
    @Modifying
    @Query(value = "UPDATE certification SET upload_certificate=?2 WHERE certification_id=?1", nativeQuery=true)
    void updateCertificateKey(Long id, String key);
}
