package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.io.Serializable;
import java.util.*;

/**
 * Implements the {@link Map} interface with a
 * Compressed Hash-Array Mapped Prefix-trie (CHAMP).
 * <p>
 * Creating a persistent copy is performed in O(1).
 * <p>
 * References:
 * <dl>
 *     <dt>This class has been derived from "The Capsule Hash Trie Collections Library".</dt>
 *     <dd>Copyright (c) Michael Steindorfer, Centrum Wiskunde & Informatica, and Contributors.
 *         BSD 2-Clause License.
 *         <a href="https://github.com/usethesource/capsule">github.com</a>.</dd>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */

public class TrieMap<K, V> extends AbstractMap<K, V> implements Serializable {
    private final static long serialVersionUID = 0L;
    private PersistentTrieHelper.Nonce bulkEdit;
    private PersistentTrieMapHelper.Node<K, V> root;
    private int hashCode;
    private int size;

    public TrieMap() {
        this.bulkEdit = new PersistentTrieHelper.Nonce();
        this.root = PersistentTrieMapHelper.emptyNode();
    }

    TrieMap(@NonNull PersistentTrieMap<K, V> trieMap) {
        this.bulkEdit = new PersistentTrieHelper.Nonce();
        this.root = trieMap.root;
        this.hashCode = trieMap.hashCode;
        this.size = trieMap.size;
    }

    @Override
    public void clear() {
        this.root = PersistentTrieMapHelper.emptyNode();
        this.size = 0;
        this.hashCode = 0;
    }

    @Override
    public boolean containsKey(final @NonNull Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return root.findByKey(key, Objects.hashCode(key), 0).keyExists();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // Type arguments are needed for Java 8!
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public boolean add(Entry<K, V> kvEntry) {
                PersistentTrieMapHelper.ChangeEvent<V> details = new PersistentTrieMapHelper.ChangeEvent<>();
                root.updated(bulkEdit, kvEntry.getKey(), kvEntry.getValue(), Objects.hashCode(kvEntry.getKey()), 0, details);
                return details.isModified();
            }

            @Override
            public void clear() {
                TrieMap.this.clear();
            }

            @Override
            public boolean contains(Object o) {
                if (o instanceof Entry) {
                    @SuppressWarnings("unchecked")
                    Entry<K, V> entry = (Entry<K, V>) o;
                    K key = entry.getKey();
                    PersistentTrieMapHelper.SearchResult<V> result = root.findByKey(key, Objects.hashCode(key), 0);
                    return result.keyExists() && Objects.equals(result.get(), entry.getValue());
                }
                return false;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new PersistentTrieMapHelper.MapEntryIterator<>(root);
            }

            @Override
            public boolean remove(Object o) {
                if (o instanceof Entry) {
                    @SuppressWarnings("unchecked")
                    Entry<K, V> entry = (Entry<K, V>) o;
                    K key = entry.getKey();
                    PersistentTrieMapHelper.SearchResult<V> result = root.findByKey(key, Objects.hashCode(key), 0);
                    if (result.keyExists() && Objects.equals(result.get(), entry.getValue())) {
                        removeAndGiveDetails(key);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    @Override
    public V get(final @NonNull Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return root.findByKey(key, Objects.hashCode(key), 0).orElse(null);
    }

    @Override
    public V put(K key, V value) {
        return putAndGiveDetails(key, value).getReplacedValue();
    }

    @NonNull PersistentTrieMapHelper.ChangeEvent<V> putAndGiveDetails(final K key, final V val) {
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieMapHelper.ChangeEvent<V> details = new PersistentTrieMapHelper.ChangeEvent<>();

        final PersistentTrieMapHelper.Node<K, V> newRootNode = root.updated(bulkEdit, key, val, keyHash, 0, details);

        if (details.isModified()) {
            if (details.hasReplacedValue()) {
                final V old = details.getReplacedValue();
                final int valHashOld = Objects.hashCode(old);
                final int valHashNew = Objects.hashCode(val);
                root = newRootNode;
                hashCode = hashCode + (keyHash ^ valHashNew) - (keyHash ^ valHashOld);
            } else {
                final int valHashNew = Objects.hashCode(val);
                root = newRootNode;
                hashCode += (keyHash ^ valHashNew);
                size += 1;
            }
        }

        return details;
    }

    @Override
    public V remove(Object o) {
        @SuppressWarnings("unchecked") final K key = (K) o;
        return removeAndGiveDetails(key).getReplacedValue();
    }

    @NonNull PersistentTrieMapHelper.ChangeEvent<V> removeAndGiveDetails(final K key) {
        final int keyHash = Objects.hashCode(key);
        final PersistentTrieMapHelper.ChangeEvent<V> details = new PersistentTrieMapHelper.ChangeEvent<>();
        final PersistentTrieMapHelper.Node<K, V> newRootNode = root.removed(bulkEdit, key, keyHash, 0, details);
        if (details.isModified()) {
            assert details.hasReplacedValue();
            final int valHash = Objects.hashCode(details.getReplacedValue());
            root = newRootNode;
            hashCode = hashCode - (keyHash ^ valHash);
            size = size - 1;
        }
        return details;
    }

    public PersistentTrieMap<K, V> toPersistent() {
        bulkEdit = new PersistentTrieHelper.Nonce();
        return size == 0 ? PersistentTrieMap.of() : new PersistentTrieMap<>(root, hashCode, size);
    }

}


