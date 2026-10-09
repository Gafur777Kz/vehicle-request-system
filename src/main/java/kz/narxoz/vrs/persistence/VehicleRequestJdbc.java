package kz.narxoz.vrs.persistence;

import kz.narxoz.vrs.domain.DuplicateVehicleRequest;
import kz.narxoz.vrs.domain.VehicleRequestId;
import kz.narxoz.vrs.domain.VehicleRequestNumber;
import kz.narxoz.vrs.domain.VehicleRequestStatus;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Весь SQL живёт здесь. Наружу не выходят ни ResultSet, ни Connection.
 * Параметры только через "?", строки в SQL не склеиваются.
 */
@Repository
public class VehicleRequestJdbc {

    private static final String INSERT = """
            INSERT INTO vehicle_request (id, business_key, status, purpose)
            VALUES (?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbc;

    public VehicleRequestJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * @throws DuplicateVehicleRequest если номер уже занят (UNIQUE, SQLState 23505)
     */
    public void insert(VehicleRequestId id, VehicleRequestNumber number,
                       VehicleRequestStatus status, String purpose) {
        try {
            jdbc.update(INSERT, id.asUuid(), number.value(), StatusColumn.toText(status), purpose);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateVehicleRequest(number);
        }
    }

    public int count(VehicleRequestNumber number) {
        Integer n = jdbc.queryForObject(
                "SELECT count(*) FROM vehicle_request WHERE business_key = ?",
                Integer.class,
                number.value());
        return n == null ? 0 : n;
    }

    public Optional<VehicleRequestStatus> findStatus(VehicleRequestNumber number) {
        List<String> rows = jdbc.queryForList(
                "SELECT status FROM vehicle_request WHERE business_key = ?",
                String.class,
                number.value());
        return rows.stream().findFirst().map(StatusColumn::fromText);
    }
}
