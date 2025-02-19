/*
 * @(#)DenseIntSet16Bit.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import java.util.Arrays;
import java.util.BitSet;

/**
 * A dense set of integer values.
 * <p>
 * This set is optimised for frequent setting and clearing.
 * <p>
 * Setting a boolean at an index is O(1).
 * <p>
 * Clearing the set is O(1) amortized. Every 255 times it takes O(n) time to
 * clear the set.
 * <p>
 * Storage space is one byte per boolean.
 */
public class DenseIntSet16Bit implements IntSet {
	/**
	 * This is a char array but it is treated like an unsigned short array.
	 */
	private char[] a;
	/**
	 * This is a char but it is treated like an unsigned short.
	 */
	private char mark = 1;

	/**
	 * Creates an empty set.
	 */
	public DenseIntSet16Bit() {
		this(0);
	}

	/**
	 * Creates a set with the specified capacity.
	 */
	public DenseIntSet16Bit(final int capacity) {
		a = new char[capacity];
	}

	@Override
	public boolean addAsInt(final int element) {
		if (a[element] != mark) {
			a[element] = mark;
			return true;
		}
		return false;
	}

	@Override
	public boolean removeAsInt(final int index) {
		if (a[index] == mark) {
			a[index] = 0;
			return true;
		}
		return false;
	}

	@Override
	public boolean containsAsInt(final int e) {
		return a[e] == mark;
	}

	@Override
	public void clear() {
		if (++mark == 0) {
			Arrays.fill(a, (char) 0);
			mark = 1;
		}
	}

	/**
	 * Sets the capacity of the set.
	 *
	 * @param capacity the new capacity
	 */
	public void setCapacity(final int capacity) {
		a = Arrays.copyOf(a, capacity);
	}

	/**
	 * Gets the capacity of the set.
	 *
	 * @return the capacity
	 */
	public int capacity() {
		return a.length;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final DenseIntSet16Bit that = (DenseIntSet16Bit) o;
		return this.capacity() == that.capacity()
				&& Arrays.equals(this.toLongArray(), that.toLongArray());
	}

	/**
	 * The hash code is the same as {@link BitSet#hashCode()}.
	 *
	 * @return hashcode
	 */
	@Override
	public int hashCode() {
		long h = 1234;
		final long[] words = toLongArray();
		for (int i = words.length; --i >= 0; ) {
			h ^= words[i] * (i + 1);
		}
		return (int) ((h >> 32) ^ h);
	}

	/**
	 * Returns a new long array containing all the bits in this bit set.
	 *
	 * @return a new long array.
	 */
	public long[] toLongArray() {
		final int length = (a.length - 1 >>> 6) + 1;
		final long[] words = new long[length];
		int lastSetBit = -1;
		for (int i = 0, n = capacity(); i < n; i++) {
			if (containsAsInt(i)) {
				lastSetBit = i;
				final int wordIndex = i >>> 6;
				final int bitIndex = i & 63;
				words[wordIndex] |= 1L << bitIndex;
			}
		}
		final int usedLength = lastSetBit == -1 ? 0 : (lastSetBit >>> 6) + 1;
		return usedLength == length ? words : Arrays.copyOf(words, usedLength);
	}

	@Override
	public String toString() {
		final StringBuilder buf = new StringBuilder();
		buf.append('{');
		int i = 0;
		final int n = capacity();
		for (; i < n; i++) {
			if (containsAsInt(i)) {
				buf.append(i++);
				break;
			}
		}
		for (; i < n; i++) {
			if (containsAsInt(i)) {
				buf.append(", ");
				buf.append(i);
			}
		}
		buf.append('}');
		return buf.toString();
	}
}
