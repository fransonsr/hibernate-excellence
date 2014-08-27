/**
 * Copyright © 2010 Intellectual Reserve, Inc. All Rights reserved.
 */

package fransonsr;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.ITableIterator;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.filter.DefaultColumnFilter;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.dataset.xml.FlatXmlWriter;
import org.dbunit.ext.h2.H2Connection;
import org.dbunit.ext.mysql.MySqlConnection;
import org.dbunit.operation.DatabaseOperation;
import org.springframework.beans.factory.InitializingBean;

/**
 * DBUnit utilities to assist DB integration tests.
 *
 * @author fransonsr
 *
 */
public class DBUnitUtils implements InitializingBean {
    private static final String pathPrefix = "dbunit/";

    private static final String ddlPrefix = "ddl/";

    private static final String goldenPathPrefix = pathPrefix + "golden/";

    private static final Logger log = Logger.getLogger(DBUnitUtils.class);

    DataSource dataSource;
    IDatabaseConnection dbUnitConnection;
    String ddlResource = ddlPrefix + "additional.sql";
    String schema;
    boolean isH2 = true;

    public void afterPropertiesSet() throws Exception {
        getDBUnitConnection();
        if (getDdlResource() != null) {
            URL ddlURL = ClassLoader.getSystemResource(getDdlResource());
            String filePath = null;
            if (ddlURL != null) {
                filePath = ddlURL.toExternalForm();
            }
            // There is an / coming up after file in window but not on Unix:
            if(filePath != null && filePath.contains("file:/")) {
                filePath = filePath.replaceFirst("/", "");
            }
            // TODO : need to find a way work in both windows and linux
          //  intializeDatabase(filePath);
        }
    }

    /**
     * Return the schema prefix.
     *
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Set the schema prefix
     *
     * @param schema the schema to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * True if H2 database is in use.
     * @return
     */
    public boolean isH2() {
        return isH2;
    }

    /**
     * Set true if H2 database is in use.
     * @param isH2
     */
    public void setH2(boolean isH2) {
        this.isH2 = isH2;
    }

    /**
     * Remove all data in the specified dataset from the DB (in reverse order).
     *
     * @param datasetFile
     * @throws Exception
     */
    public void cleanUpDataset(String datasetFile) throws Exception {

        if(log.isDebugEnabled()) {
            IDataSet dataSet = getDBUnitConnection().createDataSet();
            FlatXmlWriter writer = new FlatXmlWriter(System.out);
            writer.setPrettyPrint(true);

            writer.write(dataSet);
        }

        String file = pathPrefix + datasetFile;
        log.debug("Cleaning up dataset: " + file);

        IDataSet dataset = getDataSetFromFile(file);
        DatabaseOperation.DELETE_ALL.execute(getDBUnitConnection(), dataset);
    }

    /**
     * Read the dataset from the specified XML file.
     *
     * @param filename
     * @return
     */
    IDataSet getDataSetFromFile(String filename) {
        return getDataSetFromFile(filename, null);
    }

