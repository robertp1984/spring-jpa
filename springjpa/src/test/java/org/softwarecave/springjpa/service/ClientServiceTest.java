package org.softwarecave.springjpa.service;

import org.junit.jupiter.api.Test;
import org.softwarecave.springjpa.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("it")
@Transactional(value = "transactionManager")
@Rollback
class ClientServiceTest {

    @Autowired
    private ClientService clientService;

    @Test
    void testAddAndGetMany() {
        clientService.addClient(new Client(null, "John", "Blue"));
        clientService.addClient(new Client(null, "Jack", "Yellow"));
        clientService.addClient(new Client(null, "Joanna", "Smith"));
        clientService.addClient(new Client(null, "George", "Black"));

        List<Client> clients = clientService.getClients();
        assertThat(clients)
                .hasSize(4);

        assertThat(clients.get(0))
                .isNotNull()
                .hasFieldOrPropertyWithValue("firstName", "George")
                .hasFieldOrPropertyWithValue("lastName", "Black");
        assertThat(clients.get(1))
                .isNotNull()
                .hasFieldOrPropertyWithValue("firstName", "Jack")
                .hasFieldOrPropertyWithValue("lastName", "Yellow");
        assertThat(clients.get(2))
                .isNotNull()
                .hasFieldOrPropertyWithValue("firstName", "Joanna")
                .hasFieldOrPropertyWithValue("lastName", "Smith");
        assertThat(clients.get(3))
                .isNotNull()
                .hasFieldOrPropertyWithValue("firstName", "John")
                .hasFieldOrPropertyWithValue("lastName", "Blue");
    }
}
