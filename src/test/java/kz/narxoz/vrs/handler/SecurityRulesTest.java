package kz.narxoz.vrs.handler;

import kz.narxoz.vrs.config.Application;
import kz.narxoz.vrs.config.SecurityConfig;
import kz.narxoz.vrs.config.VehicleRequestService;
import kz.narxoz.vrs.domain.VehicleRequest;
import kz.narxoz.vrs.domain.VehicleRequestId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static kz.narxoz.vrs.domain.VehicleRequestStatus.APPROVED;
import static kz.narxoz.vrs.domain.VehicleRequestStatus.SUBMITTED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// правила из таблицы README: нет токена -> 401, EMPLOYEE на approve -> 403, MANAGER -> 200
// фильтры включены, сервис замокан, базы нет
@WebMvcTest(controllers = {VehicleRequestController.class, MeController.class})
@ContextConfiguration(classes = Application.class)
@Import(SecurityConfig.class)
class SecurityRulesTest {

    private static final String URL = "/api/v1/vehicle-requests/";
    private static final UUID ID = UUID.randomUUID();

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private VehicleRequestService service;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // jwt() - как будто пришёл проверенный токен с этим именем и ролью. Без jwt() - аноним
    private static RequestPostProcessor employee() {
        return jwt().jwt(j -> j.subject("employee")).authorities(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
    }

    private static RequestPostProcessor manager() {
        return jwt().jwt(j -> j.subject("manager")).authorities(new SimpleGrantedAuthority("ROLE_MANAGER"));
    }

    @Test
    void noTokenGets401() throws Exception {
        mvc.perform(get(URL + ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("valid token required"));

        verifyNoInteractions(service);
    }

    @Test
    void employeeCanRead() throws Exception {
        when(service.get(ID)).thenReturn(
                new VehicleRequest(new VehicleRequestId(ID.toString()), "VR-1042", SUBMITTED, "Поездка в аэропорт"));

        mvc.perform(get(URL + ID).with(employee()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUBMITTED"));
    }

    @Test
    void employeeCannotApprove() throws Exception {
        mvc.perform(post(URL + ID + "/approve").with(employee()).header("X-Correlation-Id", "sec-1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("not allowed for your role"))
                .andExpect(jsonPath("$.correlationId").value("sec-1"));

        verifyNoInteractions(service);
    }

    @Test
    void managerCanApprove() throws Exception {
        when(service.approve(ID, "manager")).thenReturn(
                new VehicleRequest(new VehicleRequestId(ID.toString()), "VR-1042", APPROVED, "Поездка в аэропорт"));

        mvc.perform(post(URL + ID + "/approve").with(manager()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        // имя взято из токена, а не из JSON
        verify(service).approve(ID, "manager");
    }

    @Test
    void approveWithoutTokenGets401() throws Exception {
        mvc.perform(post(URL + ID + "/approve"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    @Test
    void meReturnsUsernameFromToken() throws Exception {
        mvc.perform(get("/api/me").with(employee()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("employee"))
                .andExpect(jsonPath("$.authorities[0]").value("ROLE_EMPLOYEE"));
    }

    @Test
    void meWithoutTokenGets401() throws Exception {
        mvc.perform(get("/api/me"))
                .andExpect(status().isUnauthorized());
    }

    // всё, чего нет в таблице, закрыто даже с токеном (denyAll)
    @Test
    void anythingElseIsForbidden() throws Exception {
        mvc.perform(get("/actuator/env").with(manager()))
                .andExpect(status().isForbidden());
    }
}
