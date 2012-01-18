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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Utility class that holds lexicons in various languages.
 * 
 * @author baird
 *
 */
public class WordLists {

	private static HashMap<String, Set<String>> _wordLists = new HashMap<String, Set<String>>();

	private static List<CharacterSubstitution> ACCENTS = createSubstList(
			S("a", "á", "ä", "æ", "å"), 
			S("e", "é"), 
			S("i", "í"), 
			S("o", "ó", "ö", "ø"), 
			S("u", "ú", "ü"), 
			S("b", "ß")
    );
	
	private static List<CharacterSubstitution> SYMBOLS = createSubstList(
			S(" - ", " -\r"), 
			S(" - ", " -\n"), 
			S("", "-\r"), 
			S("", "-\n"), 
			S(" ", "\r"), 
			S(" ", "\n"),
			S("", "\t"), 
			S(" ", ":"), 
			S(" ", ";"), 
			S(". ", ".")
	);	

	public static void loadWordLists() {		
		loadLists("english", "german", "dutch", "french", "danish");	
	}
	
	private static void loadLists(String...languages) {
		for (String lang : languages) {
			String path = String.format("/au/org/ala/bhl/%s.txt", lang);
			_wordLists.put(lang, loadWordList(path));
		}
	}
	
	public static List<String> sanitize(String text) {
		String[] tokens = text.split("\\s");
		
        List<String> words = new ArrayList<String>();
        
        for (String token: tokens) {
            if (!StringUtils.isEmpty(token)) {
            	
            	StringBuilder b  = new StringBuilder();
            	for (int i = 0; i < token.length(); ++i) {
            		char ch = token.charAt(i);
            		if (".,;:{}[]()&$!@#`~;\"'".indexOf(ch) >= 0) {
            			continue;
            		}
            		
            		if (Character.isWhitespace(ch)) {
            			continue;
            		}
            		
            		if ("-".indexOf(ch) >= 0) {
                    	if (b.length() > 0) {	            	
                    		words.add(b.toString());
                    	} 
            			b = new StringBuilder();
            			continue;
            		}
            		
            		if (!Character.isLetter(ch)) {
            			// Throw away this token because it contains some other non-letter (numbers etc)
            			b = new StringBuilder();
            			break;
            		}
            		
            		b.append(ch);
            	}
            	
            	// Only consider words greater than one letter.
            	if (b.length() > 1) {	            	
            		words.add(b.toString());
            	}
            }
        }
        return words;
		
	}
	
	public static LanguageScore detectLanguage(String text, String preferredLanguage) {
		
		List<String> words = sanitize(text);
		
//		System.err.println(words);
        
        String bestLanguage = "";
        double bestScore = 0.0;
        
        for (String key : _wordLists.keySet()) {                
        	Set<String> lexicon = _wordLists.get(key);
        	
        	int match = 0;
        	for (String word : words) {
        		if (lexicon.contains(word.toLowerCase())) {
        			match++;
        		} else {
//         			System.err.println(key + "  " + word);
        		}
        	}
        	
        	double score = (double) match / (double) words.size();
        	
        	if (score > bestScore || score == bestScore && key.equalsIgnoreCase(preferredLanguage)) {
        		bestScore = score;
        		bestLanguage = key;
        	} 
        	
//         	System.err.println( key + ": " + score);
        	
        }
        
        if (StringUtils.isEmpty(bestLanguage)) {
        	bestLanguage = preferredLanguage;
        }
		
		return new LanguageScore(bestLanguage, bestScore);
	}

	private static HashSet<String> loadWordList(String resourcePath) {		
		HashSet<String> set = new HashSet<String>();
		InputStream is = TaxonGrab.class.getResourceAsStream(resourcePath);
		try {
			@SuppressWarnings("unchecked")
			List<String> lines = IOUtils.readLines(is);
			for (String line : lines) {
				StringBuilder word = new StringBuilder();
				StringBuilder wordAlt = new StringBuilder();
				
				for (int i = 0; i < line.length(); ++i) {
					char ch = line.charAt(i);
					char chAlt = substitute(ch);

					if (Character.isLetter(chAlt)) {						
						wordAlt.append(chAlt);
						if (chAlt > 127) {
							System.err.println(String.format("Non ascii letter: %s", ch));
						}
					}
					
					if (Character.isLetter(ch)) {
						word.append(ch);
					}
				}				
				set.add(word.toString().toLowerCase());
				if (!wordAlt.toString().equals(word.toString())) {
					set.add(wordAlt.toString());
				}
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}

		return set;

	}

	private static char substitute(char ch) {
		for (CharacterSubstitution s : ACCENTS) {
			for (String c : s.getTargets()) {
				if (Character.toLowerCase(ch) == c.charAt(0)) {
					return s.getSubstitue().charAt(0);
				}
			}
		}

		return ch;
	}

	protected static CharacterSubstitution S(String substitute, String... matches) {
		return new CharacterSubstitution(substitute, matches);
	}

	private static List<CharacterSubstitution> createSubstList(CharacterSubstitution... patterns) {
		List<CharacterSubstitution> results = new ArrayList<CharacterSubstitution>();
		for (CharacterSubstitution p : patterns) {
			results.add(p);
		}
		return results;
	}

	public static class CharacterSubstitution {

		private String _subst;
		private List<String> _targets;

		public CharacterSubstitution(String substitute, String... matches) {
			_subst = substitute;
			_targets = new ArrayList<String>();
			for (String match : matches) {
				_targets.add(match);
			}
		}

		public String replaceAll(String text) {
			String ntext = text;
			for (String t : _targets) {
				ntext = ntext.replace(t, _subst);
			}
			return ntext;
		}

		public Pattern getPattern() {
			StringBuilder regex = new StringBuilder("(");
			regex.append(StringUtils.join(_targets, "|"));
			regex.append(")");

			Pattern p = Pattern.compile(regex.toString());
			return p;
		}

		public String getSubstitue() {
			return _subst;
		}

		public List<String> getTargets() {
			return _targets;
		}

	}
	
	public static String normalizeText(String text) {
		return normalizeText(text, SYMBOLS);
	}
	
	public static String normalizeText(String text, List<CharacterSubstitution> patterns) {
        String ntext = text;
        for (CharacterSubstitution p : patterns) {
            ntext = p.replaceAll(ntext);
        }
        return ntext;
    }


	public static Set<String> getWordList(String language) {
		if (StringUtils.isEmpty(language)) {
			return null;
		}
		String key = language.toLowerCase();
		if (_wordLists.containsKey(key)) {
			return _wordLists.get(key);
		}	
		return null;
	}

}
