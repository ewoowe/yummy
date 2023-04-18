package com.github.ewoowe;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Node {
     /**
      * path node name
      */
     private String name;

     /**
      * create if this node not exist
      */
     private boolean autoCreate = false;

     /**
      * if node is an array node
      */
     private boolean array = false;

     /**
      * child node list of every array node
      */
     private Map<Integer, List<Node>> arrayChildren;

     /**
      * child node list
      */
     private List<Node> children;

     /**
      * function apply for this node
      */
     private Function<Object, Object> function;

     /**
      * functions apply for array node
      */
     private Map<Integer, Function<Object, Object>> functions;

     public String getName() {
          return name;
     }

     public void setName(String name) {
          this.name = name;
     }

     public boolean isAutoCreate() {
          return autoCreate;
     }

     public void setAutoCreate(boolean autoCreate) {
          this.autoCreate = autoCreate;
     }

     public Function<Object, Object> getFunction() {
          return function;
     }

     public void setFunction(Function<Object, Object> function) {
          this.function = function;
     }

     public void addFunction(Integer index, Function<Object, Object> function) {
          if (functions == null)
               functions = new HashMap<>();
          functions.put(index, function);
     }

     public Map<Integer, Function<Object, Object>> getFunctions() {
          return functions;
     }

     public boolean isArray() {
          return array;
     }

     public void setArray(boolean array) {
          this.array = array;
     }

     public Node getArrayChild(int index, String name) {
          if (arrayChildren != null && arrayChildren.containsKey(index)) {
               List<Node> nodes = arrayChildren.get(index);
               for (Node node : nodes) {
                    if (StringUtils.equals(name, node.getName()))
                         return node;
               }
          }
          return null;
     }

     public Node getChild(String name) {
          if (children != null) {
               for (Node node : children) {
                    if (StringUtils.equals(name, node.getName()))
                         return node;
               }
          }
          return null;
     }

     public void addArrayChildNode(int index, Node node) {
          if (arrayChildren == null)
               arrayChildren = new HashMap<>();
          if (arrayChildren.containsKey(index)) {
               arrayChildren.get(index).add(node);
          } else {
               ArrayList<Node> list = new ArrayList<>();
               list.add(node);
               arrayChildren.put(index, list);
          }
     }

     public void addChildNode(Node node) {
          if (children == null)
               children = new ArrayList<>();
          children.add(node);
     }

     public Map<Integer, List<Node>> getArrayChildren() {
          return arrayChildren;
     }

     public List<Node> getChildren() {
          return children;
     }
}
