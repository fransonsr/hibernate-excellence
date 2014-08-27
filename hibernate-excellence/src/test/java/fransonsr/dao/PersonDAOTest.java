package fransonsr.dao;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import javax.persistence.EntityManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import fransonsr.model.Person;

public class PersonDAOTest {

    @Mock
    EntityManager entityManager;

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

        Person person = test.read(id);

        assertThat(person, is(notNullValue()));
    }
}
