package tn.esprit.autoloc.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.exception.GlobalExceptionHandler;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.service.IAgenceService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AgenceControllerTest {

    @Mock
    IAgenceService service;

    MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new AgenceController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void get_unknownId_returns404WithMessage() throws Exception {
        when(service.retrieve(99L)).thenThrow(new ResourceNotFoundException("Agence", "99"));

        mvc.perform(get("/api/agences/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Agence introuvable avec l'id 99"));
    }

    @Test
    void post_validBody_returns201() throws Exception {
        Agence saved = new Agence();
        saved.setIdAgence(1L);
        saved.setNom("Tunis Centre");
        when(service.add(any(Agence.class))).thenReturn(saved);

        mvc.perform(post("/api/agences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"Tunis Centre\",\"ville\":\"Tunis\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAgence").value(1))
                .andExpect(jsonPath("$.nom").value("Tunis Centre"));
    }

    @Test
    void post_blankName_returns400() throws Exception {
        mvc.perform(post("/api/agences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nom\":\"\",\"ville\":\"Tunis\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(service, never()).add(any());
    }

    @Test
    void delete_existing_returns204() throws Exception {
        mvc.perform(delete("/api/agences/1"))
                .andExpect(status().isNoContent());

        verify(service).remove(1L);
    }
}
