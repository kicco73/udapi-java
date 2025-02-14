package cnr.ilc.conllu.core.impl;

import java.util.ArrayList;
import java.util.List;

import cnr.ilc.conllu.core.*;
import cnr.ilc.conllu.core.io.UdapiIOException;

/**
 * Implementation of Sentence.
 *
 * Serves as a container for technical node.
 * Maintains descendants in word order.
 *
 * @author Martin Vojtek
 */
public class DefaultSentence implements Sentence {

    private final Token node;
    private final Document document;
    private Bundle bundle;
    private String zone = Sentence.DEFAULT_ZONE;

    private List<String> comments = new ArrayList<>();
    private List<MultiwordToken> multiwords = new ArrayList<>();
    private List<Token> descendants = new ArrayList<>();
    private List<EmptyNode> emptyNodes = new ArrayList<>();
    private String text;
    private String id;
    private String sentId;
    private String newParId;
    private String newDocId;
    private boolean isNewDoc;
    private boolean isNewPar;

    public DefaultSentence(Document document) {
        this.document = document;
        this.node = createToken();
        this.node.setOrd(0);
    }

    public DefaultSentence(Document document, Bundle bundle) {
        this.document = document;
        this.node = createToken();
        this.node.setOrd(0);
        this.bundle = bundle;
    }

    protected Token createToken() {
        return new DefaultRootToken(this);
    }

    @Override
    public Token getToken() {
        return node;
    }

    @Override
    public Document getDocument() {
        return document;
    }

    @Override
    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public Bundle getBundle() {
        return bundle;
    }

    @Override
    public void normalizeOrder() {
        int newOrder = 1;
        for (Token descendant : node.getTokens()) {
            descendant.setOrd(newOrder++);
        }
    }

    @Override
    public List<Token> getTokens() {
        return descendants;
    }

    @Override
    public void setSentId(String sentId) {
        if (null != bundle) {
            String[] parts = sentId.split("/", 1);
            bundle.setId(parts[0]);
            if (2 == parts.length) {
                setZone(parts[1]);
            }
            this.sentId = sentId;
        } else {
            this.sentId = sentId;
        }
    }

    @Override
    public String getSentId() {
        return sentId;
    }

    @Override
    public void setZone(String zone) {
        this.zone = zone;
    }

    @Override
    public String getZone() {
        return zone;
    }

    @Override
    public Sentence copyTree() {
        DefaultSentence newRoot = new DefaultSentence(document, bundle);
        copySubtree(getToken(), newRoot.getToken());
        return newRoot;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void validateZone() {
        if (!zone.matches("^[a-z-]+(_[A-Za-z0-9-])?$")) {
            throw new UdapiIOException("'" + zone + "' is not a valid zone name (from treeId='" + id + "')");
        }
    }

    /**
     * Full (document-wide) id of the root.
     *
     * The general format of root nodes is:
     * root.bundle.id + '/' + root.zone, e.g. s123/en_udpipe.
     * If zone is empty, the slash is excluded as well, e.g. s123.
     * If bundle is missing (could occur during loading), '?' is used instead.
     * Root's address is stored in CoNLL-U files as sent_id (in a special comment).
     *
     * @return address of the root node
     */
    @Override
    public String getAddress() {
        String zone = "/" + (this.zone != null ? this.zone : "");
        if (null != bundle) {
            return getBundle().getAddress() + zone;
        } else if (null != sentId) {
            return sentId + zone;
        } else {
            return "?" + zone;
        }
    }

    @Override
    public Token createChild() {
        return node.createChild();
    }

    @Override
    public String getForm() {
        return node.getForm();
    }

    @Override
    public String getLemma() {
        return node.getLemma();
    }

    @Override
    public String getUpos() {
        return node.getUpos();
    }

    @Override
    public String getXpos() {
        return node.getXpos();
    }

    @Override
    public String getFeats() {
        return node.getFeats();
    }

    @Override
    public String getDeprel() {
        return node.getDeprel();
    }

    @Override
    public EnhancedDeps getDeps() {
        return node.getDeps();
    }

    @Override
    public String getMisc() {
        return node.getMisc();
    }

    @Override
    public void setNewParId(String newParId) {
        this.newParId = newParId;
    }

    @Override
    public String getNewParId() {
        return newParId;
    }

    @Override
    public void setNewDocId(String newDocId) {
        this.newDocId = newDocId;
    }

    @Override
    public String getNewDocId() {
        return newDocId;
    }

    @Override
    public List<EmptyNode> getEmptyNodes() {
        return emptyNodes;
    }

    @Override
    public void setEmptyNodes(List<EmptyNode> emptyNodes) {
        this.emptyNodes.clear();
        if (null != emptyNodes) {
            this.emptyNodes.addAll(emptyNodes);
        }
    }

    @Override
    public boolean isNewDoc() {
        return isNewDoc;
    }

    @Override
    public void setIsNewDoc(boolean isNewDoc) {
        this.isNewDoc = isNewDoc;
    }

    @Override
    public boolean isNewPar() {
        return isNewPar;
    }

    @Override
    public void setIsNewPar(boolean isNewPar) {
        this.isNewPar = isNewPar;
    }

    private void copySubtree(Token oldNode, Token newNode) {
        for (Token child : oldNode.getChildren()) {
            Token newChild = newNode.createChild();
            newChild.setDeps(child.getDeps());
            newChild.setDeprel(child.getDeprel());
            newChild.setFeats(child.getFeats());
            newChild.setForm(child.getForm());
            newChild.setHead(child.getHead());
            newChild.setLemma(child.getLemma());
            newChild.setMisc(child.getMisc());
            newChild.setOrd(child.getOrd());
            newChild.setUpos(child.getUpos());
            newChild.setXpos(child.getXpos());
            copySubtree(child, newChild);
        }
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    public List<String> getComments() {
        return comments;
    }

    @Override
    public void addMultiword(List<Token> words, String form, String misc) {
        MultiwordToken newMwt = new DefaultMultiwordToken();
        newMwt.setWords(words);
        newMwt.setForm(form);
        newMwt.setMisc(misc);
        newMwt.setRoot(this);

        words.forEach(m -> m.setMwt(newMwt));

        this.multiwords.add(newMwt);
    }

    public List<MultiwordToken> getMultiwords() {
        return multiwords;
    }

    @Override
    public void setText(String sentenceText) {
        this.text = sentenceText;
    }

    @Override
    public String getText() {
        return text;
    }


}