    /**
     * Read the dataset from the specified XML file.
     *
     * @param filename
     * @param replacementMap
     * @return
     */
    IDataSet getDataSetFromFile(String filename, Map<String, Object> replacementMap) {
        try {
            URL datasetURL = getClass().getClassLoader().getResource(filename);
            IDataSet dataset = new FlatXmlDataSetBuilder().build(datasetURL);
            ReplacementDataSet rDataset = new ReplacementDataSet(dataset);
            // Generate replacement values for "[NULL]" and "[UUID0]" thru "[UUID23]"
            rDataset.addReplacementObject("[NULL]", null);
            for (int i = 0; i < 24; i++) {
                UUID uuid = TestUUID.toUUID(i);
                rDataset.addReplacementObject("[UUID" + i + "]", TestUUID.toBytes(uuid));
//                rDataset.addReplacementObject("[UUID" + i + "]", uuid.toString().getBytes());
            }
            if (replacementMap != null) {
                for (Entry<String, Object> entry : replacementMap.entrySet()) {
                    rDataset.addReplacementObject(entry.getKey(), entry.getValue());
                }
            }
            rDataset.setStrictReplacement(true);
            return rDataset;
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to load dataset from file: " + filename, e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * Return the JDBC connection for DBUnit.
     *
     * @return
     * @throws Exception
     */
    protected IDatabaseConnection getDBUnitConnection() throws Exception {
        if (dbUnitConnection == null) {
            if(isH2()) {
                dbUnitConnection = new H2Connection(dataSource.getConnection(), schema);
            }
            else {
                dbUnitConnection = new MySqlConnection(dataSource.getConnection(), schema);
            }
            dbUnitConnection.getConfig().setProperty("http://www.dbunit.org/features/caseSensitiveTableNames", false);
            dbUnitConnection.getConfig().setProperty("http://www.dbunit.org/features/qualifiedTableNames", false);
        }

        return dbUnitConnection;
    }

    public String getDdlResource() {
        return ddlResource;
    }

    /**
     * Creates the schema in the DB using the specified DDL.
     *
     * @param ddl
     * @throws Exception
     */
    protected void intializeDatabase(String ddl) throws Exception {
        Connection conn = getDataSource().getConnection();

        conn.setAutoCommit(true);

        String statementString = "RUNSCRIPT FROM '" + ddl + "'";

        log.debug("Executing DDL: \n" + statementString);

        Statement stmt = conn.createStatement();
        stmt.execute(statementString);
        stmt.close();
        conn.close();
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     *
     * Set the resource name for the DDL to initialize the database.
     * If this property is null, it is assumed that the database
     * schema has been created elsewhere (such as with Hibernate).
     *
     * @param ddlResource
     */
    public void setDdlResource(String ddlResource) {
        this.ddlResource = ddlResource;
    }

    /**
     * Add data in the dataset to the DB.
     *
     * @param datasetFile
     * @throws Exception
     */
    public IDataSet setUpDataset(String datasetFile) throws Exception {
        String file = pathPrefix + datasetFile;
        log.debug("Setting up dataset: " + file);

        IDataSet dataset = getDataSetFromFile(file);
        DatabaseOperation.CLEAN_INSERT.execute(getDBUnitConnection(), dataset);

        return dataset;
    }

    /**
     * Compare the actual row count for the specified table with the expected
     * value.
     *
     * @param table
     * @param expectedRowCount
     */
    public void validateRowCount(String table, int expectedRowCount) {
        try {
            IDataSet actualDataSet = getDBUnitConnection().createDataSet();
            ITable actualTable = actualDataSet.getTable(table);
            assertThat("row count for " + table, actualTable.getRowCount(), is(equalTo(expectedRowCount)));
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to obtain row count", e);
        }
    }

    /**
     * Validate the state of a specific table in the database using a "golden"
     * DataSet specified by the filename. The comparison will only use the
     * columns defined in the golden dataset.
     *
     * @param goldenDataset
     * @param table
     */
    public void validateTable(String goldenDataset, String table) {
        validateTable(goldenDataset, table, true, false, null);
    }

    /**
     * Validate the state of a specific table in the database using a "golden"
     * DataSet specified by the filename. The comparison will only use the
     * columns defined in the golden dataset.
     *
     * @param goldenDataset
     * @param table
     * @param replacementMap
     */
    public void validateTable(String goldenDataset, String table, Map<String, Object> replacementMap) {
        validateTable(goldenDataset, table, true, false, null, replacementMap);
    }

    /**
     * Validate the state of a specific table in the database using a "golden"
     * DataSet specified by the filename. If filterColumns is set, only the
     * columns present in the golden DataSet are compared with the actual
     * database table. If sorted is set true, sort the rows by the column
     * specified in sortColumn
     *
     * @param goldenDataset
     * @param table
     * @param filterColumns
     * @param sorted
     * @param sortColumn
     */
    public void validateTable(String goldenDataset, String table,
                                 boolean filterColumns, boolean sorted, String sortColumn) {
        validateTable(goldenDataset, table, filterColumns, sorted, sortColumn, null);
    }

    /**
     * Validate the state of a specific table in the database using a "golden"
     * DataSet specified by the filename. If filterColumns is set, only the
     * columns present in the golden DataSet are compared with the actual
     * database table. If sorted is set true, sort the rows by the column
     * specified in sortColumn
     *
     * @param goldenDataset
     * @param table
     * @param filterColumns
     * @param sorted
     * @param sortColumn
     * @param replacementMap
     */
    public void validateTable(String goldenDataset, String table,
                                 boolean filterColumns, boolean sorted, String sortColumn,
                                 Map<String, Object> replacementMap) {
        try {
            String goldenFile = goldenPathPrefix + goldenDataset;
            IDataSet expectedDataSet = getDataSetFromFile(goldenFile, replacementMap);
            ITable expectedTable = expectedDataSet.getTable(table);

            IDataSet actualDataSet = getDBUnitConnection().createDataSet();
            ITable actualTable = actualDataSet.getTable(table);

            if (filterColumns) {
                actualTable = DefaultColumnFilter.includedColumnsTable(
                                                                       actualTable, expectedTable.getTableMetaData()
                                                                           .getColumns());
            }

            if (sorted) {
                SortedTable expectedSortedTable = new SortedTable(expectedTable, new String[] { sortColumn });
                expectedSortedTable.setUseComparable(true);
                SortedTable actualSortedTable = new SortedTable(actualTable, new String[] { sortColumn });
                actualSortedTable.setUseComparable(true);

                expectedTable = expectedSortedTable;
                actualTable = actualSortedTable;
            }

            org.dbunit.Assertion.assertEquals(expectedTable, actualTable);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to obtain dataset: " + e, e);
        }
    }

    /**
     * Validate all of the tables in the specified dataSet.
     *
     * @param dataSet
     */
    public void validateDataset(IDataSet dataSet) {
        try {
            IDataSet actualDataSet = getDBUnitConnection().createDataSet();
            ITableIterator iterator = dataSet.iterator();
            while (iterator.next()) {
                ITable expectedTable = iterator.getTable();
                ITable actualTable = actualDataSet.getTable(expectedTable.getTableMetaData().getTableName());
                actualTable = DefaultColumnFilter.includedColumnsTable(
                                                                       actualTable, expectedTable.getTableMetaData()
                                                                           .getColumns());
                org.dbunit.Assertion.assertEquals(expectedTable, actualTable);
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Exception validating dataset", e);
        }
    }
}
