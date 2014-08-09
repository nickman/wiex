/**
 * 
 */
package com.heliosapm.wiex.server.tracing.collectors.ajax;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.*;

/**
 * @author WhiteheN
 *
 */
public class UnCommitableServletResponse implements HttpServletResponse {
	
	
	
	protected UnCloseableServletOutputStream ucos = null;
	protected Logger log = null;
	protected FlushableCloseable fc = null;
	protected Map<String, Integer> intHeaders = new HashMap<String, Integer>();
	protected Map<String, String> headers = new HashMap<String, String>();
	protected Map<String, Long> dateHeaders = new HashMap<String, Long>();
	protected List<Cookie> cookies = new ArrayList<Cookie>();
	protected HttpServletResponse innerResponse = null;
	protected int status = -1;
	protected int error = -1;
	protected String errorMessage = null;
	protected String redirect = null;
	protected UnCloseablePrintWriter ucpw = null;
	protected int contLength = 0;
	/**
	 * @param response
	 * @throws IOException 
	 */
	public UnCommitableServletResponse(int bufferSize, HttpServletResponse innerResponse) throws IOException {
		ucos = new UnCloseableServletOutputStream(bufferSize);
		this.innerResponse = innerResponse;
		
		log = Logger.getLogger(getClass());
		
	}
	
	public UnCommitableServletResponse(HttpServletResponse innerResponse) throws IOException {
		this(UnCloseableServletOutputStream.DEFAULT_BUFFER_SIZE, innerResponse);		
	}
	
	public void process() throws IOException {
		
		for(Entry<String, Integer> entry: intHeaders.entrySet()) {
			innerResponse.setIntHeader(entry.getKey(), entry.getValue());
		}
		for(Entry<String, Long> entry: dateHeaders.entrySet()) {
			innerResponse.setDateHeader(entry.getKey(), entry.getValue());
		}
		for(Entry<String, String> entry: headers.entrySet()) {
			innerResponse.setHeader(entry.getKey(), entry.getValue());
		}
		for(Cookie cookie: cookies) {
			innerResponse.addCookie(cookie);
		}
		OutputStream os = innerResponse.getOutputStream();
		
		BufferedOutputStream bos = new BufferedOutputStream(os);
		if(ucpw!=null) {
			ucpw.flush();
		}
		ucos.flush();
		byte[] content = ucos.getBuffer().toByteArray();		
		contLength = content.length;
		log.info("Content Buffered:" + contLength);
		bos.write(content);
		if(status!=-1) innerResponse.setStatus(status);
		if(error!=-1) {
			if(errorMessage!=null) innerResponse.sendError(error, errorMessage);
			else innerResponse.sendError(error);
		}
		 bos.flush();
		 os.flush();
		 if(redirect!=null) innerResponse.sendRedirect(redirect);
		 else {
			 try { bos.close(); } catch (Exception e) { log.warn("Exception Occured Flushing bos", e); }
			 try { os.close(); } catch (Exception e) { log.warn("Exception Occured Flushing os", e); }
		 }	
		 
	}
	
	
	public void flushBuffer() {
		log.info("Void Flush");
	}
	
	public boolean isCommitted() {
		return false;
	}
	
	public ServletOutputStream getOutputStream()  throws IOException {
		log.info("Issuing UnCloseableServletOutputStream");		
		return ucos;
	}
	
	public PrintWriter getWriter() throws IOException {
		log.info("Issuing UnCloseablePrintWriter");
		ucpw = new UnCloseablePrintWriter(ucos);
		return ucpw;
	}


	/**
	 * @return the fc
	 */
	public FlushableCloseable getFc() {
		return fc;
	}

	/**
	 * @param arg0
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	public void addCookie(Cookie arg0) {
		cookies.add(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String arg0, long arg1) {
		log.info("Header Change:[addDateHeader/" + arg0 + "/" + arg1 + "]");
		dateHeaders.put(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String arg0, String arg1) {
		log.info("Header Change:[addHeader/" + arg0 + "/" + arg1 + "]");
		headers.put(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String arg0, int arg1) {
		log.info("Header Change:[addIntHeader/" + arg0 + "/" + arg1 + "]");
		intHeaders.put(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	public boolean containsHeader(String key) {
		log.info("Header Inquiry:[containsHeader/" + key + "]");
		return headers.containsKey(key) || intHeaders.containsKey(key) || dateHeaders.containsKey(key); 
	}

	/**
	 * @param arg0
	 * @return
	 * @deprecated
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		return java.net.URLEncoder.encode(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {
		return java.net.URLEncoder.encode(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @deprecated
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		return java.net.URLEncoder.encode(arg0);
	}

	/**
	 * @param arg0
	 * @return
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String arg0) {
		return java.net.URLEncoder.encode(arg0);
	}

	/**
	 * @return
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		return ucos.getBufferSize();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return innerResponse.getCharacterEncoding();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		return innerResponse.getContentType();
	}

	/**
	 * @return
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		return innerResponse.getLocale();
	}

	/**
	 * 
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		log.info("Reset Requested");
		ucos.getBuffer().reset();
		headers.clear();
		dateHeaders.clear();
		intHeaders.clear();
	}
	
	public void setBufferSize(int size) {
		if(size > ucos.getBufferSize()) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(size);
			ByteArrayOutputStream oldBaos = ucos.getBuffer();
			try {
				baos.write(oldBaos.toByteArray());
				ucos.setBuffer(baos);
			} catch (IOException e) {
				log.error("Failed to rewrite baos", e);
			}
			
		}
		//inner.setBufferSize(arg0);
	}

	/**
	 * @param arg0
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0) {
		innerResponse.setCharacterEncoding(arg0);
	}

	/**
	 * @param arg0
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int arg0) {
		//
	}

	/**
	 * @param arg0
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String arg0) {
		innerResponse.setContentType(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String arg0, long arg1) {
		log.info("Header Change:[setDateHeader/" + arg0 + "/" + arg1 + "]");
		dateHeaders.put(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String arg0, String arg1) {
		log.info("Header Change:[setHeader/" + arg0 + "/" + arg1 + "]");
		headers.put(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String arg0, int arg1) {
		log.info("Header Change:[setIntHeader/" + arg0 + "/" + arg1 + "]");
		intHeaders.put(arg0, arg1);
	}

	/**
	 * @param arg0
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale arg0) {
		innerResponse.setLocale(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @deprecated
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	public void setStatus(int arg0, String arg1) {
		status = arg0;
	}

	/**
	 * @param arg0
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int arg0) {
		status = arg0;
	}

	public void sendError(int arg0) throws IOException {
		error = arg0;
		
	}

	public void sendError(int arg0, String arg1) throws IOException {		
		error = arg0;
		errorMessage = arg1;		
		
	}

	public void sendRedirect(String arg0) throws IOException {
		redirect = arg0;
		
	}

	public void resetBuffer() {
		log.info("Buffer Reset Requested");
		ucos.getBuffer().reset();
	}

	/**
	 * @return the contLength
	 */
	protected int getContLength() {
		return contLength;
	}

	@Override
	public void setContentLengthLong(long len) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}

}
