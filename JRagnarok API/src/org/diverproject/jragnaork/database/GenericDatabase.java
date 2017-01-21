package org.diverproject.jragnaork.database;

public interface GenericDatabase<I>
{
	void clear();

	int size();
	int length();

	boolean space();
	boolean contains(I item);
}
