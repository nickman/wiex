package com.heliosapm.wiex.server.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static javax.xml.xpath.XPathConstants.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;


import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * <p>Title: XMLHelper</p>
 * <p>Description: Generic XML helper utilities</p> 
 * @version $Revision$
 */
public class XMLHelper {
	
	static {
		String xmlParser = System.getProperty("org.xml.sax.parser");
		if(xmlParser == null) {
			System.setProperty("org.xml.sax.parser", "com.sun.org.apache.xerces.internal.parsers.SAXParser");
		}		
	}
	

	  /**
	   * Searches throgh the passed NamedNodeMap for an attribute and returns it if it is found.
	   * If it is not found, returns a null.
	   * @param nnm NamedNodeMap
	   * @param name String
	   * @return String
	   */
	  public static String getAttributeValueByName(NamedNodeMap nnm, String name) {
	    for(int i = 0; i < nnm.getLength(); i++) {
	      Attr attr = (Attr)nnm.item(i);
	      if(attr.getName().equalsIgnoreCase(name)) {
	        return attr.getValue();
	      }
	    }
	    return null;
	  }
	  
	/**
	 * Returns the attribute value for the passed name in the passed node.
	 * @param node
	 * @param name
	 * @return The attribute value or null if it is not found.
	 */
	public static String getAttributeValueByName(Node node, String name) {
		  return getAttributeValueByName(node.getAttributes(), name);
	  }

	  /**
	   * Searches throgh the passed NamedNodeMap for an attribute. If it is found, it will try to convert it to a boolean.
	   * @param nnm NamedNodeMap
	   * @param name String
	   * @throws RuntimeException
	   * @return boolean
	   */
	  public static boolean getAttributeBooleanByName(NamedNodeMap nnm, String name) throws RuntimeException {
	    for(int i = 0; i < nnm.getLength(); i++) {
	      Attr attr = (Attr)nnm.item(i);
	      if(attr.getName().equalsIgnoreCase(name)) {
	        String tmp =  attr.getValue().toLowerCase();
	        if(tmp.equalsIgnoreCase("true")) return true;
	        if(tmp.equalsIgnoreCase("false")) return false;
	        throw new RuntimeException("Attribute " + name + " value not boolean:" + tmp);
	      }
	    }
	    throw new RuntimeException("Attribute " + name + " not found.");
	  }


	  /**
	   * Helper Method. Searches through the child nodes of an element and returns the first node with a matching name.
	   * @param element Element
	   * @param name String
	   * @param caseSensitive boolean
	   * @return Node
	   */
	  public static Node getChildNodeByName(Element element, String name, boolean caseSensitive) {
	    NodeList list = element.getChildNodes();
	    for(int i = 0; i < list.getLength(); i++) {
	      Node node = list.item(i);
	      if(caseSensitive) {
	        if(node.getNodeName().equals(name)) return node;
	      } else {
	        if(node.getNodeName().equalsIgnoreCase(name)) return node;
	      }
	    }
	    return null;
	  }


	  /**
	   * Helper Method. Searches through the child nodes of a node and returns the first node with a matching name.
	   * Do we need this ?
	   * @param element Element
	   * @param name String
	   * @param caseSensitive boolean
	   * @return Node
	   */

	  public static Node getChildNodeByName(Node element, String name, boolean caseSensitive) {
	    NodeList list = element.getChildNodes();
	    for(int i = 0; i < list.getLength(); i++) {
	      Node node = list.item(i);
	      if(caseSensitive) {
	        if(node.getNodeName().equals(name)) return node;
	      } else {
	        if(node.getNodeName().equalsIgnoreCase(name)) return node;
	      }
	    }
	    return null;
	  }




	  /**
	   * Helper Method. Searches through the child nodes of an element and returns an array of the matching nodes.
	   * @param element Element
	   * @param name String
	   * @param caseSensitive boolean
	   * @return ArrayList
	   */
	  public static List<Node> getChildNodesByName(Node element, String name, boolean caseSensitive) {
	    ArrayList<Node> nodes = new ArrayList<Node>();
	    NodeList list = element.getChildNodes();
	    for (int i = 0; i < list.getLength(); i++) {
	      Node node = list.item(i);
	      if (caseSensitive) {
	        if (node.getNodeName().equals(name)) nodes.add(node);
	      }
	      else {
	        if (node.getNodeName().equalsIgnoreCase(name)) nodes.add(node);
	      }
	    }
	    return nodes;
	  }
	  
	/**
	 * Parses an input source and generates an XML document.
	 * @param is An input source to an XML source.
	 * @return An XML doucument.
	 */
	public static Document parseXML(InputSource is) {
		  try {
			  Document doc = null;
			  DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			  doc = documentBuilder.parse(is);		  
		  return doc;
		  } catch (Exception e) {
			  throw new RuntimeException("Failed to parse XML source", e);
		  }
	}
	  
	  
	/**
	 * Parses an input stream and generates an XML document.
	 * @param is An input stream to an XML source.
	 * @return An XML doucument.
	 */
	public static Document parseXML(InputStream is) {
		return parseXML(new InputSource(is));
	}
	
	/**
	 * Parses a file and generates an XML document.
	 * @param file
	 * @return An XML doucument.
	 */
	public static Document parseXML(File file) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return parseXML(fis);
		} catch (Exception e) {
			throw new RuntimeException("Failed to open XML file:" + file, e);
		} finally {
			try { fis.close(); } catch (Exception e) {}
		}		
	}
	
	/**
	 * Parses an XML string and generates an XML document.
	 * @param xml
	 * @return An XML doucument.
	 */
	public static Document parseXML(String xml) {
		StringReader sr = new StringReader(xml);
		return parseXML(new InputSource(sr));
	}
	
	/**
	 * Uses the passed XPath expression to locate a set of nodes in the passed element.
	 * @param targetNode The node to search.
	 * @param expression The XPath expression.
	 * @return A list of located nodes.
	 */
	public static List<Node> xGetNodes(Node targetNode, String expression) {
		List<Node> nodes = new ArrayList<Node>();		
		XPath xpath = null;
		try {
			xpath = XPathFactory.newInstance().newXPath();
			XPathExpression xpathExpression = xpath.compile(expression);
			NodeList nodeList = (NodeList)xpathExpression.evaluate(targetNode, NODESET);
			if(nodeList!=null) {
				for(int i = 0; i < nodeList.getLength(); i++) {
					nodes.add(nodeList.item(i));
				}
			}
			return nodes;
		} catch (Exception e) {
			throw new RuntimeException("XPath:Failed to locate the nodes:" + expression, e);
		}		
	}
	
	/**
	 * Uses the passed XPath expression to locate a single node in the passed element.
	 * @param targetNode The node to search.
	 * @param expression The XPath expression.
	 * @return The located node or null if one is not found.
	 */
	public static Node xGetNode(Node targetNode, String expression) {
		Node node = null;		
		XPath xpath = null;
		try {
			xpath = XPathFactory.newInstance().newXPath();
			XPathExpression xpathExpression = xpath.compile(expression);
			node = (Node)xpathExpression.evaluate(targetNode, NODE);
			return node;
		} catch (Exception e) {
			throw new RuntimeException("XPath:Failed to locate the node:" + expression, e);
		}		
	}
	
	  
}

