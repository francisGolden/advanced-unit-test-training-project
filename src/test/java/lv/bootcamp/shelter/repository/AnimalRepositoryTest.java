package lv.bootcamp.shelter.repository;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Task: Repository tests with @DataJpaTest.
 *
 * Use entityManager.persist() + entityManager.flush() to set up test data.
 * Each test rolls back automatically — no cleanup needed.
 */
@DataJpaTest
class AnimalRepositoryTest {

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_shouldPersistAnimalAndGenerateId() {
        Animal animalToSave = new Animal(
                null, "Rex", AnimalType.DOG, "Labrador", 3,
                "Friendly dog", AnimalStatus.AVAILABLE
        );

        Animal result = animalRepository.save(animalToSave);

        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Rex");
    }

    @Test
    void findByStatus_shouldReturnOnlyMatchingAnimals() {
        Animal available1 = new Animal(
                null, "Rex", AnimalType.DOG, "Labrador", 3,
                "Friendly dog", AnimalStatus.AVAILABLE
        );
        Animal available2 = new Animal(
                null, "Luna", AnimalType.CAT, "Siamese", 2,
                "Playful cat", AnimalStatus.AVAILABLE
        );
        Animal adopted = new Animal(
                null, "Buddy", AnimalType.DOG, "Beagle", 5,
                "Already adopted", AnimalStatus.ADOPTED
        );

        entityManager.persist(available1);
        entityManager.persist(available2);
        entityManager.persist(adopted);
        entityManager.flush();

        List<Animal> result = animalRepository.findByStatus(AnimalStatus.AVAILABLE);

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Animal::getName)
                .containsExactlyInAnyOrder("Rex", "Luna");
        assertThat(result)
                .extracting(Animal::getStatus)
                .containsOnly(AnimalStatus.AVAILABLE);
    }

    @Test
    void findByType_shouldReturnAnimalsOfGivenType() {
        Animal dog = new Animal(
                null, "Rex", AnimalType.DOG, "Labrador", 3,
                "Friendly dog", AnimalStatus.AVAILABLE
        );
        Animal cat = new Animal(
                null, "Luna", AnimalType.CAT, "Siamese", 2,
                "Playful cat", AnimalStatus.AVAILABLE
        );

        entityManager.persist(dog);
        entityManager.persist(cat);
        entityManager.flush();

        List<Animal> result = animalRepository.findByType(AnimalType.DOG);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Rex");
        assertThat(result.get(0).getType()).isEqualTo(AnimalType.DOG);
    }

    @Test
    void findByNameContainingIgnoreCase_shouldMatchPartialName() {
        Animal rex = new Animal(
                null, "Rex", AnimalType.DOG, "Labrador", 3,
                "Friendly dog", AnimalStatus.AVAILABLE
        );
        Animal rexyJr = new Animal(
                null, "Rexy Jr", AnimalType.DOG, "Labrador", 1,
                "Rex's puppy", AnimalStatus.AVAILABLE
        );
        Animal mia = new Animal(
                null, "Mia", AnimalType.CAT, "Siamese", 2,
                "Playful cat", AnimalStatus.AVAILABLE
        );

        entityManager.persist(rex);
        entityManager.persist(rexyJr);
        entityManager.persist(mia);
        entityManager.flush();

        List<Animal> result = animalRepository.findByNameContainingIgnoreCase("rex");

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Animal::getName)
                .containsExactlyInAnyOrder("Rex", "Rexy Jr");
    }
}
