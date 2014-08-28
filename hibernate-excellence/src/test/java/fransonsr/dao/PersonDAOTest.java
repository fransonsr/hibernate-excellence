package fransonsr.dao;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fransonsr.model.Person;

public class PersonDAOTest {

    @Mock
    EntityManager entityManager;

    @Mock
    Query query;

    @InjectMocks
    PersonDAO test;

    Person person;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        person = new Person();
        person.setFirstName("First");
        person.setLastName("Last");
        person.setEmail("user@somewhere.com");

        when(entityManager.createNamedQuery("personDeleteById")).thenReturn(query);
    }

    @Test
    public void testCreate() throws Exception {

        test.create(person);

        verify(entityManager).persist(person);
    }

    @Test
    public void testRead() throws Exception {
        Long id = 1L;

        when(entityManager.find(Person.class, id)).thenReturn(person);

        Person actual = test.read(id);

        assertThat(actual, is(notNullValue()));
    }

    // Nothing to test for update.

    @Test
    public void testDelete() throws Exception {

        test.delete(person);

        verify(entityManager).remove(person);
    }

    @Test
    public void testDelete_long() throws Exception {

        Long id = 1L;

        when(query.setParameter("id", id)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(1);

        test.delete(id);

        verify(entityManager).createNamedQuery("personDeleteById");
    }

    @Test
    public void testReference() throws Exception {
        Long id = 1L;

        when(entityManager.getReference(Person.class, id)).thenReturn(person);

        Person actual = test.reference(id);

        assertThat(actual, is(notNullValue()));
    }
}
