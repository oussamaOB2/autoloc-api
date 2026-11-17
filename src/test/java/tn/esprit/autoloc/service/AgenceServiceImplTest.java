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
    void findById_existing_returnsAgence() {
        Agence a = new Agence();
        a.setIdAgence(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(a));

        assertSame(a, service.findById(1L));
    }

    @Test
    void findById_missing_throwsResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(ResourceNotFoundException.class, () -> service.findById(99L));
        assertEquals("Agence introuvable avec l'id 99", ex.getMessage());
    }

    @Test
    void create_withBlankName_throwsIllegalArgument() {
        Agence a = new Agence();
        a.setNom("  ");

        assertThrows(IllegalArgumentException.class, () -> service.create(a));
        verify(repository, never()).save(any());
    }

    @Test
    void update_missingId_throwsResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.update(99L, new Agence()));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteById_existing_deletes() {
        Agence a = new Agence();
        a.setIdAgence(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(a));

        service.deleteById(1L);

        verify(repository).delete(a);
        verify(repository).flush();
    }

    @Test
    void deleteById_missing_throwsResourceNotFound() {
        when(repository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteById(5L));
        verify(repository, never()).delete(any());
        verify(repository, never()).flush();
    }
}
