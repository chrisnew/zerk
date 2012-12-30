package de.chrisnew.zerk.game;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.chrisnew.zerk.game.entities.BaseEntity;

public class EntityCollection implements Collection<BaseEntity> {

//	private final ConcurrentSkipListSet<BaseEntity> backend = new ConcurrentSkipListSet<>();
	private final List<BaseEntity> backend = Collections.synchronizedList(new LinkedList<BaseEntity> ());

	@Override
	public int size() {
		return backend.size();
	}

	@Override
	public boolean isEmpty() {
		return backend.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return backend.contains(o);
	}

	@Override
	public Iterator<BaseEntity> iterator() {
		return backend.iterator();
	}

	@Override
	public Object[] toArray() {
		return backend.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return backend.toArray(a);
	}

	@Override
	public boolean add(BaseEntity e) {
		return backend.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return backend.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return backend.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends BaseEntity> c) {
		return backend.addAll(c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return backend.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return backend.retainAll(c);
	}

	@Override
	public void clear() {
		backend.clear();
	}

	public BaseEntity getEntityById(int id) {
		for (BaseEntity be : this) {
			if (be.getId() == id) {
				return be;
			}
		}

		return null;
	}

	public boolean hasEntityById(int id) {
		for (BaseEntity be : this) {
			if (be.getId() == id) {
				return true;
			}
		}

		return false;
	}
}
