/**
 * 
 */
package com.heliosapm.wiex.server.collectors.ajax;

import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import org.apache.log4j.*;

/**
 * @author WhiteheN
 *
 */
public class UnCloseablePrintWriter extends PrintWriter implements FlushableCloseable {

	protected UnCloseableServletOutputStream ucos = null;
	protected PrintWriter inner = null;
	protected Logger log = null;
	/**
	 * @param out
	 */
	public UnCloseablePrintWriter(UnCloseableServletOutputStream ucos) {
		super(new PipedOutputStream(), false);
		log = Logger.getLogger(getClass());
		this.ucos = ucos;
		inner = new PrintWriter(ucos.getBuffer(), true);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param c
	 * @return
	 * @see java.io.PrintWriter#append(char)
	 */
	public PrintWriter append(char c) {
		
		return inner.append(c);
	}
	/**
	 * @param csq
	 * @param start
	 * @param end
	 * @return
	 * @see java.io.PrintWriter#append(java.lang.CharSequence, int, int)
	 */
	public PrintWriter append(CharSequence csq, int start, int end) {
		return inner.append(csq, start, end);
	}
	/**
	 * @param csq
	 * @return
	 * @see java.io.PrintWriter#append(java.lang.CharSequence)
	 */
	public PrintWriter append(CharSequence csq) {
		return inner.append(csq);
	}
	/**
	 * @return
	 * @see java.io.PrintWriter#checkError()
	 */
	public boolean checkError() {
		return inner.checkError();
	}
	/**
	 * 
	 * @see java.io.PrintWriter#close()
	 */
	public void close() {
		inner.close();
	}
	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return inner.equals(obj);
	}
	/**
	 * 
	 * @see java.io.PrintWriter#flush()
	 */
	public void flush() {
		inner.flush();
	}
	/**
	 * @param l
	 * @param format
	 * @param args
	 * @return
	 * @see java.io.PrintWriter#format(java.util.Locale, java.lang.String, java.lang.Object[])
	 */
	public PrintWriter format(Locale l, String format, Object... args) {
		return inner.format(l, format, args);
	}
	/**
	 * @param format
	 * @param args
	 * @return
	 * @see java.io.PrintWriter#format(java.lang.String, java.lang.Object[])
	 */
	public PrintWriter format(String format, Object... args) {
		return inner.format(format, args);
	}
	/**
	 * @return
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return inner.hashCode();
	}
	/**
	 * @param b
	 * @see java.io.PrintWriter#print(boolean)
	 */
	public void print(boolean b) {
		inner.print(b);
	}
	/**
	 * @param c
	 * @see java.io.PrintWriter#print(char)
	 */
	public void print(char c) {
		inner.print(c);
	}
	/**
	 * @param s
	 * @see java.io.PrintWriter#print(char[])
	 */
	public void print(char[] s) {
		inner.print(s);
	}
	/**
	 * @param d
	 * @see java.io.PrintWriter#print(double)
	 */
	public void print(double d) {
		inner.print(d);
	}
	/**
	 * @param f
	 * @see java.io.PrintWriter#print(float)
	 */
	public void print(float f) {
		inner.print(f);
	}
	/**
	 * @param i
	 * @see java.io.PrintWriter#print(int)
	 */
	public void print(int i) {
		inner.print(i);
	}
	/**
	 * @param l
	 * @see java.io.PrintWriter#print(long)
	 */
	public void print(long l) {
		inner.print(l);
	}
	/**
	 * @param obj
	 * @see java.io.PrintWriter#print(java.lang.Object)
	 */
	public void print(Object obj) {
		inner.print(obj);
	}
	/**
	 * @param s
	 * @see java.io.PrintWriter#print(java.lang.String)
	 */
	public void print(String s) {
		inner.print(s);
	}
	/**
	 * @param l
	 * @param format
	 * @param args
	 * @return
	 * @see java.io.PrintWriter#printf(java.util.Locale, java.lang.String, java.lang.Object[])
	 */
	public PrintWriter printf(Locale l, String format, Object... args) {
		return inner.printf(l, format, args);
	}
	/**
	 * @param format
	 * @param args
	 * @return
	 * @see java.io.PrintWriter#printf(java.lang.String, java.lang.Object[])
	 */
	public PrintWriter printf(String format, Object... args) {
		return inner.printf(format, args);
	}
	/**
	 * 
	 * @see java.io.PrintWriter#println()
	 */
	public void println() {
		inner.println();
	}
	/**
	 * @param x
	 * @see java.io.PrintWriter#println(boolean)
	 */
	public void println(boolean x) {
		inner.println(x);
	}
	/**
	 * @param x
	 * @see java.io.PrintWriter#println(char)
	 */
	public void println(char x) {
		inner.println(x);
	}
	/**
	 * @param x
	 * @see java.io.PrintWriter#println(char[])
	 */
	public void println(char[] x) {
		inner.println(x);
	}
	/**
	 * @param x
	 * @see java.io.PrintWriter#println(double)
	 */
	public void println(double x) {
		inner.println(x);
	}
	/**
	 * @param x
	 * @see java.io.PrintWriter#println(float)
	 */
	public void println(float x) {
		inner.println(x);
	}
	/**
	 * @param x
	 * @see java.io.PrintWriter#println(int)
	 */
	public void println(int x) {
		inner.println(x);
	}
	/**
	 * @param x
	 * @see java.io.PrintWriter#println(long)
	 */
	public void println(long x) {
		inner.println(x);
	}
	/**
	 * @param x
	 * @see java.io.PrintWriter#println(java.lang.Object)
	 */
	public void println(Object x) {
		inner.println(x);
	}
	/**
	 * @param x
	 * @see java.io.PrintWriter#println(java.lang.String)
	 */
	public void println(String x) {
		inner.println(x);
	}
	/**
	 * @return
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return inner.toString();
	}
	/**
	 * @param buf
	 * @param off
	 * @param len
	 * @see java.io.PrintWriter#write(char[], int, int)
	 */
	public void write(char[] buf, int off, int len) {
		//String s = new String(buf, off, len);
		//log.info("Writing a char buff[" + buf.length + "/" + off + "/" + len + "]\n==========\n" + s + "\n==========");		
		inner.write(buf, off, len);
		//inner.flush();
	}
	/**
	 * @param buf
	 * @see java.io.PrintWriter#write(char[])
	 */
	public void write(char[] buf) {
		inner.write(buf);
	}
	/**
	 * @param c
	 * @see java.io.PrintWriter#write(int)
	 */
	public void write(int c) {
		inner.write(c);
	}
	/**
	 * @param s
	 * @param off
	 * @param len
	 * @see java.io.PrintWriter#write(java.lang.String, int, int)
	 */
	public void write(String s, int off, int len) {
		inner.write(s, off, len);
	}
	/**
	 * @param s
	 * @see java.io.PrintWriter#write(java.lang.String)
	 */
	public void write(String s) {
		inner.write(s);
	}


}
