package au.org.ala.bhl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

public class DateTest {

	@Test
	public void testDate0() throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss zz YYYY");
		Date d = Calendar.getInstance().getTime();
		System.out.println(d);
		System.out.println(sdf.format(d));

	}

	@Test
	public void testDate() throws Exception {
		String dateStr = "Fri Dec 02 18:32:51 EST 2011";

		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd hh:mm:ss ZZZ YYYY");
		Date d = sdf.parse(dateStr);
		System.out.println(d);

	}

}
