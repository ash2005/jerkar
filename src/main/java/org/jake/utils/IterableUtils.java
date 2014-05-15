package org.jake.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class IterableUtils {
	
	public static Iterable<File> emptyFile() {
		return Collections.emptyList();
	}
	
	public static <T> Iterable<T> single(T item) {
		final List<T> result = new LinkedList<T>();
		result.add(item);
		return result;
	}
	
	
	public static <T> List<T> toList(Iterable<T> it) {
		if (it instanceof List) {
			return (List<T>) it;
		}
		List<T> result = new LinkedList<T>();
		for (T t : it) {
			result.add(t);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T[] toArray(Iterable<T> it, Class<T> clazz) {
		List<T> list = toList(it);
		T[] result = (T[]) Array.newInstance(clazz, list.size());
		int i = 0;
		for (T t : it) {
			result[i] = t;
			i++;
		}
		return result;
	}
	
	public static <T> Iterable<T> chain(Iterable<T> ... iterables) {
		return chainAll(Arrays.asList(iterables));
	}
	
	@SuppressWarnings("unchecked")
	public static <T> Iterable<T> chain(Iterable<T> iterable, T ... items) {
		return chain(iterable, Arrays.asList(items));
	}
	
	public static <T> Iterable<T> chain(T item, Iterable<T> ... iterables) {
		final List<Iterable<T>> list = new LinkedList<Iterable<T>>();
		final List<T> single = new ArrayList<T>(3);
		single.add(item);
		list.add(single);
		list.addAll(Arrays.asList(iterables));
		return chainAll(list);
	}
	
	public static <T> Iterable<T> chainAll(Iterable<Iterable<T>> iterables) {
		final List<Iterable<T>> effectiveIterables = removeEmptyIt(iterables);
		if (effectiveIterables.isEmpty()) {
			return Collections.emptyList();
		}
		if (effectiveIterables.size() == 1) {
			return effectiveIterables.get(0);
		}
		return new ChainedIterable<T>(effectiveIterables);
	}
	
	private static <T> List<Iterable<T>> removeEmptyIt(Iterable<Iterable<T>> iterables) {
		List<Iterable<T>> result = new LinkedList<Iterable<T>>();
		for (Iterable<T> iterable : iterables) {
			if (iterable.iterator().hasNext()) {
				result.add(iterable);
			}
		}
		return result;
	}
	
	public static final class ChainedIterable<T> implements Iterable<T> {
		
		private final Iterable<Iterable<T>> iterables;
		
		public ChainedIterable(Iterable<Iterable<T>> iterables) {
			super();
			this.iterables = iterables;
		}
		
		public ChainedIterable(Iterable<T>... iterables) {
			this(Arrays.asList(iterables));
		}


		@Override
		public Iterator<T> iterator() {
			List<Iterator<T>> iterators = new LinkedList<Iterator<T>>();
			for (Iterable<T> iterable : iterables) {
				iterators.add(iterable.iterator());
			}
			return new ChainedIterator<T>(iterators);
		}
		
	}
	
	
	private static final class ChainedIterator<T> implements Iterator<T> {
		
		private final Iterator<Iterator<T>> iterators;
		
		private Iterator<T> current;

		public ChainedIterator(Iterable<Iterator<T>> iterables) {
			super();
			this.iterators = iterables.iterator();
			current = iterators.next();
		}
		

		@Override
		public boolean hasNext() {
			boolean currentNext = current.hasNext();
			if (currentNext) {
				return true;
			}
			while(iterators.hasNext()) {
				current = iterators.next();
				if (current.hasNext()) {
					return true;
				}
			}
			return false;
		}

		@Override
		public T next() {
			if (current.hasNext()) {
				return current.next();
			} 
			while(iterators.hasNext()) {
				current = iterators.next();
				if (current.hasNext()) {
					return current.next();
				}
			}
			return current.next();
		}

		@Override
		public void remove() {
			current.remove();
		}
		
	}
	
	
	

}