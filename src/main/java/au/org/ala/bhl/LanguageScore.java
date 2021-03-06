/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 *   
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *   
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.bhl;

/**
 * Transfer Object class holding the results of a language detection attempt
 * 
 * @author baird
 *
 */
public class LanguageScore {
	
	private String _language;
	private double _score;
	
	public LanguageScore(String name, double score) {
		_language = name;
		_score = score;
	}
	
	public String getName() {
		return _language;
	}
	
	public double getScore() {
		return _score;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%g)", _language, _score);
	}

}
