package org.example.cookingappbackend.service;

import org.example.cookingappbackend.dto.response.IngredientResponse;
import org.example.cookingappbackend.model.Ingredient;
import org.example.cookingappbackend.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    private final IngredientRepository repo = mock(IngredientRepository.class);
    private final IngredientService service = new IngredientService(repo);

    @Test
    void create_savesIngredient_andReturnsDtoWithId() {
        IngredientResponse dto = new IngredientResponse();
        dto.setName("Milk");
        dto.setUnit("ml");

        Ingredient saved = new Ingredient();
        saved.setId(10L);
        saved.setName("Milk");
        saved.setUnit("ml");

        when(repo.save(any(Ingredient.class))).thenReturn(saved);

        IngredientResponse result = service.create(dto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Milk");
        assertThat(result.getUnit()).isEqualTo("ml");

        ArgumentCaptor<Ingredient> captor = ArgumentCaptor.forClass(Ingredient.class);
        verify(repo).save(captor.capture());
        Ingredient toSave = captor.getValue();
        assertThat(toSave.getName()).isEqualTo("Milk");
        assertThat(toSave.getUnit()).isEqualTo("ml");

        verifyNoMoreInteractions(repo);
    }

    @Test
    void create_whenDuplicateName_throwsIllegalArgumentException() {
        IngredientResponse dto = new IngredientResponse();
        dto.setName("Milk");
        dto.setUnit("ml");

        when(repo.save(any(Ingredient.class))).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> service.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredient with this name already exists");

        verify(repo).save(any(Ingredient.class));
        verifyNoMoreInteractions(repo);
    }

    @Test
    void list_returnsAllFromRepo() {
        Ingredient i1 = new Ingredient();
        i1.setId(1L);
        Ingredient i2 = new Ingredient();
        i2.setId(2L);

        when(repo.findAll()).thenReturn(List.of(i1, i2));

        List<Ingredient> result = service.list();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);

        verify(repo).findAll();
        verifyNoMoreInteractions(repo);
    }
}
