package au.org.ala.bhl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;

import au.org.ala.bhl.service.WebServiceHelper;

public class TaxonGrab {

    static List<SubstPattern> ACCENTS;
    static List<SubstPattern> SYMBOLS;    

    private static Pattern pat_symbols = Pattern.compile("[\\$\\%\\|\\{\\}\\*\\+\\?\\=\\-\\'\\^\\/\\@\\&]|[0-9]");
    private static Pattern pat_bracket_w = Pattern.compile("\\([\\sa-z]+");
    private static Pattern pat_brack_rem = Pattern.compile("[\\(\\)]");
    private static Pattern pat_punct_rem = Pattern.compile("[\\.,]");
    private static Pattern pat_dot = Pattern.compile("\\.");
    // Names
    private static Pattern pat_var_sub = Pattern.compile("^[A-Za-z\\(\\)]{2,}");
    private static Pattern pat_fam = Pattern.compile("\\A(^[A-Z][a-z]+)|(^[A-Z][a-z]?\\.)\\z");
    private static Pattern pat_vs = Pattern.compile("var|subsp", Pattern.CASE_INSENSITIVE);
    private static Pattern pat_spec = Pattern.compile("^[a-z]{3,}.");
    private static Pattern pat_subgen = Pattern.compile("\\A\\([A-Z][a-z]{3,}\\)\\z");
    private static Pattern pat_var_spec = Pattern.compile("^[a-zA-Z\\(\\)]{2,}");
    private static Pattern par_subvar = Pattern.compile("var|subsp|subg|ssp", Pattern.CASE_INSENSITIVE);
    // Patterns used in unification
    private static Pattern pat_bracket_beg = Pattern.compile("\\(");
    private static Pattern pat_bracket_end = Pattern.compile("\\)");

    static {
        ACCENTS = createSubstList(SUBST("á", "a"), SUBST("é", "e"), SUBST("í", "i"), SUBST("ó", "o"), SUBST("ú", "u"));
        SYMBOLS = createSubstList(SUBST(" -\r", " - "), SUBST(" -\n", " - "), SUBST("-\r", ""), SUBST("-\n", ""), SUBST("\r", " "), SUBST("\t", ""), SUBST(":", " "), SUBST(";", " "), SUBST(".", ". "));
    }
    
    private Set<String> _lexicon;
    
    public TaxonGrab() {
    	WordLists.loadWordLists();    	
    }

    public List<String> findNames(String text, String language) {
    	
    	_lexicon = WordLists.getWordList(language);
    	
    	if (_lexicon == null) {
    		System.err.println("Warning! No words found for language: " + language);
    		_lexicon = new HashSet<String>();
    	}
        
        String[] tokens = normalizeText(text, SYMBOLS).split(" ");

        List<String> words = new ArrayList<String>();
        for (String token: tokens) {
            if (!StringUtils.isEmpty(token)) {
                words.add(token);
            }
        }
        
        SearchState state = new SearchState();
        for (String word : words) {
            analyse(word, _lexicon, state);
        }

        return removeUnverifiedNames(state.Taxa);
    }
    
    private List<String> removeUnverifiedNames(Collection<String> names) {
    	List<String> verified = new ArrayList<String>();
    	
    	for (String name : names) {
    		try {
				JsonNode root = WebServiceHelper.getJSON(String.format("http://bie.ala.org.au/ws/guid/%s", URLEncoder.encode(name, "utf-8")));
				if (root.isArray() && root.size() > 0) {
					verified.add(name);
				}
								
			} catch (Exception e) {
				e.printStackTrace();
			}  		
    	}
    	
    	return verified;
    	
    }
    
    private void analyse(String word, Set<String> lexicon, SearchState state) {
        if (!pat_symbols.matcher(word).find()) {
            Matcher m = pat_bracket_w.matcher(word); 
            if (m.find()) {
                word = m.replaceAll("");
            }            
            
            String wordKey = pat_punct_rem.matcher(word).replaceAll("").toLowerCase();
            
            if (!StringUtils.isEmpty(state.CurrentFullName) && pat_var_sub.matcher(wordKey).find()) {
                state.Taxa.push(state.CurrentFullName + " " + word);
                state.CurrentFullName = null;
            }

            // if the word is contained in the lexicon it is discarded
	            
            
            if (!lexicon.contains(normalizeText(wordKey, ACCENTS))) {
                
                if (pat_fam.matcher(word).find() && !pat_vs.matcher(word).find()) {
                    state.F_Word = word;
                    state.S_Word = "";
                } else if (!StringUtils.isEmpty(state.F_Word) && StringUtils.isEmpty(state.S_Word)) {
                    word = word.replace(",", "");
                    if (pat_spec.matcher(word).find()) {
                        state.S_Word = word;
                        state.Taxa.push(state.F_Word + " " + state.S_Word);
                    } else if (pat_subgen.matcher(word).find()) {
                        state.S_Word = word;
                    } else {
                        state.S_Word = state.F_Word = null;
                    }
                } else if (!StringUtils.isEmpty(state.F_Word) && !StringUtils.isEmpty(state.S_Word) && word.length() > 2) {
                    word = word.replace(",", "");
                    if (pat_var_spec.matcher(word).find()) {
                    	if (state.Taxa.size() > 0) {
                    		state.Taxa.pop();
                    	}
                    	
                        if (par_subvar.matcher(word).find()) {
                            state.CurrentFullName = state.F_Word + " " + state.S_Word + " " + word;
                        } else if (!pat_dot.matcher(word).find()) {
                            state.Taxa.push(state.F_Word + " " + state.S_Word + " " + word);
                        }
                    }
                    state.F_Word = state.S_Word = null;
                } else {
                    state.F_Word = state.S_Word = null;
                }
                
            } else {
                state.F_Word = state.S_Word = null;                
            }
            
        } else {
            state.F_Word =  state.S_Word = state.CurrentFullName = null;                   
        }
    }

    public String normalizeText(String text, List<SubstPattern> patterns) {
        String ntext = text;
        for (SubstPattern p : patterns) {
            ntext = p.replaceAll(ntext);
        }
        return ntext;
    }

    private Set<String> loadLexicon() {
        HashSet<String> set = new HashSet<String>();
        String path = "/au/org/ala/bhl/english.txt";
        InputStream is = TaxonGrab.class.getResourceAsStream(path);
        try {
            @SuppressWarnings("unchecked")
            List<String> lines = IOUtils.readLines(is);
            int count = 0;
            for (String line : lines) {
                StringBuilder word = new StringBuilder();
                for (int i = 0; i < line.length(); ++i) {
                    char ch = line.charAt(i);
                    if (Character.isLetter(ch)) {
                        word.append(ch);
                    }
                }
                count++;
                set.add(word.toString().toLowerCase());
            }

            System.out.println("" + count + " words loaded.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return set;
    }

    private static SubstPattern SUBST(String what, String with) {
        return new SubstPattern(with, what);
    }

    private static List<SubstPattern> createSubstList(SubstPattern... patterns) {
        List<SubstPattern> results = new ArrayList<SubstPattern>();
        for (SubstPattern p : patterns) {
            results.add(p);
        }
        return results;
    }

}

class SearchState {
    public String F_Word;
    public String S_Word;
    public Stack<String> Taxa = new Stack<String>();
    public String CurrentFullName;    
}

class SubstPattern {

    private String _subst;
    private List<String> _targets;

    public SubstPattern(String substWith, String... matches) {
        _subst = substWith;
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

}
