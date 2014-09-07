package fransonsr.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import fransonsr.DBUnitUtils;
import fransonsr.PersistenceCTConfiguration;
import fransonsr.model.Person;

@ContextConfiguration(classes = {PersistenceCTConfiguration.class})
@TransactionConfiguration(defaultRollback = false)
@EnableTransactionManagement
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class PersonDAOCT {

    private static final String datasetFile = "person.dbunit.xml";

    @Autowired
    DBUnitUtils dbUnitUtils;

    @Autowired
    PersonDAO dao;

    private Runnable afterTxAction;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @BeforeTransaction
    public void executeBeforeTx() throws Exception {
        dbUnitUtils.setUpDataset(datasetFile);
    }

    @AfterTransaction
    public void executeAfterTxAction() throws Exception {
        if(afterTxAction != null) {
            afterTxAction.run();
        }

        afterTxAction = null;
        dbUnitUtils.cleanUpDataset(datasetFile);
    }

    @Before
    public void setup() throws Exception {
        afterTxAction = null;
    }

    @Test
    public void testCreate() throws Exception {
        afterTxAction = new Runnable() {
            public void run() {
                dbUnitUtils.validateTable("person.create.dbunit.xml", "PERSON");
            }
        };

        Person person = new Person();
        person.setFirstName("First");
        person.setLastName("Last");
        person.setEmail("person@somewhere.com");

        person.setId(3L);   // NOTE: since no ID generator is defined, it is assigned.

        dao.create(person);
    }

    @Test
    public void testRead() throws Exception {
        Person person = dao.read(1L);

        Person expected = new Person();
        expected.setFirstName("Mickey");
        expected.setLastName("Mouse");

        assertThat(person, is(equalTo(expected)));
    }

    @Test
    public void testUpdate() throws Exception {
        afterTxAction = new Runnable() {
            public void run() {
                dbUnitUtils.validateTable("person.update.dbunit.xml", "PERSON");
            }
        };

        Person person = dao.read(1L);

        person.setEmail("mickey.mouse@disney.com");
    }

    @Test
    public void testDelete() throws Exception {
        afterTxAction = new Runnable() {
            public void run() {
                dbUnitUtils.validateTable("person.delete.dbunit.xml", "PERSON");
            }
        };

        Person person = dao.read(1L);
        dao.delete(person);
    }

    @Test
    public void testDelete_long() throws Exception {
        afterTxAction = new Runnable() {
            public void run() {
                dbUnitUtils.validateTable("person.delete.dbunit.xml", "PERSON");
            }
        };

        dao.delete(1L);
    }

    @Test
    public void testReference() throws Exception {
        Person person = dao.reference(1L);

        assertThat(person.getClass().getCanonicalName(), is(not(Person.class.getCanonicalName())));
        assertThat(person, is(instanceOf(Person.class)));
    }
}
