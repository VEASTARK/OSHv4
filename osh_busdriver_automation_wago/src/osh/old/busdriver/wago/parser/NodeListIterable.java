package osh.old.busdriver.wago.parser;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

/**
 * Iterator for a NodeList (XML DOM)
 */
public class NodeListIterable implements Iterable<Node> {
    private final NodeList list;

    public NodeListIterable(NodeList list) {
        this.list = list;
    }

    @Override
    public Iterator<Node> iterator() {
        return new NodeListIterator(this.list);
    }

    public static class NodeListIterator implements Iterator<Node> {
        private final NodeList list;
        private int pos;

        public NodeListIterator(NodeList list) {
            this.list = list;
        }

        @Override
        public boolean hasNext() {
            return this.pos < this.list.getLength();
        }

        @Override
        public Node next() {
            return this.list.item(this.pos++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }
}
