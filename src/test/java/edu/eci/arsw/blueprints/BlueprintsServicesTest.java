package edu.eci.arsw.blueprints;


import edu.eci.arsw.blueprints.filters.BlueprintsFilter;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistence;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlueprintsServicesTest {

    @Mock
    private BlueprintPersistence persistence;

    @Mock
    private BlueprintsFilter filter;

    @InjectMocks
    private BlueprintsServices services;

    private Blueprint blueprint;

    @BeforeEach
    void setUp() {
        blueprint = new Blueprint(
                "oscar",
                "bp1",
                List.of(new Point(1, 2), new Point(3, 4))
        );
    }

    @Test
    void shouldAddNewBlueprint() throws Exception {

        services.addNewBlueprint(blueprint);

        verify(persistence, times(1)).saveBlueprint(blueprint);
    }

    @Test
    void shouldThrowExceptionWhenAddingBlueprintFails() throws Exception {

        doThrow(new BlueprintPersistenceException("error"))
                .when(persistence)
                .saveBlueprint(blueprint);

        assertThrows(BlueprintPersistenceException.class,
                () -> services.addNewBlueprint(blueprint));
    }

    @Test
    void shouldReturnAllFilteredBlueprints() {

        when(persistence.getAllBlueprints())
                .thenReturn(Set.of(blueprint));

        when(filter.apply(blueprint))
                .thenReturn(blueprint);

        Set<Blueprint> result = services.getAllBlueprints();

        assertEquals(1, result.size());
        verify(filter).apply(blueprint);
    }

    @Test
    void shouldReturnFilteredBlueprintsByAuthor() throws Exception {

        when(persistence.getBlueprintsByAuthor("oscar"))
                .thenReturn(Set.of(blueprint));

        when(filter.apply(blueprint))
                .thenReturn(blueprint);

        Set<Blueprint> result = services.getBlueprintsByAuthor("oscar");

        assertEquals(1, result.size());
        verify(filter).apply(blueprint);
    }

    @Test
    void shouldThrowExceptionWhenAuthorNotFound() throws Exception {

        when(persistence.getBlueprintsByAuthor("unknown"))
                .thenThrow(new BlueprintNotFoundException("not found"));

        assertThrows(BlueprintNotFoundException.class,
                () -> services.getBlueprintsByAuthor("unknown"));
    }

    @Test
    void shouldReturnFilteredBlueprint() throws Exception {

        when(persistence.getBlueprint("oscar", "bp1"))
                .thenReturn(blueprint);

        when(filter.apply(blueprint))
                .thenReturn(blueprint);

        Blueprint result = services.getBlueprint("oscar", "bp1");

        assertNotNull(result);
        verify(filter).apply(blueprint);
    }

    @Test
    void shouldThrowExceptionWhenBlueprintNotFound() throws Exception {

        when(persistence.getBlueprint("oscar", "bp1"))
                .thenThrow(new BlueprintNotFoundException("not found"));

        assertThrows(BlueprintNotFoundException.class,
                () -> services.getBlueprint("oscar", "bp1"));
    }

    @Test
    void shouldAddPoint() throws Exception {

        services.addPoint("oscar", "bp1", 5, 6);

        verify(persistence).addPoint("oscar", "bp1", 5, 6);
    }

    @Test
    void shouldThrowExceptionWhenAddingPointFails() throws Exception {

        doThrow(new BlueprintNotFoundException("not found"))
                .when(persistence)
                .addPoint("oscar", "bp1", 5, 6);

        assertThrows(BlueprintNotFoundException.class,
                () -> services.addPoint("oscar", "bp1", 5, 6));
    }
}