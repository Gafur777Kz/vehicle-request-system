package kz.narxoz.vrs.domain;

/**
 * Заявка с таким номером уже есть.
 * Unchecked специально: исключение, вышедшее из @Transactional-метода, откатывает транзакцию.
 */
public class DuplicateVehicleRequest extends RuntimeException {

    public DuplicateVehicleRequest(VehicleRequestNumber number) {
        super("duplicate vehicle request: " + number);
    }
}
