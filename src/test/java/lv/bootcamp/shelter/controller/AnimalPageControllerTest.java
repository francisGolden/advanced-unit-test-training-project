package lv.bootcamp.shelter.controller;

import lv.bootcamp.shelter.dto.AnimalResponse;
import lv.bootcamp.shelter.model.AnimalStatus;
import lv.bootcamp.shelter.model.AnimalType;
import lv.bootcamp.shelter.service.AnimalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Task: View controller tests with MockMvc and @WebMvcTest.
 *
 * A @Controller returns a view name, not JSON.
 * Use view().name() and model().attribute() instead of jsonPath().
 * Use content().string(containsString(...)) to check rendered HTML.
 */
@WebMvcTest(AnimalPageController.class)
class AnimalPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAnimals_shouldRenderAnimalsView() throws Exception {
        when(animalService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/animals"))
                .andExpect(status().isOk())
                .andExpect(view().name("animals"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAnimals_shouldAddAnimalsToModel() throws Exception {
        AnimalResponse rex = new AnimalResponse(1L, "Rex", AnimalType.DOG, "Siamese", 2, "Calm", AnimalStatus.AVAILABLE);
        when(animalService.findAll()).thenReturn(List.of(rex));
        mockMvc.perform(get("/animals"))
                .andExpect(model().attribute("animals", List.of(rex)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAnimals_shouldRenderAnimalNameInHtml() throws Exception {
        AnimalResponse rex = new AnimalResponse(1L, "Rex", AnimalType.DOG, "Siamese", 2, "Calm", AnimalStatus.AVAILABLE);
        when(animalService.findAll()).thenReturn(List.of(rex));
        mockMvc.perform(get("/animals"))
                .andExpect(content().string(containsString("Rex")));

    }
}
