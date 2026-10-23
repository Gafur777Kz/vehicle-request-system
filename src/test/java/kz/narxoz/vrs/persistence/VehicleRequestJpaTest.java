package kz.narxoz.vrs.persistence;

import kz.narxoz.vrs.config.Application;
import kz.narxoz.vrs.config.VehicleRequestService;
import kz.narxoz.vrs.domain.DuplicateVehicleRequest;
import kz.narxoz.vrs.domain.VehicleRequestId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// настоящий PostgreSQL localhost:5432/css, таблицу создаёт Flyway (V1)
// без @Transactional на тесте, иначе count увидит то, что потом откатится
@SpringBootTest(classes = Application.class)
class VehicleRequestJpaTest {

    @Autowired
    private VehicleRequestService service;

    @Autowired
    private VehicleRequestJpa requests;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void empty() {
        jdbc.update("DELETE FROM vehicle_request");
    }

    @Test
    void secondStatementRollsBack() {
        assertThrows(DuplicateVehicleRequest.class, () -> service.insertTwice("VR-1042"));
        assertEquals(0, requests.count("VR-1042"));
    }

    @Test
    void secondRequestKeepsTheFirst() {
        service.register(VehicleRequestId.newId(), "VR-1042", "Поездка в аэропорт");
        assertThrows(DuplicateVehicleRequest.class,
                () -> service.register(VehicleRequestId.newId(), "VR-1042", "Ещё раз"));
        assertEquals(1, requests.count("VR-1042"));
    }

    @Test
    void blankNumberThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(VehicleRequestId.newId(), "  ", "Пусто"));
    }
}
