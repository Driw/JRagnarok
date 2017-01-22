package org.diverproject.jragnaork.database;

public interface GenericDatabase<I> extends Iterable<I>
{
	void clear();

	int size();
	int length();

	boolean space();
	boolean contains(I item);
}
