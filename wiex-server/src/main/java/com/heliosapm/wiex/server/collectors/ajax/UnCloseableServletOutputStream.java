/**
 * 
 */
package com.heliosapm.wiex.server.collectors.ajax;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import org.apache.log4j.*;

/**
 * @author WhiteheN
 *
 */
public class UnCloseableServletOutputStream extends ServletOutputStream implements FlushableCloseable{
	
	//protected ServletOutputStream os = null;
	public static final int DEFAULT_BUFFER_SIZE = 8092;
	protected ByteArrayOutputStream baos = null; 
	protected Logger log = null;
	protected int bufferSize = DEFAULT_BUFFER_SIZE;
	
	protected static final String CR = System.getProperty("line.separator", "\n");
	
	public UnCloseableServletOutputStream(int bufferSize) {
		baos = new ByteArrayOutputStream(bufferSize);
		this.bufferSize = bufferSize;
		log = Logger.getLogger(getClass());
	}
	
	public UnCloseableServletOutputStream() {
		this(DEFAULT_BUFFER_SIZE);
	}

	/**
	 * @throws IOException
	 * @see java.io.ByteArrayOutputStream#close()
	 */
	public void close() throws IOException {
		log.info("Bypassing close() command");
	}

	/**
	 * @param obj
	 * @return
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return baos.equals(obj);
	}

	/**
	 * @throws IOException
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		baos.flush();
	}


	/**
	 * @param b
	 * @param off
	 * @param len
	 * @see java.io.ByteArrayOutputStream#write(byte[], int, int)
	 */
	public void write(byte[] b, int off, int len) {
		baos.write(b, off, len);
	}

	/**
	 * @param b
	 * @throws IOException
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] b) throws IOException {
		baos.write(b);
	}

	/**
	 * @param b
	 * @see java.io.ByteArrayOutputStream#write(int)
	 */
	public void write(int b) {
		baos.write(b);
	}



	/**
	 * @param arg0
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#print(boolean)
	 */
	public void print(boolean arg0) throws IOException {
		baos.write(("" + arg0).getBytes());
	}

	/**
	 * @param c
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#print(char)
	 */
	public void print(char c) throws IOException {
		baos.write(("" + c).getBytes());
	}

	/**
	 * @param d
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#print(double)
	 */
	public void print(double d) throws IOException {
		baos.write(("" + d).getBytes());
	}

	/**
	 * @param f
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#print(float)
	 */
	public void print(float f) throws IOException {
		baos.write(("" + f).getBytes());
	}

	/**
	 * @param i
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#print(int)
	 */
	public void print(int i) throws IOException {
		baos.write(("" + i).getBytes());
	}

	/**
	 * @param l
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#print(long)
	 */
	public void print(long l) throws IOException {
		baos.write(("" + l).getBytes());
	}

	/**
	 * @param arg0
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#print(java.lang.String)
	 */
	public void print(String arg0) throws IOException {
		baos.write((arg0).getBytes());
	}

	/**
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#println()
	 */
	public void println() throws IOException {
		baos.write((CR).getBytes());
	}

	/**
	 * @param b
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#println(boolean)
	 */
	public void println(boolean b) throws IOException {
		baos.write(("" + b + CR).getBytes());
	}

	/**
	 * @param c
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#println(char)
	 */
	public void println(char c) throws IOException {
		baos.write(("" + c + CR).getBytes());
	}

	/**
	 * @param d
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#println(double)
	 */
	public void println(double d) throws IOException {
		baos.write(("" + d + CR).getBytes());
	}

	/**
	 * @param f
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#println(float)
	 */
	public void println(float f) throws IOException {
		baos.write(("" + f + CR).getBytes());
	}

	/**
	 * @param i
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#println(int)
	 */
	public void println(int i) throws IOException {
		baos.write(("" + i + CR).getBytes());
	}

	/**
	 * @param l
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#println(long)
	 */
	public void println(long l) throws IOException {
		baos.write(("" + l + CR).getBytes());
	}

	/**
	 * @param s
	 * @throws IOException
	 * @see javax.servlet.ServletOutputStream#println(java.lang.String)
	 */
	public void println(String s) throws IOException {
		baos.write((s + CR).getBytes());
	}

	/**
	 * @return the bufferSize
	 */
	public int getBufferSize() {
		return bufferSize;
	}
	
	public ByteArrayOutputStream getBuffer() {
		return baos;
	}
	
	public void setBuffer(ByteArrayOutputStream baos) {
		this.baos = baos;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		// TODO Auto-generated method stub
		
	}	



// ======================================================================================
// ======================================================================================

}
