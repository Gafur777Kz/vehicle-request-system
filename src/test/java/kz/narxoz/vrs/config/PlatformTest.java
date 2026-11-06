package kz.narxoz.vrs.config;

import kz.narxoz.vrs.client.DownstreamProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import kz.narxoz.vrs.persistence.VehicleRequestJpa;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// профиль dev: адрес соседа и timeout приходят из yaml, health открыт, env закрыт
// сеть на 9090 не трогаем
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PlatformTest {

    @Autowired
    private DownstreamProperties downstream;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private VehicleRequestJpa requests;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void downstreamComesFromDevYaml() {
        assertEquals(URI.create("http://localhost:9090"), downstream.baseUrl());
        assertEquals(Duration.ofSeconds(2), downstream.timeout());
    }

    @Test
    void healthIsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void envAndHeapdumpAreHidden() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/heapdump")).andExpect(status().isNotFound());
    }

    // весь путь: HTTP -> сервис -> PostgreSQL. Второй POST того же номера = 409, строка одна
    @Test
    void secondPostOfSameKeyIs409AndOneRowStays() throws Exception {
        jdbc.update("DELETE FROM vehicle_request");
        String body = "{\"businessKey\":\"VR-2001\",\"purpose\":\"Поездка в аэропорт\"}";

        mvc.perform(post("/api/v1/vehicle-requests").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/vehicle-requests").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));

        assertEquals(1, requests.count("VR-2001"));
    }
}
