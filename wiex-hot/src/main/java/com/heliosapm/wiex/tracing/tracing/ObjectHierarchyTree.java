package com.heliosapm.wiex.tracing.tracing;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Title: ObjectHierarchyTree</p>
 * <p>Description: A hierarchical object reference tree.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.5 $
 */

public class ObjectHierarchyTree<K> {
	
	protected Map<String, HierarchyNode<K>> hierarchy = new ConcurrentHashMap<String, HierarchyNode<K>>();
	
	/**	The name space delimeter */
	protected String delimeter = null;
	/** The name space segment wild card */
	protected String wildCard = null;
	/** The default value for sparse lookups */
	protected K defaultValue = null;
	/** The sparse value for incidental nodes */
	protected K sparseValue = null;
	/** Average Lookup Time */
	protected AtomicLong averageLookupTime = new AtomicLong(0L);
	/** one reading taken */
	protected boolean oneReading = false;
	
	
	
	
	/**
	 * Creates a new ObjectHierarchyTree
	 * @param delimeter The name space delimeter
	 * @param wildCard The name space segment wild card
	 * @param defaultValue The default value for sparse lookups
	 * @param sparseValue The value used for incidental nodes placed in the tree to complete the navigation to a progeny node.
	 */
	public ObjectHierarchyTree(String delimeter, String wildCard, K defaultValue, K sparseValue) {
		this.wildCard = wildCard;
		this.delimeter = delimeter;
		this.defaultValue = defaultValue;
		this.sparseValue = sparseValue;
	}
	
	/**
	 * Returns the number of nodes in the whole tree.
	 * @return The number of nodes in the tree.
	 */
	public int getTreeSize() {
		int tSize = 0;
		for(HierarchyNode<K> node: hierarchy.values()) {
			tSize++;
			tSize += getNodeSize(node);
		}
		
		return tSize;
	}
	
	/**
	 * Empties the hierarchy.
	 */
	public void clear() {
		hierarchy.clear();
	}
	
	/**
	 * Returns the number of progeny nodes in a node.
	 * @param node The node to get the progeny count for.
	 * @return The number of progeny nodes in a node.
	 */
	protected int getNodeSize(HierarchyNode<K> node) {
		int tSize = 0;
		for(HierarchyNode<K> cNode: node.getChildren().values()) {
			tSize++;
			tSize += getNodeSize(cNode);
		}
		
		return tSize;
		
	}
	
	
	/**
	 * Locates the name space node and sets the value.
	 * If the node does not exist, it is created.
	 * @param name The name space.
	 * @param value The value to set the node to.
	 */
	public void setMember(String name, K value) {
		String[] segments  = null;
		if(!name.contains(delimeter.replace("\\", ""))) {
			segments = new String[]{name};
		} else {
			segments = name.split(delimeter);
		}		
		HierarchyNode<K> node = null;
		Map<String, HierarchyNode<K>> currentMap = hierarchy;		 
		for(String s: segments) {			
			node = currentMap.get(s);
			if(node==null) {
				node = new HierarchyNode<K>(sparseValue);
				currentMap.put(s, node);				
			}	
			currentMap = node.getChildren();
		}
		node.setMember(value);
	}
	

	
	/**
	 * Gets the value from the tree navigated to by the namespace.
	 * If the node does not exist, the default value is returned.
	 * @param name
	 * @return The located node's value or null.
	 */
	public K getValue(String name) {
		long start = System.currentTimeMillis();
		String[] segments = name.split(delimeter);
		HierarchyNode<K> node = null;
		HierarchyNode<K> node2 = null;
		Map<String, HierarchyNode<K>> currentMap = hierarchy;
		
		for(String s: segments) {
			node = lookup(s, currentMap);
			if(node==null) {
				if(node2!=null) {
					return node2.getMember();
				} else {
					return defaultValue;
				}
			}
			else {
				currentMap = node.getChildren();
				node2 = node;
			}
		}
		long elapsed = System.currentTimeMillis()-start;
		if(!oneReading) {
			oneReading = true;
			averageLookupTime.set(elapsed);
		} else {
			long currentAvg = averageLookupTime.get();
			if((currentAvg + elapsed)==0L) {
				averageLookupTime.set(0L);
			} else {
				averageLookupTime.set((currentAvg + elapsed)/2);
			}									
		}
		return node.getMember();
	}
	
	/**
	 * Returns the node navigated to by the name space passed.
	 * @param name The name space to retrieve the node from.
	 * @return The node found or null if it does not exist.
	 */
	public HierarchyNode<K> getNode(String name) {
		
		String[] segments = name.split(delimeter);
		HierarchyNode<K> node = null;		
		Map<String, HierarchyNode<K>> currentMap = hierarchy;
		
		for(String s: segments) {
			node = lookup(s, currentMap);
			if(node==null) {
				break;
			}
			else {
				currentMap = node.getChildren();				
			}
		}
		return node;
	}
	
	
	/**
	 * Looks up a child node in a parent node.
	 * If the exact match is not found, a wildcard is looked up.
	 * If neither the exact match or wildcard is fund, returns null.
	 * @param key The name space segment.
	 * @param map The map of nodes to search in.
	 * @return A located node or null.
	 */
	protected HierarchyNode<K> lookup(String key, Map<String, HierarchyNode<K>> map) {
		HierarchyNode<K> node = map.get(key);
		if(node != null) return node;
		node = map.get(wildCard);
		return node;
	}
	
