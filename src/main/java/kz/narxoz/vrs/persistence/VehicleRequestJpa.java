package kz.narxoz.vrs.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

@Repository
public class VehicleRequestJpa {

    private final EntityManager em;

    public VehicleRequestJpa(EntityManager em) {
        this.em = em;
    }

    public void insert(VehicleRequestEntity request) {
        em.persist(request);
    }

    public int count(String businessKey) {
        Long n = em.createQuery("""
                        select count(r) from VehicleRequestEntity r
                        where r.businessKey = :key
                        """, Long.class)
                .setParameter("key", businessKey)
                .getSingleResult();
        return n.intValue();
    }

    public void flush() {
        em.flush();
    }
}
