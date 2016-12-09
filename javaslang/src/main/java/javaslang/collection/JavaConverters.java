/*     / \____  _    _  ____   ______  / \ ____  __    _______
 *    /  /    \/ \  / \/    \ /  /\__\/  //    \/  \  //  /\__\   JΛVΛSLΛNG
 *  _/  /  /\  \  \/  /  /\  \\__\\  \  //  /\  \ /\\/ \ /__\ \   Copyright 2014-2016 Javaslang, http://javaslang.io
 * /___/\_/  \_/\____/\_/  \_/\__\/__/\__\_/  \_//  \__/\_____/   Licensed under the Apache License, Version 2.0
 */
package javaslang.collection;

import javaslang.Function1;
import javaslang.Tuple;
import javaslang.Tuple2;

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
    private static abstract class HasDelegate<C extends Traversable<T>, T> implements Serializable {

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

        // if mutable, applies f, which takes the old delegate and returns a modified delegate
        boolean swapDelegate(Function1<C, C> f) {
            if (mutable) {
                C newDelegate = f.apply(delegate);
                if (newDelegate != delegate) {
                    delegate = newDelegate;
                    return true;
                } else {
                    return false;
                }
            } else {
                throw new UnsupportedOperationException();
            }
        }

        // if mutable, applies f, which takes the old delegate, returns modified delegate and an element
        T swapDelegateAndGetElement(Function1<C, Tuple2<C, T>> f) {
            if (mutable) {
                Tuple2<C, T> newDelegateAndOldElement = f.apply(delegate);
                delegate = newDelegateAndOldElement._1;
                return newDelegateAndOldElement._2;
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private static class ListView<T> extends HasDelegate<Seq<T>, T> implements java.util.List<T> {

        private static final long serialVersionUID = 1L;

        ListView(Seq<T> delegate, boolean mutable) {
            super(delegate, mutable);
        }

        @Override
        public boolean add(T element) {
            return swapDelegate(old -> old.append(element));
        }

        @Override
        public void add(int index, T element) {
            // may throw an IndexOutOfBoundsException accordingly to the j.u.List.add(int, T)
            swapDelegate(old -> old.insert(index, element));
        }

        @Override
        public boolean addAll(Collection<? extends T> collection) {
            return swapDelegate(old -> old.appendAll(collection));
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> collection) {
            // may throw an IndexOutOfBoundsException accordingly to the j.u.List.addAll(int, Collection)
            return swapDelegate(old -> old.insertAll(index, collection));
        }

        @Override
        public void clear() {
            swapDelegate(old -> old.take(0));
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
            return new ListIterator<>(this, 0);
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return new ListIterator<>(this, index);
        }

        @Override
        public T remove(int index) {
            return swapDelegateAndGetElement(old -> {
                // may throw an IndexOutOfBoundsException according to j.u.List.remove(int)
                Seq<T> newDelegate = old.removeAt(index);
                return Tuple.of(newDelegate, old.get(index));
            });
        }

        @Override
        public boolean remove(Object obj) {
            // may throw a ClassCastException accordingly to the j.u.List.remove(Object)
            @SuppressWarnings("unchecked") final T that = (T) obj;
            return swapDelegate(old -> old.remove(that));
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            return swapDelegate(old -> {
                // may throw a ClassCastException according to j.u.List.removeAll(Collection)
                @SuppressWarnings("unchecked") final Collection<T> that = (Collection<T>) collection;
                return old.removeAll(that);
            });
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            return swapDelegate(old -> {
                // may throw a ClassCastException according to j.u.List.retainAll(Collection)
                @SuppressWarnings("unchecked") final Collection<T> that = (Collection<T>) collection;
                return old.retainAll(that);
            });
        }

        @Override
        public T set(int index, T element) {
            return swapDelegateAndGetElement(old -> {
                // may throw an IndexOutOfBoundsException according to j.u.List.set(int, T)
                Seq<T> newDelegate = old.update(index, element);
                return Tuple.of(newDelegate, old.get(index));
            });
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

        // DEV-Note: An iterator is intentionally not serializable.
        private static class ListIterator<T> implements java.util.ListIterator<T> {

            private ListView<T> list;
            private int index;
            private boolean dirty = true;

            ListIterator(ListView<T> list, int index) {
                this.list = list;
                this.index = index;
            }

            @Override
            public boolean hasNext() {
                return index < list.size();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                dirty = false;
                return list.get(index++);
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
                dirty = false;
                return list.get(--index);
            }

            @Override
            public int previousIndex() {
                return index - 1;
            }

            @Override
            public void remove() {
                checkDirty();
                // may throw a ClassCastException accordingly to j.u.ListIterator.remove()
                list.remove(index);
                dirty = true;
            }

            @Override
            public void set(T value) {
                checkDirty();
                // may throw a ClassCastException accordingly to j.u.ListIterator.set(T)
                list.set(index, value);
            }

            @Override
            public void add(T value) {
                /* TODO:
                 * The new element is inserted before the implicit
                 * cursor: a subsequent call to {@code next} would be unaffected, and a
                 * subsequent call to {@code previous} would return the new element.
                 * (This call increases by one the value that would be returned by a
                 * call to {@code nextIndex} or {@code previousIndex}.)
                 */
                // may throw a ClassCastException accordingly to j.u.ListIterator.add(T)
                list.add(index, value);
                dirty = true;
            }

            private void checkDirty() {
                if (dirty) {
                    throw new IllegalStateException();
                }
            }
        }
    }

    private static abstract class SetView<T> extends HasDelegate<Set<T>, T> implements java.util.Set<T> {

        private static final long serialVersionUID = 1L;

        SetView(Set<T> delegate, boolean mutable) {
            super(delegate, mutable);
        }

        // TODO
    }

    private static abstract class NavigableSetView<T> extends HasDelegate<SortedSet<T>, T> implements java.util.NavigableSet<T> {

        private static final long serialVersionUID = 1L;

        NavigableSetView(SortedSet<T> delegate, boolean mutable) {
            super(delegate, mutable);
        }

        // TODO
    }

    private static abstract class MapView<K, V> extends HasDelegate<Map<K, V>, Tuple2<K, V>> implements java.util.Map<K, V> {

        private static final long serialVersionUID = 1L;

        MapView(Map<K, V> delegate, boolean mutable) {
            super(delegate, mutable);
        }

        // TODO
    }

    private static abstract class NavigableMapView<K, V> extends HasDelegate<SortedMap<K, V>, Tuple2<K, V>> implements java.util.NavigableMap<K, V> {

        private static final long serialVersionUID = 1L;

        NavigableMapView(SortedMap<K, V> delegate, boolean mutable) {
            super(delegate, mutable);
        }

        // TODO
    }
}
