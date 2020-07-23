package uk.ac.ebi.ega.permissions.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class PermissionsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("OK Response when request sent to /{accountId}/permissions endpoint")
    public void shouldReturnOkOnProjectsEndpoint() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/sample-id/permissions"))
                .andExpect(status().isOk());
    }

}