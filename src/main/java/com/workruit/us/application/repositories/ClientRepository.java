package com.workruit.us.application.repositories;

import com.workruit.us.application.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ClientRepository extends JpaRepository<Client, Long> {

    @Transactional
    @Modifying
    @Query(value = "DELETE from client  WHERE client_id=?1", nativeQuery = true)
    void deleteClientById(Long id);
}
