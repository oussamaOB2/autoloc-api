package tn.esprit.autoloc.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.autoloc.domain.Agence;
import tn.esprit.autoloc.exception.ResourceNotFoundException;
import tn.esprit.autoloc.repository.IAgenceRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgenceServiceImplTest {

    @Mock
    IAgenceRepository repository;

    @InjectMocks
    AgenceServiceImpl service;

    @Test
    void retrieve_existing_returnsAgence() {
        Agence a = new Agence();
        a.setIdAgence(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(a));

        assertSame(a, service.retrieve(1L));
    }

    @Test
    void retrieve_missing_throwsResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> service.retrieve(99L));
        assertEquals("Agence introuvable avec l'id 99", ex.getMessage());
    }

    @Test
    void add_withBlankName_throwsIllegalArgument() {
        Agence a = new Agence();
        a.setNom("  ");

        assertThrows(IllegalArgumentException.class, () -> service.add(a));
        verify(repository, never()).save(any());
    }

    @Test
    void modify_missingId_throwsResourceNotFound() {
        Agence a = new Agence(); // id == null

        assertThrows(ResourceNotFoundException.class, () -> service.modify(a));
        verify(repository, never()).save(any());
    }

    @Test
    void remove_existing_deletes() {
        when(repository.existsById(1L)).thenReturn(true);

        service.remove(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void remove_missing_throwsResourceNotFound() {
        when(repository.existsById(5L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.remove(5L));
        verify(repository, never()).deleteById(any());
    }
}
