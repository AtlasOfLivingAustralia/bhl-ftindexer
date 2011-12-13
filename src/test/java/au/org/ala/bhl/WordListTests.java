package au.org.ala.bhl;

import java.io.File;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;


public class WordListTests extends TestCase {

	@Override
	protected void setUp() throws Exception {
		WordLists.loadWordLists();
	}
	
	@Test
	public void testLoad1() {
		WordLists.loadWordLists();
	}
	
	@Test
	public void normalizeTest1() throws Exception {		
        InputStream is = TaxonGrabTest.class.getResourceAsStream("/sample2.txt");
        String data = IOUtils.toString(is);
        
        String n = WordLists.normalizeText(data);
        System.err.println(n);
		
	}
	
    @Test
    public void testDetectLanguage1() throws Exception {
    	InputStream is = TaxonGrabTest.class.getResourceAsStream("/sample2.txt");
    	String text = IOUtils.toString(is);
    	LanguageScore lang = WordLists.detectLanguage(text, "english");
    	System.out.println(lang);
    }
    
    @Test
    public void testDetectLanguage2() throws Exception {
    	InputStream is = TaxonGrabTest.class.getResourceAsStream("/sample3.txt");
    	String text = IOUtils.toString(is);
    	LanguageScore lang = WordLists.detectLanguage(text, "english");
    	System.out.println(lang);
    }
    
    @Test
    public void testDetectLanguage3() throws Exception {
    	
    	InputStream is = TaxonGrabTest.class.getResourceAsStream("/sample4.txt");
    	String text = IOUtils.toString(is);
    	LanguageScore lang = WordLists.detectLanguage(text, "english");
    	System.out.println(lang);
    }
    
    @Test
    public void testDetectLanguage4() throws Exception {
    	String path = "J:\\data\\DocCache\\21753000002021\\00111_6026853.txt";
    	String text = FileUtils.readFileToString(new File(path), "utf-8");
    	LanguageScore lang = WordLists.detectLanguage(text, "english");
    	System.out.println(lang);

    }
    



}
