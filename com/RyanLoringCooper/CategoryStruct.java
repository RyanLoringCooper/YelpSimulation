package com.RyanLoringCooper;

public class CategoryStruct {
	public String cat;
	public String bid;

	public CategoryStruct(String cat, String bid) {
		this.cat = cat;
		this.bid = bid;
	}

	public boolean sameCategory(String cat) {
		return this.cat.equals(cat);
	}

	public boolean sameBusiness(String bid) {
		return this.bid.equals(bid);
	}
}