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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceGrab {
	
	private static Pattern[] _patterns = new Pattern[] {
		Pattern.compile("([\\d]+)\\s*(\\w+\\.*)\\s+(e|east|w|west|s|south|n|north|o|osten)\\s+(of|from)\\s+(\\w+)"),
		Pattern.compile("(\\d{1,3})\\s*.\\s*([\\d.]{1,2})'\\s*.\\s*([\\d.]{1,2})\"*\\s*(N|S|E|O|W|n|e|o|s|w)\\s*"),
		Pattern.compile("(\\d{1,3}).([\\d.]{1,2})'.([\\d.]{1,2})\"*\\s*(N|S|E|O|W|n|e|o|s|w)\\s*")		
	};
	
	public static List<String> findPlaces(String text) {
		List<String> results = new ArrayList<String>();
		String ntext = WordLists.normalizeText(text);
		for (Pattern p : _patterns) {
			Matcher m = p.matcher(ntext);
			while (m.find()) {				
				results.add(m.group());
			}
		}
		return results;
	}

}
