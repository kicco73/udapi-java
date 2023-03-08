package cnr.ilc.conllu.core.impl;

import java.util.ArrayList;
import java.util.List;

import cnr.ilc.conllu.core.Bundle;
import cnr.ilc.conllu.core.Document;
import cnr.ilc.conllu.core.Sentence;

/**
 * Implementation of document.
 *
 * Generates unique IDs for nodes.
 *
 * @author Martin Vojtek
 */
public class DefaultDocument implements Document {
    private int nodeUniqueId;

    private final List<Bundle> bundles = new ArrayList<>();

    /**
     *
     * @return unique ID for node
     */
    @Override
    public int getUniqueNodeId() {
        return ++nodeUniqueId;
    }

    /**
     * Default constructor.
     */
    public DefaultDocument() {
    }

    /**
     * Adds bundle to document.
     *
     * @param bundle bundle to add
     */
    @Override
    public void addBundle(Bundle bundle) {
        bundles.add(bundle);
    }

    /**
     * Creates new bundle and adds it to document.
     *
     * @return bundle added to document.
     */
    @Override
    public Bundle createBundle() {
        DefaultBundle bundle = new DefaultBundle(this);

        bundle.index = bundles.size();
        bundle.setId(String.valueOf(bundle.index));

        bundles.add(bundle);
        return bundle;
    }

    /**
     * Returns bundles in document.
     *
     * @return bundles in document.
     */
    @Override
    public List<Bundle> getBundles() {
        return bundles;
    }

    /**
     * Helper method. Returns first bundle.
     *
     * @return first bundle
     */
    @Override
    public Bundle getDefaultBundle() {
        return bundles.get(0);
    }

    /**
     * Helper method. Returns all sentences.
     *
     * @return all sentences in all bundles
     */
    @Override
    public List<Sentence> getSentences() {
        List<Sentence> sentences = new ArrayList<Sentence>();
        for (Bundle bundle: bundles) {
            sentences.addAll(bundle.getSentences());
        }
        return sentences;
    }

}
