package com.heliosapm.wiex.tracing.tracing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: HierarchyNode</p>
 * <p>Description: Represents a node in an ObjectHierarchyTree.</p> 
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organization: Helios Development Group</p>
 * @author Whitehead
 * @version $Revision: 1.1 $
 */

class HierarchyNode<K> {
	
	/**	 The value of the node */
	protected K member = null;
	/** A map of the node's children */
	protected Map<String, HierarchyNode<K>> children = new ConcurrentHashMap<String, HierarchyNode<K>>();
	
	/**
	 * Creates a new node.
	 * @param member The member value of the node.
	 */
	public HierarchyNode(K member) {
		this.member = member;
	}
	
	/**
	 * Returns the value of the node.
	 * @return The value of the node.
	 */
	public K getMember() {
		return member;
	}
	
	/**
	 * Sets the value of the node.
	 * @param value The value of the node.
	 */
	public void setMember(K value) {
		member = value;
	}
	
	/**
	 * Returns the children of the node.
	 * @return A map of the children.
	 */
	public Map<String, HierarchyNode<K>> getChildren() {
		return children;
	}
	
	/**
	 * Sets the children of the node.
	 * @param children A map of the children of the node.
	 */
	public void setChildren(Map<String, HierarchyNode<K>> children) {
		this.children = children;
	}
}