	/**
	 * Generates a string representation of the sparse object tree.
	 * @return A map of the object tree.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder("Hierarchy\n");
		int depth = 0;
		Set<Entry<String, HierarchyNode<K>>>  children = hierarchy.entrySet();
		for(Entry<String, HierarchyNode<K>> entry: children) {
			printNode(entry.getValue(), entry.getKey(), buff, depth);
		}		
		return buff.toString();
	}
	
	/**
	 * Returns a string representation of one node.
	 * @param nodeName The name space of the node to render.
	 * @return A map of the object tree from the located node down.
	 */
	public String printDetails(String nodeName) {
		StringBuilder buff = new StringBuilder("Node\n");
		HierarchyNode<K> node = getNode(nodeName);
		if(node==null) {
			buff.append("Empty");
		} else {
			int depth = 0;
			Set<Entry<String, HierarchyNode<K>>>  children = node.getChildren().entrySet();
			for(Entry<String, HierarchyNode<K>> entry: children) {
				printNode(entry.getValue(), entry.getKey(), buff, depth);
			}					
		}
		return buff.toString();
		
	}
	
	/**
	 * Returns the average lookup time for the hierarchy tree in milliseconds.
	 * @return The average lookup time.
	 */
	public long getAverageLookupTime() {
		return averageLookupTime.get();
	}
	
	/**
	 * Generates a string representation of a node.
	 * @param node The node to render.
	 * @param nodeKey The key of the node.
	 * @param buff The buffer to append to append the rendered text into.
	 * @param depth The current depth of the node.
	 */
	protected void printNode(HierarchyNode<K> node, String nodeKey, StringBuilder buff, int depth) {
		buff.append(indent(depth));
		buff.append(nodeKey);
		buff.append(":");
		buff.append(node.getMember());
		buff.append("\n");
		Set<Entry<String, HierarchyNode<K>>>  children = node.getChildren().entrySet();
		depth++;
		for(Entry<String, HierarchyNode<K>> entry: children) {
			String name = entry.getKey();
			HierarchyNode<K> entryNode = entry.getValue();
			printNode(entryNode, name, buff, depth);
		}
		depth--;
	}
	
	/**
	 * Generates an indentation for formating the node rendering.
	 * @param i The depth of the indentation.
	 * @return A string of the correct width and content to generate the indentation.
	 */
	protected String indent(int i) {
		StringBuilder buff = new StringBuilder();
		for(int x = 0; x < i; x++) {
			buff.append("\t");
		}
		return buff.toString();
	}
	

	/**
	 * A static test of the tree.
	 * @param args
	 */
	public static void main(String[] args) {
		ObjectHierarchyTree<Boolean> hier = new ObjectHierarchyTree<Boolean>("\\.", "*", Boolean.TRUE, Boolean.FALSE);
		hier.setMember("A", Boolean.TRUE);
		hier.setMember("A.B", Boolean.FALSE);
		hier.setMember("A.B.C", Boolean.TRUE);
		hier.setMember("X.*.Z", Boolean.FALSE);
		
		hier.setMember("L.*.M", Boolean.FALSE);
		hier.setMember("L.O.M", Boolean.TRUE);
		
		log("TreeSize:" + hier.getTreeSize());
		
		log(hier.toString());
		log("============");
		log("A:" + hier.getValue("A"));
		log("A.B:" + hier.getValue("A.B"));
		log("A.B:" + hier.getValue("A.B"));
		log("A.B.C:" + hier.getValue("A.B.C"));
		log("A.B.D:" + hier.getValue("A.B.D"));
		log("A.B.D.A.B.D.A.B.D.A.B.D.A.B.D:" + hier.getValue("A.B.D.A.B.D.A.B.D.A.B.D.A.B.D"));
		log("A.B.C.D:" + hier.getValue("A.B.C.D"));
		log("D:" + hier.getValue("D"));
		log("X:" + hier.getValue("X"));
		log("X.Y:" + hier.getValue("X.Y"));
		log("X.Y.Z:" + hier.getValue("X.Y.Z"));
		log("L.A.M:" + hier.getValue("L.A.M"));
		log("L.A.M.A:" + hier.getValue("L.A.M.A"));
		log("L.O.M:" + hier.getValue("L.O.M"));
		log("L.O.M.O:" + hier.getValue("L.O.M.O"));
		
		log("==========================");
		
		log(hier.printDetails("A"));
		
	}
		
	
	static boolean p = true;
	
	public static void log(Object message) {
		if(p)System.out.println(message);
	}

}


