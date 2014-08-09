/**
 * 
 */
package com.heliosapm.wiex.server.collectors.jdbc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Title: FormatTest</p>
 * <p>Description: Simple test class for metric name parsing</p> 
 * <p>Copyright: Copyright (c) 2007</p>
 * <p>Company: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision$
 */

public class FormatTest {

	static final String text = "TheBeatlesAre{0}And-{1}-And{2}And ({3} ) END";
	//final String regex = "(\\b[^\\{\\}])|(\\{\\d++\\})";
	static final String regex = "([a-zA-Z\\(\\)\\s-]+)|(\\{\\d++\\})";
	static final String regex2 = "\\d++";
	static final String[] binds = new String[]{"John", "George", "Paul", "Ringo"};
	static Pattern p = null;
	static Pattern p2 = null;
	
	static {
		p = Pattern.compile(regex);
		p2 = Pattern.compile(regex2);		
	}
	
	public static String format(String fText, String[] values) {
		StringBuilder buff = new StringBuilder();
		Matcher m = p.matcher(fText);
		String s = null;
		boolean found = m.find();
		while(found) {
			s = m.group();
			if(s.startsWith("{")) {
				Matcher m2 = p2.matcher(s);
				if(m2.find()) {
					int i = Integer.parseInt(m2.group());
					buff.append(values[i]);					
				}
			} else {
				buff.append(s);
			}
			found = m.find();
		}
		return buff.toString();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		log(format(text, binds));
	}
	
	public static void log(Object message) {
		System.out.println(message);
	}

}
