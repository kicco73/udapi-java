package cnr.ilc.stores.filterstore;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cnr.ilc.lemon.resource.ConceptInterface;
import cnr.ilc.lemon.resource.GlobalInterface;
import cnr.ilc.lemon.resource.PojoConcept;
import cnr.ilc.lemon.resource.PojoGlobal;
import cnr.ilc.lemon.resource.PojoSense;
import cnr.ilc.lemon.resource.PojoWord;
import cnr.ilc.lemon.resource.SenseInterface;
import cnr.ilc.lemon.resource.TermInterface;
import cnr.ilc.rut.Filter;
import cnr.ilc.rut.utils.Metadata;

public class SqliteConnector {
    private Connection connection;
    private Statement statement;

    private void createIndices(String tableName, String... columnNames) throws SQLException {
        for (String columnName: columnNames) {
            executeUpdate("create index %s_%s_idx on %s (%s)", 
                tableName, columnName, tableName, columnName
            );
        }
    }

    private void createTable(String tableName, String ...fields) throws SQLException {
        List<String> defaultFields = new ArrayList<String>(
            Arrays.asList("rowid integer primary key autoincrement", "language string", "date string", "subjectField string", "metadata json", "serialised string default ''"))
        ;
        defaultFields.addAll(Arrays.asList(fields));
        String fieldsString = String.join(", ", defaultFields);
        
        executeUpdate("drop table if exists %s", tableName);
        executeUpdate("create table %s (%s)", tableName, fieldsString);
        createIndices(tableName, "language", "date", "subjectField");
    }

    private void createSchema() throws SQLException {
        createTable("global");
        createTable("concept", "conceptId string");
        createIndices("concept", "conceptId");
        createTable("word", "conceptId string", "conceptFQN string", "lemma string", "FQName string", "polysemicGroup integer", "senseFQN", "serialisedSenses string");
        createIndices("word", "conceptId", "lemma", "polysemicGroup");
    }

    public void connect(String fileName) throws SQLException {
        boolean exists = new File(fileName).exists();
        String url = String.format("jdbc:sqlite:%s", fileName);
        connection = DriverManager.getConnection(url);
        statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec
        if(!exists) createSchema();
    }

    public void executeUpdate(String query, Object ...args) throws SQLException {
        query = String.format(query, args);
        statement.executeUpdate(query);
    }

    public ResultSet executeQuery(String query, Object ...args) throws SQLException {
        query = String.format(query, args);
        //System.err.println(query);
        ResultSet rs = statement.executeQuery(query);
        return rs;
    }

    public String quote(String s) {
        if (s == null) return null;
        s = "'" + s.replaceAll("'", "''") + "'";
        return s;
    }
    
    public GlobalInterface hydrateGlobal(ResultSet rs) throws SQLException {
        String language = rs.getString("language");
        String subjectField = rs.getString("subjectField");
        String serialised = rs.getString("serialised");
        String json = rs.getString("metadata");
        return new PojoGlobal(language, subjectField, serialised, json);
    }

    private ConceptInterface hydrateConcept(ResultSet rs) throws SQLException {
        String id = rs.getString("conceptId");
        String fqn = "Zio pino"; // TODO: rs.getString("FQName");
        String date = rs.getString("date");
        String subjectField = rs.getString("subjectField");
        String subjectFieldFQN = "Zio pino"; //rs.getString("subjectFieldFQN");
        String serialised = rs.getString("serialised");
        String json = rs.getString("metadata");
        return new PojoConcept(id, fqn, subjectField, subjectFieldFQN, date, serialised, json);
    }

    public TermInterface hydrateWord(ResultSet rs) throws SQLException {
        String lemma = rs.getString("lemma");
        String language = rs.getString("language");
        String fqName = rs.getString("FQName");
        String serialised = rs.getString("serialised");
        String serialisedSenses = rs.getString("serialisedSenses");
        String conceptFQN = rs.getString("conceptFQN");
        String senseFQN = rs.getString("senseFQN");
        PojoWord word = new PojoWord(lemma, language, fqName, serialised, conceptFQN);

        // TODO: enforce this field supports only one sense
        SenseInterface sense = new PojoSense(senseFQN, conceptFQN, serialisedSenses);
        word.getSenses().add(sense);
    
        word.rowid = rs.getInt("rowid");
        return word;
    }
    
