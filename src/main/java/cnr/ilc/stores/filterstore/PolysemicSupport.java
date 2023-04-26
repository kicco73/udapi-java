package cnr.ilc.stores.filterstore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import cnr.ilc.lemon.PojoWord;
import cnr.ilc.lemon.PolysemicResolver;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.rut.utils.Metadata;

public class PolysemicSupport {
    private SqliteConnector db;
    PolysemicResolver resolver = new PolysemicResolver();

    public PolysemicSupport(SqliteConnector db) {
        this.db = db;
    }

    private WordInterface hydrateWord(ResultSet rs) throws SQLException {
        String lemma = rs.getString("lemma");
        String language = rs.getString("language");
        String fqName = rs.getString("FQName");
        String serialised = rs.getString("serialised");
        PojoWord word = new PojoWord(lemma, language, fqName, serialised);
        word.rowid = rs.getInt("rowid");
        System.err.println(String.format("POLYSEMIC HYD: %d", word.rowid));

        return word;
    }

    public void markPolysemicGroups() throws SQLException {
        String query = """
                select rowid, lemma, language from word 
                group by lemma, language having count(*) > 1
        """;

        String update = "update word set polysemicGroup = %d where lemma = '%s' and language ='%s' ";

        ResultSet rs = db.executeQuery(query);
        while (rs.next()) {
            int groupId = rs.getInt("rowid");
            String lemma = rs.getString("lemma");
            String language = rs.getString("language");
            db.executeUpdate(update, groupId, lemma, language);
        }
    }

    private Collection<Integer> listPolysemicGroups(Filter filter) throws SQLException {
        Collection<Integer> results = new ArrayList<>();

        String query = """
                select distinct polysemicGroup from word where polysemicGroup is not null and %s 
        """;
        String where = filter.buildWhere("word");
        ResultSet rs = db.executeQuery(query, where);
        while (rs.next()) {
            results.add(rs.getInt("polysemicGroup"));
        }
        return results;
    }

    private Collection<WordInterface> getPolysemicGroup(int groupId, Filter filter) throws SQLException {
        Collection<WordInterface> results = new ArrayList<>();

        String query = "select * from word where %s and polysemicGroup = %d";
        String where = filter.buildWhere("word");
        ResultSet rs = db.executeQuery(query, where, groupId);
        while (rs.next()) {
            WordInterface word = hydrateWord(rs);
            results.add(word);
        }
        return results;
    }

    public Object selectPolysemicEntries(Filter filter) throws SQLException {
        Metadata result = new Metadata();

        String query = """
            select polysemicGroup, lemma, conceptId, language from word 
                where polysemicGroup is not null and %s 
                order by lemma, language
        """;
        String where = filter.buildWhere("word");
        ResultSet rs = db.executeQuery(query, where);
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

    public Collection<WordInterface> findAndResolvePolysemicEntries(Filter filter) throws SQLException {
        Collection<WordInterface> replacementWords = new ArrayList<>();

		for (int groupId: listPolysemicGroups(filter)) {
			Collection<WordInterface> wordSet = getPolysemicGroup(groupId, filter);
            Collection<WordInterface> replacementSet = resolver.resolve(wordSet);
            replacementWords.addAll(replacementSet);
		}
        return replacementWords;
    }
}   
