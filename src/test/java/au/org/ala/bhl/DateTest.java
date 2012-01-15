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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class DateTest {

	@Test
	public void testDate0() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zz yyyy");
		Date d = Calendar.getInstance().getTime();
		System.out.println(d);
		System.out.println(sdf.format(d));

	}

	@Test
	public void testDate() throws Exception {
		String dateStr = "Fri Dec 02 18:32:51 EST 2011";

		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss ZZZ yyyy");
		Date d = sdf.parse(dateStr);
		System.out.println(d);

	}

}
