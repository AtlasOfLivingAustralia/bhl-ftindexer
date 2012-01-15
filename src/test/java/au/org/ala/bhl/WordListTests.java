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

import java.io.InputStream;

import junit.framework.TestCase;

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

	// @Test
	// public void testDetectLanguage4() throws Exception {
	// String path = "/data/bhl-ftindex/doccache/2/21753000002021/00111_6026853.txt";
	// String text = FileUtils.readFileToString(new File(path), "utf-8");
	// LanguageScore lang = WordLists.detectLanguage(text, "english");
	// System.out.println(lang);
	//
	// }
	//

}
