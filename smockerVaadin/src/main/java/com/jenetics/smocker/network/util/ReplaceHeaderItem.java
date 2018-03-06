package com.jenetics.smocker.network.util;

/**
 * Used to store the replacement in header
 * @author igolus
 *
 */
public class ReplaceHeaderItem {
	private String regExp;
	private String replaceValue;
	
	public ReplaceHeaderItem() {
		super();
	}

	public ReplaceHeaderItem(String regExp, String reapceValue) {
		super();
		this.regExp = regExp;
		this.replaceValue = reapceValue;
	}

	public String getRegExp() {
		return regExp;
	}

	public String getReplaceValue() {
		return replaceValue;
	}
	
	
	public void setRegExp(String regExp) {
		this.regExp = regExp;
	}

	public void setReplaceValue(String replaceValue) {
		this.replaceValue = replaceValue;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((regExp == null) ? 0 : regExp.hashCode());
		result = prime * result + ((replaceValue == null) ? 0 : replaceValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReplaceHeaderItem other = (ReplaceHeaderItem) obj;
		if (regExp == null) {
			if (other.regExp != null)
				return false;
		} else if (!regExp.equals(other.regExp))
			return false;
		if (replaceValue == null) {
			if (other.replaceValue != null)
				return false;
		} else if (!replaceValue.equals(other.replaceValue))
			return false;
		return true;
	}
	
	
	
}
