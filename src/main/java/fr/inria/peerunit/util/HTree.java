/*
    This file is part of PeerUnit.

    PeerUnit is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    PeerUnit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.util;

import java.lang.reflect.Array;

/**
 * The HTree is an hierarchical tree.
 *
 * @author sunye
 */
public class HTree<K,V> {

    private final int order;

    private HNodeImpl head;

    private int size = 0;

    public HTree(int b) {
        order = b;
    }

    /**
     *
     * @return the head of this HTree. Returns null if empty.
     */
    public HNode<K,V> head() {
        return head;
    }

    /**
     * Inserts an element into the tree.
     * The first inserted element becomes the header.
     * @param e
     */
    public void put(K key, V value) {
        if (head == null) {
            head = new HNodeImpl(key, value);
        } else {
            head.put(key, value);
        }
        size++;
    }

    /**
     * Checks if an element belongs to this Tree
     * @param e the element to check
     * @return true, if the tree contains the element.
     */
    public boolean containsKey(K key) {
        return (head == null) ? false : head.containsKey(key);
    }

    public int size() {
        return size;
    }

    public void clear() {
        head = null;
        size = 0;
    }

    class HNodeImpl implements HNode<K,V> {
        private int position = 0;

        private K key;
        private V value;

        private HNodeImpl[] children = (HNodeImpl[]) Array.newInstance(HNodeImpl.class, order);
        
        public HNodeImpl(K k, V v) {
            key = k;
            value = v;
        }

        public K key() {
            return key;
        }
        
        public V value() {
            return value;
        }

        /**
         * Returns an array containing the children of this node.
         * If this node is a leaf, an empty array will be returned.
         * @return
         */
        public HNode<K,V>[] children() {
            int i = 0;
            HNode<K,V>[] result;
            while(i < children.length && children[i] != null ) {
                i++;
            }
            result = (HNodeImpl[]) Array.newInstance(HNodeImpl.class, i);
            System.arraycopy(children, 0, result, 0, i);
            return result;
        }

        /**
         * Checks if this node is a leaf.
         * @return true, if this node has no child.
         */
        public boolean isLeaf() {
            return  children[0] == null;
        }

        /**
         * Inserts an element into this node.
         * When this node is full (no more null child), elements
         * are inserted in the children in a equilibrated way.
         * @param e
         */
        public void put(K k, V v) {
            if(children[position] == null) {
                children[position] = new HNodeImpl(k,v);
            } else {
                children[position].put(k,v);
            }
            position = (position + 1) % order;
        }

        /**
         * Looks up for an element, returns true if found.
         * Uses a breadth-first search (BFS) algorithm.
         * @param e the searched element
         * @return true if found, false otherwise
         */
        public boolean containsKey(K k) {
            boolean result = false;
            if(k == key) {
                result = true;
            } else {
                for (int i = 0; i < children.length && ! result && children[i] != null; i++) {
                    result = children[i].containsKey(k);
                }
            }
            return result;
        }

        @Override
        public String toString() {
            return String.format("HNode<%s,%s>", key, value);
        }
    }

}
