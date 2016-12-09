/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang.collection;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static javaslang.API.TODO;

/**
 * THIS CLASS IS INTENDED TO BE USED INTERNALLY ONLY!
 * <p>
 * This helper class is similar to scala.collection.JavaConverters.
 * It provides methods that return views on Java collections.
 * <p>
 * These views are tightly coupled to collections of same characteristics, e.g.
 *
 * <ul>
 * <li>javaslang.collection.Seq has a java.util.List view</li>
 * <li>javaslang.collection.Set has a java.util.Set view</li>
 * <li>javaslang.collection.SortedSet has a java.util.NavigableSet view</li>
 * <li>javaslang.collection.Map has a java.util.Map view</li>
 * <li>javaslang.collection.SortedMap has a java.util.NavigableMap view</li>
 * <li>javaslang.collection.Multimap has a java.util.Map view</li>
 * <li>javaslang.collection.SortedMultimap has a java.util.NavigableMap view</li>
 * </ul>
 *
 * Subtypes of the Javaslang types mentioned above can have special views that make use of optimized implementations.
 *
 * @author Daniel Dietrich, Pap Lőrinc, Sean Flanigan
 * @since 2.1.0
 */
class JavaConverters {

    private JavaConverters() {
    }

    static <T> java.util.List<T> asJava(IndexedSeq<T> seq, ChangePolicy changePolicy) {
        return new ListView<>(seq, changePolicy.isMutable());
    }

    static <T> java.util.List<T> asJava(LinearSeq<T> seq, ChangePolicy changePolicy) {
        return new ListView<>(seq, changePolicy.isMutable());
    }

    static <T> java.util.Set<T> asJava(Set<T> set, ChangePolicy changePolicy) {
        return TODO("new SetView<>(set, changePolicy.isMutable());");
    }

    static <T> java.util.NavigableSet<T> asJava(SortedSet<T> set, ChangePolicy changePolicy) {
        return TODO("new NavigableSetView<>(set, changePolicy.isMutable());");
    }

    static <K, V> java.util.Map<K, V> asJava(Map<K, V> map, ChangePolicy changePolicy) {
        return TODO("new MapView<>(map, changePolicy.isMutable());");
    }

    static <K, V> java.util.NavigableMap<K, V> asJava(SortedMap<K, V> map, ChangePolicy changePolicy) {
        return TODO("new NavigableMapView<>(map, changePolicy.isMutable());");
    }

    /*
    static <K, V> java.util.Map<K, java.util.Collection<V>> asJava(Multimap<K, V> map, ChangePolicy changePolicy) {
        return TODO("new MapView<>(map, changePolicy.isMutable());");
    }

    TODO: create interface javaslang.collection.SortedMultimap
    static <K, V> java.util.NavigableMap<K, java.util.Collection<V>> asJava(SortedMultimap<K, V> map, ChangePolicy changePolicy) {
        return TODO("new NavigableMapView<>(map, changePolicy.isMutable());");
    }
    */

    enum ChangePolicy {

        IMMUTABLE, MUTABLE;

        boolean isMutable() {
            return this == MUTABLE;
        }
    }

    // -- private view implementations

    /**
     * Encapsulates the access to delegate and performs mutability checks.
     *
     * @param <C> The Javaslang collection type
     */
    private static abstract class HasDelegate<C extends Traversable<?>> implements Serializable {

        private static final long serialVersionUID = 1L;

        private C delegate;
        private final boolean mutable;

        HasDelegate(C delegate, boolean mutable) {
            this.delegate = delegate;
            this.mutable = mutable;
        }

        boolean isMutable() {
            return mutable;
        }

        C getDelegate() {
            return delegate;
        }

