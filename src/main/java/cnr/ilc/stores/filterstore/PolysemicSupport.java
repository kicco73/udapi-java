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

@SuppressWarnings("unchecked")
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

    private Collection<WordInterface> getPolysemicWords(Filter filter) throws SQLException {
        Collection<WordInterface> results = new ArrayList<>();

        String query = """
                select * from word where %s 
                group by lemma, language having count(*) > 1
        """;
        String where = db.buildWhere("word", filter);
        ResultSet rs = db.executeQuery(query, where);
        while (rs.next()) {
            WordInterface word = hydrateWord(rs);

            System.err.println("POLYSEMIC WORD: " +  word.getLemma());
            results.add(word);
        }
        return results;
    }

    private Collection<WordInterface> getPolysemicWordSet(WordInterface polysemicWord, Filter filter) throws SQLException {
        Collection<WordInterface> results = new ArrayList<>();

        String query = "select * from word where %s and lemma='%s' and language='%s'";
        String where = db.buildWhere("word", filter);
        ResultSet rs = db.executeQuery(query, where, polysemicWord.getLemma(), polysemicWord.getLanguage());
        while (rs.next()) {
            WordInterface word = hydrateWord(rs);
            results.add(word);
        }
        return results;
    }

    public Object selectPolysemicEntries(Filter filter) throws SQLException {
        Metadata result = new Metadata();

        String query = """
            select lemma, conceptId, language from word where lemma || "@" || language in (
                select lemma || "@" || language from word where %s 
                group by lemma, language having count(*) > 1
            ) order by lemma, language
        """;
        String where = db.buildWhere("word", filter);
        ResultSet rs = db.executeQuery(query, where);
        while (rs.next()) {
            Map<String,String> term = new HashMap<>();
            term.put("t", rs.getString("lemma"));
            term.put("c", rs.getString("conceptId"));
            term.put("l", rs.getString("language"));
            result.addToList("*", term);
        }
        return result.getObject("*");
    }

    public Filter filterOut(Collection<WordInterface> excludeWords, Filter filter) {
        Filter newFilter = new Filter(filter);
        Collection<String> excludedIds = new ArrayList<>();
        for (WordInterface excludeWord: excludeWords) {
            PojoWord pojoWord = (PojoWord) excludeWord;
            excludedIds.add(String.format("%d", pojoWord.rowid));
        }
        System.err.println("NEW FILTER1 " + excludeWords);
        newFilter.setExcludeIds(excludedIds);
        System.err.println("NEW FILTER " + newFilter.getExcludeIds());
        return newFilter;
    }

    public Collection<Collection<WordInterface>> findAndResolvePolysemicEntries(Filter filter) throws SQLException {
        Collection<WordInterface> replacedWords = new ArrayList<>();
        Collection<WordInterface> replacementWords = new ArrayList<>();
        Collection<Collection<WordInterface>> result = new ArrayList<>();
        result.add(replacedWords);
        result.add(replacementWords);

		for (WordInterface word: getPolysemicWords(filter)) {
			Collection<WordInterface> wordSet = getPolysemicWordSet(word, filter);
            replacedWords.addAll(wordSet);
            Collection<WordInterface> replacementSet = resolver.resolve(wordSet);
            replacementWords.addAll(replacementSet);
		}
        return result;
    }
}   
