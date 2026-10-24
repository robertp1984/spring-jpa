package org.softwarecave.springjpa.service;

import java.util.UUID;
import org.softwarecave.springjpa.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, UUID> {}
