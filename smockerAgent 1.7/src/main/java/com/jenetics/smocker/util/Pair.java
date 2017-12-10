package com.jenetics.smocker.util;

public class Pair<L, R> {

	private L left = null;
	private R right = null;

	public Pair() {
	}

	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}

	public void setLeft(L left) {
		this.left = left;
	}

	public void setRight(R right) {
		this.right = right;
	}

	public L getLeft() {
		return left;
	}

	public R getRight() {
		return right;
	}

	@Override
	public int hashCode() {
		return left.hashCode() ^ right.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair))
			return false;
		Pair pairo = (Pair) o;
		return this.left.equals(pairo.getLeft()) && this.right.equals(pairo.getRight());
	}

}