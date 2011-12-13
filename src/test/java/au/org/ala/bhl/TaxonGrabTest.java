package au.org.ala.bhl;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class TaxonGrabTest {
    
    @Test
    public void test1() {
        TaxonGrab tg = new TaxonGrab();
        tg.findNames("bcd asdhj jHAS Kjash", "english");
    }
    
    @Test
    public void testNormalize1() {
        TaxonGrab tg = new TaxonGrab();
        String actual = tg.normalizeText("x -\rx -\nx:x;x.", TaxonGrab.SYMBOLS);
        
        System.err.println(actual);
    }
    
    @Test
    public void testFind1() throws Exception {
        
        InputStream is = TaxonGrabTest.class.getResourceAsStream("/sample1.txt");
        String data = IOUtils.toString(is);
        
        TaxonGrab tg = new TaxonGrab();
        List<String> names = tg.findNames(data, "english");
        
        for (String name : names) {
            System.err.println(name);
        }
        
    }


}