        C setDelegate(C newDelegate) {
            if (mutable) {
                final C previousDelegate = delegate;
                delegate = newDelegate;
                return previousDelegate;
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class ListView<T> extends HasDelegate<Seq<T>> implements java.util.List<T> {

        private static final long serialVersionUID = 1L;

        ListView(Seq<T> delegate, boolean mutable) {
            super(delegate, mutable);
        }

        @Override
        public boolean add(T element) {
            return setDelegateAndCheckChanged(getDelegate().append(element));
        }

        @Override
        public void add(int index, T element) {
            // may throw an IndexOutOfBoundsException accordingly to the j.u.List.add(int, T)
            setDelegate(getDelegate().insert(index, element));
        }

        @Override
        public boolean addAll(Collection<? extends T> collection) {
            return setDelegateAndCheckChanged(getDelegate().appendAll(collection));
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> collection) {
            // may throw an IndexOutOfBoundsException accordingly to the j.u.List.addAll(int, Collection)
            return setDelegateAndCheckChanged(getDelegate().insertAll(index, collection));
        }

        @Override
        public void clear() {
            setDelegate(getDelegate().take(0));
        }

        @Override
        public boolean contains(Object obj) {
            // may throw a ClassCastException accordingly to the j.u.List.contains(Object)
            @SuppressWarnings("unchecked") final T that = (T) obj;
            return getDelegate().contains(that);
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            // may throw a ClassCastException accordingly to the j.u.List.containsAll(Collection)
            @SuppressWarnings("unchecked") final Collection<T> that = (Collection<T>) collection;
            return getDelegate().containsAll(that);
        }

        @Override
        public T get(int index) {
            // may throw an IndexOutOfBoundsException accordingly to the j.u.List.get(int)
            return getDelegate().get(index);
        }

        @Override
        public int indexOf(Object obj) {
            // may throw a ClassCastException accordingly to the j.u.List.indexOf(Object)
            @SuppressWarnings("unchecked") final T that = (T) obj;
            return getDelegate().indexOf(that);
        }

        @Override
        public boolean isEmpty() {
            return getDelegate().isEmpty();
        }

        @Override
        public Iterator<T> iterator() {
            return getDelegate().iterator();
        }

        @Override
        public int lastIndexOf(Object obj) {
            // may throw a ClassCastException accordingly to the j.u.List.lastIndexOf(Object)
            @SuppressWarnings("unchecked") final T that = (T) obj;
            return getDelegate().lastIndexOf(that);
        }

        @Override
        public ListIterator<T> listIterator() {
            return ListIterator.of(getDelegate(), 0, isMutable());
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return ListIterator.of(getDelegate(), index, isMutable());
        }

        @Override
        public T remove(int index) {
            // may throw an IndexOutOfBoundsException accordingly to the j.u.List.remove(int)
            return setDelegateAndGetPreviousElement(index, getDelegate().removeAt(index));
        }

        @Override
        public boolean remove(Object obj) {
            // may throw a ClassCastException accordingly to the j.u.List.remove(Object)
            @SuppressWarnings("unchecked") final T that = (T) obj;
            return setDelegateAndCheckChanged(getDelegate().remove(that));
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            // may throw a ClassCastException accordingly to the j.u.List.removeAll(Collection)
            @SuppressWarnings("unchecked") final Collection<T> that = (Collection<T>) collection;
            return setDelegateAndCheckChanged(getDelegate().removeAll(that));
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            // may throw a ClassCastException accordingly to j.u.List.retainAll(Collection)
            @SuppressWarnings("unchecked") final Collection<T> that = (Collection<T>) collection;
            return setDelegateAndCheckChanged(getDelegate().retainAll(that));
        }

        @Override
        public T set(int index, T element) {
            // may throw an IndexOutOfBoundsException accordingly to the j.u.List.set(int, T)
            return setDelegateAndGetPreviousElement(index, getDelegate().update(index, element));
        }

        @Override
        public int size() {
            return getDelegate().size();
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            // may throw IndexOutOfBoundsException accordingly to j.u.List.subList(int, int)
            return new ListView<>(getDelegate().subSequence(fromIndex, toIndex), isMutable());
        }

        @Override
        public Object[] toArray() {
            return getDelegate().toJavaArray();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <U> U[] toArray(U[] array) {
            final U[] target;
            final int length = getDelegate().length();
            if (array.length < length) {
                final Class<? extends Object[]> newType = array.getClass();
                target = (newType == Object[].class)
                         ? (U[]) new Object[length]
                         : (U[]) java.lang.reflect.Array.newInstance(newType.getComponentType(), length);
            } else {
                target = array;
            }
            final Iterator<T> iter = iterator();
            for (int i = 0; i < length; i++) {
                target[i] = (U) iter.next();
            }
            return target;
        }

        // -- Object.*

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o instanceof java.util.List) {
                return Collections.areEqual(getDelegate(), (java.util.List<?>) o);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return getDelegate().hashCode();
        }

        @Override
        public String toString() {
            return getDelegate().mkString("[", ", ", "]");
        }

        // -- private helpers

        private boolean setDelegateAndCheckChanged(Seq<T> delegate) {
            return setDelegate(delegate) != delegate;
        }

        private T setDelegateAndGetPreviousElement(int index, Seq<T> delegate) {
            return setDelegate(delegate).get(index);
        }

        private static class ListIterator<T> extends HasDelegate<IndexedSeq<T>> implements java.util.ListIterator<T> {

            private static final long serialVersionUID = 1L;

            private int index;

            static <T> ListIterator<T> of(Seq<T> seq, int index, boolean mutable) {
                final IndexedSeq<T> indexedSeq = (seq instanceof IndexedSeq) ? (IndexedSeq<T>) seq : seq.toVector();
                return new ListIterator<>(indexedSeq, index, mutable);
            }

            ListIterator(IndexedSeq<T> delegate, int index, boolean mutable) {
                super(delegate, mutable);
                this.index = index;
            }

            @Override
            public boolean hasNext() {
                return index < getDelegate().length();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return getDelegate().get(index++);
            }

            @Override
            public int nextIndex() {
                return index;
            }

            @Override
            public boolean hasPrevious() {
                return index > 0;
            }

            @Override
            public T previous() {
                if (!hasPrevious()) {
                    throw new NoSuchElementException();
                }
                return getDelegate().get(--index);
            }

            @Override
            public int previousIndex() {
                return index - 1;
            }

            @Override
            public void remove() {
                /* TODO:
                 * @throws IllegalStateException if neither {@code next} nor
                 *         {@code previous} have been called, or {@code remove} or
                 *         {@code add} have been called after the last call to
                 *         {@code next} or {@code previous}
                 */
                setDelegate(getDelegate().removeAt(index));
            }

            @Override
            public void set(T value) {
                /* TODO:
                 * @throws ClassCastException if the class of the specified element
                 *         prevents it from being added to this list
                 * @throws IllegalArgumentException if some aspect of the specified
                 *         element prevents it from being added to this list
                 * @throws IllegalStateException if neither {@code next} nor
                 *         {@code previous} have been called, or {@code remove} or
                 *         {@code add} have been called after the last call to
                 *         {@code next} or {@code previous}
                 */
                setDelegate(getDelegate().update(index, value));
            }

            @Override
            public void add(T value) {
                /* TODO:
                 * @throws ClassCastException if the class of the specified element
                 *         prevents it from being added to this list
                 * @throws IllegalArgumentException if some aspect of this element
                 *         prevents it from being added to this list
                 */
                setDelegate(getDelegate().insert(index, value));
            }
        }
    }

    private static abstract class SetView<T> extends HasDelegate<Set<T>> implements java.util.Set<T> {

        private static final long serialVersionUID = 1L;

        SetView(Set<T> delegate, boolean mutable) {
            super(delegate, mutable);
        }

        // TODO
    }

    private static abstract class NavigableSetView<T> extends HasDelegate<SortedSet<T>> implements java.util.NavigableSet<T> {

        private static final long serialVersionUID = 1L;

        NavigableSetView(SortedSet<T> delegate, boolean mutable) {
            super(delegate, mutable);
        }

        // TODO
    }

    private static abstract class MapView<K, V> extends HasDelegate<Map<K, V>> implements java.util.Map<K, V> {

        private static final long serialVersionUID = 1L;

        MapView(Map<K, V> delegate, boolean mutable) {
            super(delegate, mutable);
        }

        // TODO
    }

    private static abstract class NavigableMapView<K, V> extends HasDelegate<SortedMap<K, V>> implements java.util.NavigableMap<K, V> {

        private static final long serialVersionUID = 1L;

        NavigableMapView(SortedMap<K, V> delegate, boolean mutable) {
            super(delegate, mutable);
        }

        // TODO
    }
}
