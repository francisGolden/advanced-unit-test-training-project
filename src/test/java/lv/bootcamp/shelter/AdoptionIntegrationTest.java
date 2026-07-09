package lv.bootcamp.shelter;

import lv.bootcamp.shelter.client.NotificationClient;
import lv.bootcamp.shelter.dto.AdoptionRequest;
import lv.bootcamp.shelter.dto.AnimalCreateRequest;
import lv.bootcamp.shelter.dto.AnimalResponse;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import lv.bootcamp.shelter.service.AnimalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// TODO: add imports as you write the test (e.g. assertThat, verify)

/**
 * Task: Integration test with @SpringBootTest.
 *
 * The full application context loads — use @MockitoBean only for the external
 * NotificationClient. Everything else (service, repository, JPA) is real.
 * @Transactional rolls back after each test.
 */
@SpringBootTest
@Transactional
class AdoptionIntegrationTest {

    @Autowired
    private AnimalService animalService;

    @MockitoBean
    private NotificationClient notificationClient;

    @Test
    void adoptionFlow_shouldPersistStatusAndNotifyExternalSystem() {
        AnimalCreateRequest request = new AnimalCreateRequest("Millie", AnimalType.CAT, "Siamese", 2, "Nice cat.");
        AnimalResponse animalResponse = animalService.create(request);
        assertThat(animalResponse.status()).isEqualTo(AnimalStatus.AVAILABLE);
        AdoptionRequest adoptionRequest = new AdoptionRequest(
                1L,
                "John Doe",
                "john.doe@email.com"
        );
        AnimalResponse response = animalService.adopt(adoptionRequest);
        assertThat(response.status()).isEqualTo(AnimalStatus.ADOPTED);
        verify(notificationClient, times(1))
                .sendAdoptionNotification(response.id(), response.name(), adoptionRequest.adopterEmail());
        assertThat(animalService.findById(response.id()).status()).isEqualTo(AnimalStatus.ADOPTED);
    }
}
