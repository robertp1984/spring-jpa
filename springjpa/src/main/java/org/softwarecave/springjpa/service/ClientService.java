package org.softwarecave.springjpa.service;

import lombok.RequiredArgsConstructor;
import org.softwarecave.springjpa.common.UUIDGenerator;
import org.softwarecave.springjpa.model.Client;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(value = "transactionManager")
    public Client addClient(Client client) {
        if (client.getId() != null) {
            throw new DataValidationException("New client must have null key");
        }

        client.setId(UUIDGenerator.get());
        return clientRepository.save(client);
    }

    public List<Client> getClients() {
        return clientRepository.findAll(Sort.by(Sort.Order.asc("firstName")));
    }
}
