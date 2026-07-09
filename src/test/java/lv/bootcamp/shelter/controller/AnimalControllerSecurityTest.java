package lv.bootcamp.shelter.controller;

import lv.bootcamp.shelter.config.SecurityConfig;
import lv.bootcamp.shelter.service.AnimalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Task: Testing role-based endpoint security with MockMvc.
 * <p>
 * DELETE /api/animals/{id} is restricted to ROLE_ADMIN (see SecurityConfig).
 * Use @WithMockUser to fake an authenticated principal for a test method;
 * omit it to simulate an anonymous (unauthenticated) request.
 * <p>
 * Note: @WebMvcTest does not load your custom SecurityConfig automatically
 * (it only scans controllers/filters, not regular @Configuration classes) —
 * that's why it's pulled in explicitly with @Import below.
 */
@WebMvcTest(AnimalController.class)
@Import(SecurityConfig.class)
class AnimalControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteAnimal() throws Exception {
        animalService.delete(1L);
        doNothing().when(animalService).delete(1L);

        mockMvc.perform(delete("/api/animals/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotDeleteAnimal() throws Exception {
        mockMvc.perform(delete("/api/animals/1")).andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(delete("/api/animals/1")).andExpect(status().isUnauthorized());

    }
}
