package cnr.ilc.stores.filterstore.processors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.simple.JSONArray;

import cnr.ilc.lemon.PolysemicResolver;
import cnr.ilc.lemon.resource.WordInterface;
import cnr.ilc.rut.utils.Metadata;
import cnr.ilc.sparql.TripleSerialiser;
import cnr.ilc.stores.filterstore.Filter;
import cnr.ilc.stores.filterstore.SqliteConnector;

@SuppressWarnings("unchecked")
public class PolysemicProcessor implements ProcessorInterface {
    private SqliteConnector db;
    PolysemicResolver resolver = new PolysemicResolver();

    public PolysemicProcessor(SqliteConnector db) {
        this.db = db;
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

    @Override
    public Collection<WordInterface> filter(Collection<WordInterface> words, TripleSerialiser triples)  {
        Collection<WordInterface> result = new ArrayList<>();
        Metadata metadata = new Metadata();

		for (WordInterface word: words) {
            metadata.addToList(word.getLanguage(), word, word.getLemma());
        }

        Collection<String> languages = metadata.getRoot().keySet();

        for (String language: languages) {
            Map<String, Object> map = metadata.getMap(language);
            for (Entry<String, Object> group: map.entrySet()) {
                String term = group.getKey();
                Collection<WordInterface> wordSet = (JSONArray) group.getValue();
                Collection<WordInterface> replacementSet = resolver.resolve(wordSet);
                result.addAll(replacementSet);

                if (wordSet.size() > 1)
                    triples.addComment("[Polysemic Processor] term `%s`@%s merged", term, language);
            }
        }
        return result;
    }
}   
