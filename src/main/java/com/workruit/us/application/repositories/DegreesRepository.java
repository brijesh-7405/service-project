package com.workruit.us.application.repositories;

import com.workruit.us.application.models.Degrees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DegreesRepository extends JpaRepository<Degrees, Integer> {

    @Query(value = "select * from degrees where title=?1 or SHORT_TITLE=?1", nativeQuery = true)
    Degrees findByShortTitle(String title);
}
