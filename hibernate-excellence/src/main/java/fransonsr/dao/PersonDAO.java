package fransonsr.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

import fransonsr.model.Person;

@Repository
public class PersonDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void create(Person person) {
        entityManager.persist(person);
    }

    public Person read(Long id) {
        return entityManager.find(Person.class, id);
    }

    public void delete(Person person) {
        entityManager.remove(person);
    }

    public Person reference(Long id) {
        return entityManager.getReference(Person.class, id);
    }
}
