package edu.eci.arsw.blueprints;


import edu.eci.arsw.blueprints.controllers.BlueprintsAPIController;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlueprintsAPIController.class)
class BlueprintsAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlueprintsServices services;

    @Test
    void shouldReturnAllBlueprints() throws Exception {

        Blueprint bp = new Blueprint("Andres", "house",
                List.of(new Point(1, 2)));

        when(services.getAllBlueprints())
                .thenReturn(Set.of(bp));

        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].author").value("Andres"));
    }


    @Test
    void shouldReturnBlueprintsByAuthor() throws Exception {

        Blueprint bp = new Blueprint("Andres", "house",
                List.of(new Point(1, 2)));

        when(services.getBlueprintsByAuthor("Andres"))
                .thenReturn(Set.of(bp));

        mockMvc.perform(get("/api/v1/blueprints/Andres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void shouldReturn404WhenAuthorNotFound() throws Exception {

        when(services.getBlueprintsByAuthor("unknown"))
                .thenThrow(new BlueprintNotFoundException("not found"));

        mockMvc.perform(get("/api/v1/blueprints/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }


    @Test
    void shouldReturnBlueprintByAuthorAndName() throws Exception {

        Blueprint bp = new Blueprint("Andres", "house",
                List.of(new Point(1, 2)));

        when(services.getBlueprint("Andres", "house"))
                .thenReturn(bp);

        mockMvc.perform(get("/api/v1/blueprints/Andres/house"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("house"));
    }

    @Test
    void shouldReturn404WhenBlueprintNotFound() throws Exception {

        when(services.getBlueprint("Andres", "house"))
                .thenThrow(new BlueprintNotFoundException("not found"));

        mockMvc.perform(get("/api/v1/blueprints/Andres/house"))
                .andExpect(status().isNotFound());
    }


    @Test
    void shouldCreateBlueprint() throws Exception {

        String json = """
                {
                  "author": "Andres",
                  "name": "house",
                  "points": [
                    { "x": 1, "y": 2 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201));
    }

    @Test
    void shouldReturn403WhenBlueprintAlreadyExists() throws Exception {

        doThrow(new BlueprintPersistenceException("exists"))
                .when(services)
                .addNewBlueprint(any());

        String json = """
                {
                  "author": "Andres",
                  "name": "house",
                  "points": [
                    { "x": 1, "y": 2 }
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void shouldAddPoint() throws Exception {

        String json = """
                {
                  "x": 10,
                  "y": 20
                }
                """;

        mockMvc.perform(put("/api/v1/blueprints/Andres/house/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(202));
    }

    @Test
    void shouldReturn404WhenAddingPointFails() throws Exception {

        doThrow(new BlueprintNotFoundException("not found"))
                .when(services)
                .addPoint("Andres", "house", 10, 20);

        String json = """
                {
                  "x": 10,
                  "y": 20
                }
                """;

        mockMvc.perform(put("/api/v1/blueprints/Andres/house/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}