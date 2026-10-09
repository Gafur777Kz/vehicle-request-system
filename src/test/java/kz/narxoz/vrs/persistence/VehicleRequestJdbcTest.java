package kz.narxoz.vrs.persistence;

import kz.narxoz.vrs.config.Application;
import kz.narxoz.vrs.config.VehicleRequestService;
import kz.narxoz.vrs.domain.DuplicateVehicleRequest;
import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.domain.VehicleRequestNumber;
import kz.narxoz.vrs.domain.VehicleRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Против настоящего PostgreSQL (localhost:5432/css, css/css).
 * Специально БЕЗ @Transactional на классе и методах: count читается
 * из отдельной, уже закоммиченной транзакции, а не из той, что под тестом.
 */
@SpringBootTest(classes = Application.class)
class VehicleRequestJdbcTest {

    private static final VehicleRequestNumber VR_1042 = new VehicleRequestNumber("VR-1042");

    @Autowired
    private VehicleRequestService service;

    @Autowired
    private VehicleRequestJdbc requests;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() throws IOException {
        jdbc.execute("DROP TABLE IF EXISTS vehicle_request");
        jdbc.execute(Files.readString(Path.of("src/main/resources/db/schema.sql")));
    }

    @Test
    void secondStatementRollsBack() {
        assertThrows(DuplicateVehicleRequest.class,
                () -> service.insertTwice("VR-1042"));
        assertEquals(0, requests.count(VR_1042));
    }

    @Test
    void secondRequestKeepsTheFirst() {
        service.register(VehicleRequestId.newId(), "VR-1042", "Поездка в аэропорт");
        assertThrows(DuplicateVehicleRequest.class,
                () -> service.register(VehicleRequestId.newId(), "VR-1042", "Ещё раз"));
        assertEquals(1, requests.count(VR_1042));
    }

    @Test
    void statusIsStoredAsTextAndReadBack() {
        service.register(VehicleRequestId.newId(), "VR-7", "Командировка");
        assertEquals(Optional.of(VehicleRequestStatus.DRAFT),
                requests.findStatus(new VehicleRequestNumber("VR-7")));
    }

    @Test
    void blankNumberNeverReachesTheDatabase() {
        assertThrows(IllegalArgumentException.class,
                () -> service.register(VehicleRequestId.newId(), "   ", "Пусто"));
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM vehicle_request", Integer.class));
    }

    @Test
    void checkConstraintRejectsUnknownStatus() {
        assertThrows(DataIntegrityViolationException.class, () -> jdbc.update(
                "INSERT INTO vehicle_request (id, business_key, status, purpose) VALUES (?, ?, ?, ?)",
                UUID.randomUUID(), "VR-9", "CANCELLED", "Неизвестный статус"));
    }
}