    public Collection<ConceptInterface> selectConcepts(Filter filter) throws SQLException {
        Collection<ConceptInterface> result = new ArrayList<ConceptInterface>();
        filter = new Filter(filter);
        Collection<String> langs = filter.getLanguages();
        if (langs.size() > 0) langs.add("*");

        String query = """
            select distinct * from concept 
            where %s and conceptId in (
                select distinct concept.conceptId as conceptId
                from concept
                inner join word 
                on concept.conceptId = word.conceptId
                where concept.language = '*' and %s
            ) order by conceptId, language  
        """;

        String whereConcept = filter.buildWhere("concept");
        String whereWord = filter.buildWhere("word");
        ResultSet rs = executeQuery(query, whereConcept, whereWord);

        while (rs.next()) {
			ConceptInterface concept = hydrateConcept(rs);
			result.add(concept);
		}
        return result;
    }

    public Collection<String> selectConcept(String columnName, Filter filter) throws SQLException {
        Collection<String> result = new ArrayList<String>();
        filter = new Filter(filter);
        Collection<String> langs = filter.getLanguages();
        if (langs.size() > 0) langs.add("*");

        String query = """
            select distinct %s from concept 
            where %s and conceptId in (
                select distinct concept.conceptId as conceptId
                from concept
                inner join word 
                on concept.conceptId = word.conceptId
                where concept.language = '*' and %s
            ) order by conceptId, language  
        """;

        String whereConcept = filter.buildWhere("concept");
        String whereWord = filter.buildWhere("word");
        ResultSet rs = executeQuery(query, columnName, whereConcept, whereWord);
        while (rs.next()) {
            result.add(rs.getString(columnName));
        }
        return result;
    }

    public ResultSet selectEntity(String entityName, Filter filter) throws SQLException {
        filter = new Filter(filter);
        Collection<String> languages = filter.getLanguages();
        if (languages.size() > 0) languages.add("*");
        String where = filter.buildWhere(entityName);
        ResultSet rs = executeQuery("select count(*) as n from %s where %s order by language", entityName, where);
        rs.next();
        int rows = rs.getInt("n");
        rs = executeQuery("select * from %s where %s order by language", entityName, where);
        rs.setFetchSize(rows);
        return rs;
    }

    public void markPolysemicGroups() throws SQLException {
        String query = """
                select rowid, lemma, language from word 
                group by lemma, language having count(*) > 1
        """;

        String update = "update word set polysemicGroup = %d where lemma = '%s' and language ='%s' ";

        ResultSet rs = executeQuery(query);
        while (rs.next()) {
            int groupId = rs.getInt("rowid");
            String lemma = rs.getString("lemma");
            String language = rs.getString("language");
            executeUpdate(update, groupId, lemma, language);
        }
    }
    public Object selectPolysemicEntries(Filter filter) throws SQLException {
        Metadata result = new Metadata();

        String query = """
            select polysemicGroup, lemma, conceptId, language from word 
                where polysemicGroup is not null and %s 
                order by lemma, language
        """;
        String where = filter.buildWhere("word");
        ResultSet rs = executeQuery(query, where);
        while (rs.next()) {
            Map<String,String> term = new HashMap<>();
            term.put("t", rs.getString("lemma"));
            term.put("c", rs.getString("conceptId"));
            term.put("l", rs.getString("language"));
            term.put("g", rs.getString("polysemicGroup"));
            result.addToList("*", term);
        }
        return result.getObject("*");
    }

    public Map<String, Long> selectTermStats(Filter filter) throws SQLException {
        Map<String, Long> results = new LinkedHashMap<>();

        String query = "select language, count(*) as n from word where %s group by language";
        String where = filter.buildWhere("word");
        ResultSet rs = executeQuery(query, where);

        while (rs.next()) {
            String language = rs.getString("language");
            long count = rs.getLong("n");
            results.put(language, count);
        }

        return results;
    }

    public Collection<String> selectConceptDates(Filter filter) throws SQLException {
        Collection<String> results = new ArrayList<>();

        String query = "select distinct date from concept where date is not null and %s";
        String where = filter.buildWhere("concept");
        ResultSet rs = executeQuery(query, where);

        while (rs.next()) {
            String date = rs.getString("date");
            results.add(date);
        }

        return results;
    }

    public void clear() throws SQLException {
        createSchema();
    }
}   
