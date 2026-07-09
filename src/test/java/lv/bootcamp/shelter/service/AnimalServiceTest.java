package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.client.NotificationClient;
import lv.bootcamp.shelter.dto.AdoptionRequest;
import lv.bootcamp.shelter.dto.AnimalCreateRequest;
import lv.bootcamp.shelter.dto.AnimalResponse;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import lv.bootcamp.shelter.repository.AnimalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.Captor;


/**
 * Task: Service-layer tests with Mockito.
 *
 * Use @Mock, @InjectMocks, stubbing, verify(), and ArgumentCaptor.
 * Write Arrange-Act-Assert for each method.
 */
@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private AnimalRepository animalRepository;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private AnimalService animalService;

    @Captor
    private ArgumentCaptor<List<Long>> idsCaptor;

    @Test
    void create_shouldSaveAnimalWithAvailableStatus() {
        AnimalCreateRequest request = new AnimalCreateRequest(
                "Rex",
                AnimalType.DOG,
                "Labrador",
                3,
                "Friendly dog"
        );

        Animal savedAnimal = new Animal(
                1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                3,
                "Friendly dog",
                AnimalStatus.AVAILABLE
        );

        when(animalRepository.save(any(Animal.class))).thenReturn(savedAnimal);

        AnimalResponse response = animalService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Rex");
        assertThat(response.status()).isEqualTo(AnimalStatus.AVAILABLE);

        ArgumentCaptor<Animal> animalCaptor = ArgumentCaptor.forClass(Animal.class);
        verify(animalRepository).save(animalCaptor.capture());

        Animal capturedAnimal = animalCaptor.getValue();
        assertThat(capturedAnimal.getStatus()).isEqualTo(AnimalStatus.AVAILABLE);
        assertThat(capturedAnimal.getName()).isEqualTo("Rex");
    }

    @Test
    void findById_shouldThrowWhenAnimalNotFound() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.findById(99L))
                .isInstanceOf(AnimalNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void adopt_shouldChangeStatusAndSendNotification() {
        Animal existingAnimal = new Animal(
                1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                3,
                "Friendly dog",
                AnimalStatus.AVAILABLE
        );

        when(animalRepository.findById(1L)).thenReturn(Optional.of(existingAnimal));

        when(animalRepository.save(any(Animal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdoptionRequest adoptionRequest = new AdoptionRequest(
                1L,
                "John Doe",
                "john.doe@example.com"
        );

        AnimalResponse response = animalService.adopt(adoptionRequest);

        assertThat(response.status()).isEqualTo(AnimalStatus.ADOPTED);

        verify(notificationClient).sendAdoptionNotification(1L, "Rex", "john.doe@example.com");
    }

    @Test
    void adopt_shouldThrowWhenAnimalAlreadyAdopted() {
        Animal alreadyAdoptedAnimal = new Animal(
                1L,
                "Rex",
                AnimalType.DOG,
                "Labrador",
                3,
                "Friendly dog",
                AnimalStatus.ADOPTED
        );

        when(animalRepository.findById(1L)).thenReturn(Optional.of(alreadyAdoptedAnimal));

        AdoptionRequest adoptionRequest = new AdoptionRequest(
                1L,
                "John Doe",
                "john.doe@example.com"
        );

        assertThatThrownBy(() -> animalService.adopt(adoptionRequest))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(notificationClient);
    }

    @Test
    void reserveMultiple_shouldNotifyWithReservedIds() {
        Animal animal1 = new Animal(
                1L, "Rex", AnimalType.DOG, "Labrador", 3,
                "Friendly dog", AnimalStatus.AVAILABLE
        );
        Animal animal2 = new Animal(
                2L, "Luna", AnimalType.CAT, "Siamese", 2,
                "Playful cat", AnimalStatus.AVAILABLE
        );

        when(animalRepository.findAllById(List.of(1L, 2L)))
                .thenReturn(List.of(animal1, animal2));

        when(animalRepository.save(any(Animal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<AnimalResponse> responses = animalService.reserveMultiple(List.of(1L, 2L));

        assertThat(responses)
                .hasSize(2)
                .extracting(AnimalResponse::status)
                .containsOnly(AnimalStatus.RESERVED);

        verify(notificationClient).sendBulkStatusNotification(idsCaptor.capture(), anyString());

        assertThat(idsCaptor.getValue()).containsExactly(1L, 2L);
    }
}
