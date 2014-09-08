package fransonsr.dao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import javax.validation.ConstraintViolationException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import fransonsr.DBUnitUtils;
import fransonsr.PersistenceCTConfiguration;
import fransonsr.model.Address;
import fransonsr.model.Person;

@ContextConfiguration(classes = {PersistenceCTConfiguration.class})
@TransactionConfiguration(defaultRollback = false)
@EnableTransactionManagement
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class PersonDAOCT {

    private static final String datasetFile = "person.dbunit.xml";

    private static final String STRING_10 = "01234456789";
    private static final String STRING_20 = STRING_10 + STRING_10;
    private static final String STRING_100 = STRING_20 + STRING_20 + STRING_20 + STRING_20 + STRING_20;
    private static final String STRING_110 = STRING_100 + STRING_10;

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

        Address address = new Address();
        address.setStreet("1235 N State");
        address.setCity("Provo");
        address.setState("UT");
        address.setZip("84606");

        person.setAddress(address); // NOTE: by direction mapping
        address.setPerson(person);

        dao.create(person);
    }

    @Test
    @Rollback(true)
    public void testCreate_fail_nullFirstName() throws Exception {
        Person person = new Person();
        person.setFirstName(null);
        person.setLastName("Last");
        person.setEmail("person@somewhere.com");

        thrown.expect(ConstraintViolationException.class);

        dao.create(person);

        dao.flush();    // force the constraint violation
    }

    @Test
    @Rollback(true)
    public void testCreate_fail_firstNameTooLong() throws Exception {
        Person person = new Person();
        person.setFirstName(STRING_110);
        person.setLastName("Last");
        person.setEmail("person@somewhere.com");

        thrown.expect(ConstraintViolationException.class);

        dao.create(person);

        dao.flush();    // force the constraint violation
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
