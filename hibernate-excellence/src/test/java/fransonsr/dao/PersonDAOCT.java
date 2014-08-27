package fransonsr.dao;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
    PersonDAO test;

    private Runnable afterTxAction;

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
        person.setId(1L);

        test.create(person);
    }

}
