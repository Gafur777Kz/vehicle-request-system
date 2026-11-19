package kz.narxoz.vrs.config;

import com.jayway.jsonpath.JsonPath;
import kz.narxoz.vrs.client.DownstreamProperties;
import kz.narxoz.vrs.persistence.VehicleRequestJpa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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

// всё приложение на профиле dev с настоящим PostgreSQL и настоящим логином
// сеть на 9090 не трогаем
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PlatformTest {

    private static final String URL = "/api/v1/vehicle-requests";

    @Autowired
    private DownstreamProperties downstream;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private VehicleRequestJpa requests;

    @Autowired
    private JdbcTemplate jdbc;

    // настоящий логин: POST /api/v1/auth/login -> "Bearer eyJ..."
    private String bearer(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + JsonPath.read(body, "$.accessToken");
    }

    @Test
    void downstreamComesFromDevYaml() {
        assertEquals(URI.create("http://localhost:9090"), downstream.baseUrl());
        assertEquals(Duration.ofSeconds(2), downstream.timeout());
    }

    @Test
    void healthIsOpenWithoutToken() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    // env и heapdump по-прежнему закрыты: без токена 401
    @Test
    void envAndHeapdumpAreClosed() throws Exception {
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
        mvc.perform(get("/actuator/heapdump")).andExpect(status().isUnauthorized());
    }

    // неизвестный пользователь и неверный пароль - один и тот же 401
    @Test
    void badLoginIsTheSame401() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"employee\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("bad username or password"));

        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nobody\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("bad username or password"));
    }

    // весь путь: HTTP -> сервис -> PostgreSQL. Второй POST того же номера = 409, строка одна
    @Test
    void secondPostOfSameKeyIs409AndOneRowStays() throws Exception {
        jdbc.update("DELETE FROM vehicle_request");
        String token = bearer("employee", "employee-dev");
        String body = "{\"businessKey\":\"VR-2001\",\"purpose\":\"Поездка в аэропорт\"}";

        mvc.perform(post(URL).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mvc.perform(post(URL).header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE"));

        assertEquals(1, requests.count("VR-2001"));
    }

    // путь проверяющего: сотрудник создаёт и отправляет, approve -> 403 и статус не меняется,
    // руководитель согласует -> APPROVED
    @Test
    void employeeGets403AndManagerApproves() throws Exception {
        jdbc.update("DELETE FROM vehicle_request");
        String employee = bearer("employee", "employee-dev");
        String manager = bearer("manager", "manager-dev");

        String created = mvc.perform(post(URL).header("Authorization", employee)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessKey\":\"VR-3001\",\"purpose\":\"Поездка в аэропорт\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");

        mvc.perform(post(URL + "/" + id + "/submit").header("Authorization", employee))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        mvc.perform(post(URL + "/" + id + "/approve").header("Authorization", employee))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mvc.perform(get(URL + "/" + id).header("Authorization", employee))
                .andExpect(jsonPath("$.status").value("SUBMITTED"));

        mvc.perform(post(URL + "/" + id + "/approve").header("Authorization", manager))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    // руководитель, но правило статусов не пускает: DRAFT нельзя сразу согласовать
    @Test
    void managerCannotApproveDraft() throws Exception {
        jdbc.update("DELETE FROM vehicle_request");
        String manager = bearer("manager", "manager-dev");

        String created = mvc.perform(post(URL).header("Authorization", manager)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessKey\":\"VR-3002\",\"purpose\":\"Поездка в аэропорт\"}"))
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(created, "$.id");

        mvc.perform(post(URL + "/" + id + "/approve").header("Authorization", manager))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TRANSITION_NOT_ALLOWED"));
        mvc.perform(get(URL + "/" + id).header("Authorization", manager))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }
}
