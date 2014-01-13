/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freedesktop.udisks2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author aploese
 */
public class TreeNode {

    public TreeNode getParent() {
        return parent;
    }

    public Iterable<TreeNode> getChildren() {
        return children;
    }

    public Iterable<DBusInterface> getInterfaces() {
        return interfaces;
    }

    private TreeNode parent;
    private List<TreeNode> children;
    private List<DBusInterface> interfaces;
    private String name;

    TreeNode(String name) {
        this.name = name;
    }

    TreeNode getChildNode(String name) {
        if (children == null) {
            return null;
        } else {
            for (TreeNode node : children) {
                if (node.name.equals(name)) {
                    return node;
                }
            }
            return null;
        }
    }

    public boolean addChildNode(TreeNode node) {
        if (children == null) {
            children = new ArrayList<>();
        }
        final boolean result = children.add(node);
        node.parent = this;
        return result;
    }

    public boolean addInterface(DBusInterface i) {
        if (interfaces == null) {
            interfaces = new ArrayList<>();
        }
        return interfaces.add(i);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    String getPath() {
        StringBuilder sb = new StringBuilder();
        TreeNode node = this;
        while (node != null) {
            if (node.name == null || node.name.isEmpty()) {
            } else {
                sb.insert(0, node.name);
                sb.insert(0, '/');
            }
                node = node.parent;
        }
        return sb.toString();
    }

    public Collection<TreeNode> getNodesOfPath(String path) {
        String[] splittedPath = path.split("/");
        TreeNode node = this;
        for (int i = 1 ; i < splittedPath.length ; i++) {
            node = node.getChildNode(splittedPath[i]);
        }
        return node.children;
    }

    public <T extends DBusInterface> T getChildInterface(Class<T> iClass) {
        for (DBusInterface i : interfaces) {
            if (i.getClass().equals(iClass)) {
                return (T)i;
            }
        }
        throw new RuntimeException("InterfaceClass not found" + iClass.getName());
    }

}
