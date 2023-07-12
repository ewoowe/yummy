package com.github.ewoowe;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@SuppressWarnings("DuplicatedCode")
public class Yummy {

    /**
     * set yaml file to value by path
     *
     * @param yamlPath yaml file path
     * @param paths path list of value
     * @param values value list
     * @param autoCreate auto creat variable if path not exist
     * @throws YummyException throw when process can not be continuing
     */
    @SuppressWarnings("unchecked")
    public static void set(String yamlPath, String[] paths, Object[] values, boolean autoCreate) throws YummyException {
        if (yamlPath == null || paths == null || paths.length == 0 || values == null || values.length == 0)
            throw new YummyException(YummyExceptionEnum.ILLEGAL_ARGUMENTS, "cant be null or empty");
        if (paths.length != values.length)
            throw new YummyException(YummyExceptionEnum.ILLEGAL_ARGUMENTS, "path.length not equal to value.length");
        List<Node> roots = parseNodes(paths, values, autoCreate);
        if (!roots.isEmpty()) {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            FileInputStream fileInputStream = null;
            FileWriter fileWriter = null;
            try {
                File file = new File(yamlPath);
                fileInputStream = new FileInputStream(file);
                Map<String, Object> map = (Map<String, Object>) yaml.loadAs(fileInputStream, LinkedHashMap.class);
                setMapValue(map, roots);
                fileWriter = new FileWriter(file);
                yaml.dump(map, fileWriter);
            } catch (IOException ioException) {
                throw new YummyException(YummyExceptionEnum.YAML_FILE_ERROR, ioException.getMessage());
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

    private static void ensureList(List<Object> list, int size) {
        if (list.size() < size) {
            for (int i = list.size(); i < size; i++) {
                list.add(null);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static Object setMapValue(String pname, Object parent, List<Node> children) throws YummyException {
        if (children == null || children.isEmpty())
            return parent;
        if (parent == null) {
            parent = new LinkedHashMap<String, Object>();
            for (Node node : children) {
                if (!node.isAutoCreate())
                    throw new YummyException(YummyExceptionEnum.KEY_NOT_EXIST, pname + "." + node.getName());
                if (node.isArray()) {
                    List<Object> list = new ArrayList<>();
                    for (Map.Entry<Integer, List<Node>> entry : node.getArrayChildren().entrySet())
                        ensureList(list, entry.getKey() + 1);
                    ((Map<String, Object>) parent).put(node.getName(), list);
                    if (node.getFunctions() != null) {
                        for (Map.Entry<Integer, Function<Object, Object>> entry : node.getFunctions().entrySet()) {
                            if (entry.getValue() != null)
                                list.set(entry.getKey(), entry.getValue().apply(list.get(entry.getKey())));
                        }
                    }
                    Map<Integer, List<Node>> arrayChildren = node.getArrayChildren();
                    if (arrayChildren != null) {
                        for (Map.Entry<Integer, List<Node>> entry : arrayChildren.entrySet()) {
                            Object o1 = list.get(entry.getKey());
                            list.set(entry.getKey(), setMapValue(node.getName() + "[" + entry.getKey() + "]",
                                    o1, entry.getValue()));
                        }
                    }
                } else {
                    ((Map<String, Object>) parent).put(node.getName(), new LinkedHashMap<String, Object>());
                    if (node.getFunction() != null)
                        ((Map<String, Object>) parent).put(node.getName(), node.getFunction()
                                .apply(((Map<String, Object>) parent).get(node.getName())));
                    if (node.getChildren() != null)
                        ((Map<String, Object>) parent).put(node.getName(), setMapValue(node.getName(),
                                ((Map<String, Object>) parent).get(node.getName()), node.getChildren()));
                }
            }
            return parent;
        } else {
            if (parent instanceof Map) {
                for (Node node : children) {
                    if (((Map<String, Object>) parent).containsKey(node.getName())) {
                        Object o = ((Map<String, Object>) parent).get(node.getName());
                        if (node.isArray()) {
                            if (!(o instanceof List))
                                throw new YummyException(YummyExceptionEnum.KEY_NOT_ARRAY, pname + "." + node.getName());
                            List<Object> ol = (List<Object>) o;
                            if (node.getFunctions() != null) {
                                for (Map.Entry<Integer, Function<Object, Object>> entry : node.getFunctions().entrySet()) {
                                    try {
                                        Object o1 = ol.get(entry.getKey());
                                        if (entry.getValue() != null)
                                            ol.set(entry.getKey(), entry.getValue().apply(o1));
                                    } catch (IndexOutOfBoundsException ignore) {
                                        if (node.isAutoCreate()) {
                                            ensureList(ol, entry.getKey() + 1);
                                            ol.set(entry.getKey(), entry.getValue().apply(null));
                                        } else {
                                            throw new YummyException(YummyExceptionEnum.KEY_NOT_EXIST,
                                                    pname + "." + node.getName() + "[" + entry.getKey()
                                                            + "]" + " out of index");
                                        }
                                    }
                                }
                            }
                            Map<Integer, List<Node>> arrayChildren = node.getArrayChildren();
                            if (arrayChildren != null) {
                                for (Map.Entry<Integer, List<Node>> entry : arrayChildren.entrySet()) {
                                    try {
                                        Object o1 = ol.get(entry.getKey());
                                        ol.set(entry.getKey(), setMapValue(pname + "." + node.getName()
                                                + "[" + entry.getKey() + "]", o1, entry.getValue()));
                                    } catch (IndexOutOfBoundsException ignore) {
                                        if (node.isAutoCreate()) {
                                            ensureList(ol, entry.getKey() + 1);
                                            ol.set(entry.getKey(), setMapValue(pname + "." + node.getName()
                                                    + "[" + entry.getKey() + "]", ol.get(entry.getKey()), entry.getValue()));
                                        } else {
                                            throw new YummyException(YummyExceptionEnum.KEY_NOT_EXIST,
                                                    pname + "." + node.getName() + "[" + entry.getKey()
                                                            + "]" + " out of index");
                                        }
                                    }
                                }
                            }
                        } else {
                            if (o instanceof List)
                                throw new YummyException(YummyExceptionEnum.KEY_IS_ARRAY, pname + "." + node.getName());
                            if (node.getFunction() != null)
                                ((Map<String, Object>) parent).put(node.getName(), node.getFunction().apply(o));
                            List<Node> children2 = node.getChildren();
                            if (children2 != null)
                                ((Map<String, Object>) parent).put(node.getName(), setMapValue(node.getName(),
                                        ((Map<String, Object>) parent).get(node.getName()), children2));
                        }
                    } else {
                        if (!node.isAutoCreate())
                            throw new YummyException(YummyExceptionEnum.KEY_NOT_EXIST, pname + "." + node.getName());
                        if (node.isArray()) {
                            List<Object> list = new ArrayList<>();
                            for (Map.Entry<Integer, List<Node>> entry : node.getArrayChildren().entrySet())
                                ensureList(list, entry.getKey() + 1);
                            ((Map<String, Object>) parent).put(node.getName(), list);
                            if (node.getFunctions() != null) {
                                for (Map.Entry<Integer, Function<Object, Object>> entry : node.getFunctions().entrySet()) {
                                    if (entry.getValue() != null)
                                        list.set(entry.getKey(), entry.getValue().apply(list.get(entry.getKey())));
                                }
                            }
                            Map<Integer, List<Node>> arrayChildren = node.getArrayChildren();
                            if (arrayChildren != null) {
                                for (Map.Entry<Integer, List<Node>> entry : arrayChildren.entrySet()) {
                                    Object o1 = list.get(entry.getKey());
                                    list.set(entry.getKey(), setMapValue(pname + "." + node.getName()
                                            + "[" + entry.getKey() + "]", o1, entry.getValue()));
                                }
                            }
                        } else {
                            ((Map<String, Object>) parent).put(node.getName(), new LinkedHashMap<String, Object>());
                            if (node.getFunction() != null)
                                ((Map<String, Object>) parent).put(node.getName(), node.getFunction()
                                        .apply(((Map<String, Object>) parent).get(node.getName())));
                            if (node.getChildren() != null)
                                ((Map<String, Object>) parent).put(node.getName(),
                                        setMapValue(pname + "." + node.getName(),
                                        ((Map<String, Object>) parent).get(node.getName()), node.getChildren()));
                        }
                    }
                }
            } else {
                throw new YummyException(YummyExceptionEnum.KEY_MUST_MAP, pname);
            }
            return parent;
        }
    }

    @SuppressWarnings("unchecked")
    private static void setMapValue(Map<String, Object> map, List<Node> roots) throws YummyException {
        for (Node node : roots) {
            if (map.containsKey(node.getName())) {
                Object o = map.get(node.getName());
                if (node.isArray()) {
                    if (!(o instanceof List))
                        throw new YummyException(YummyExceptionEnum.KEY_NOT_ARRAY, node.getName());
                    List<Object> ol = (List<Object>) o;
                    if (node.getFunctions() != null) {
                        for (Map.Entry<Integer, Function<Object, Object>> entry : node.getFunctions().entrySet()) {
                            try {
                                Object o1 = ol.get(entry.getKey());
                                if (entry.getValue() != null)
                                    ol.set(entry.getKey(), entry.getValue().apply(o1));
                            } catch (IndexOutOfBoundsException ignore) {
                                if (node.isAutoCreate()) {
                                    ensureList(ol, entry.getKey() + 1);
                                    ol.set(entry.getKey(), entry.getValue().apply(null));
                                } else {
                                    throw new YummyException(YummyExceptionEnum.KEY_NOT_EXIST,
                                            node.getName() + "[" + entry.getKey() + "]" + " out of index");
                                }
                            }
                        }
                    }
                    Map<Integer, List<Node>> arrayChildren = node.getArrayChildren();
                    if (arrayChildren != null) {
                        for (Map.Entry<Integer, List<Node>> entry : arrayChildren.entrySet()) {
                            try {
                                Object o1 = ol.get(entry.getKey());
                                ol.set(entry.getKey(), setMapValue(node.getName() + "[" + entry.getKey() + "]",
                                        o1, entry.getValue()));
                            } catch (IndexOutOfBoundsException ignore) {
                                if (node.isAutoCreate()) {
                                    ensureList(ol, entry.getKey() + 1);
                                    ol.set(entry.getKey(), setMapValue(node.getName() + "[" + entry.getKey() + "]",
                                            ol.get(entry.getKey()), entry.getValue()));
                                } else {
                                    throw new YummyException(YummyExceptionEnum.KEY_NOT_EXIST,
                                            node.getName() + "[" + entry.getKey() + "]" + " out of index");
                                }
                            }
                        }
                    }
                } else {
                    if (o instanceof List)
                        throw new YummyException(YummyExceptionEnum.KEY_IS_ARRAY, node.getName());
                    if (node.getFunction() != null)
                        map.put(node.getName(), node.getFunction().apply(o));
                    List<Node> children = node.getChildren();
                    if (children != null)
                        map.put(node.getName(), setMapValue(node.getName(), map.get(node.getName()), children));
                }
            } else {
                if (!node.isAutoCreate())
                    throw new YummyException(YummyExceptionEnum.KEY_NOT_EXIST, node.getName());
                if (node.isArray()) {
                    List<Object> list = new ArrayList<>();
                    for (Map.Entry<Integer, List<Node>> entry : node.getArrayChildren().entrySet())
                        ensureList(list, entry.getKey() + 1);
                    map.put(node.getName(), list);
                    if (node.getFunctions() != null) {
                        for (Map.Entry<Integer, Function<Object, Object>> entry : node.getFunctions().entrySet()) {
                            if (entry.getValue() != null)
                                list.set(entry.getKey(), entry.getValue().apply(list.get(entry.getKey())));
                        }
                    }
                    Map<Integer, List<Node>> arrayChildren = node.getArrayChildren();
                    if (arrayChildren != null) {
                        for (Map.Entry<Integer, List<Node>> entry : arrayChildren.entrySet()) {
                            Object o1 = list.get(entry.getKey());
                            list.set(entry.getKey(), setMapValue(node.getName() + "[" + entry.getKey() + "]",
                                    o1, entry.getValue()));
                        }
                    }
                } else {
                    map.put(node.getName(), new LinkedHashMap<String, Object>());
                    if (node.getFunction() != null)
                        map.put(node.getName(), node.getFunction().apply(map.get(node.getName())));
                    if (node.getChildren() != null)
                        map.put(node.getName(), setMapValue(node.getName(), map.get(node.getName()), node.getChildren()));
                }
            }
        }
    }

    /**
     * get real name from original name
     *
     * @param name original name
     * @return real name
     */
    private static String getRealName(String name) {
        int i = name.indexOf("[");
        return i == -1 ? name : name.substring(0, i);
    }

    /**
     * if this node is an array node, get all array index
     *
     * @param name original name
     * @param indexStart start index
     * @return array index, if not an array node, return null
     * @throws YummyException YummyException
     */
    private static List<Integer> getArrayNode(String name, int indexStart) throws YummyException {
        int i = name.indexOf("[", indexStart);
        int j = name.indexOf("]", indexStart);
        if (i > j)
            throw new YummyException(YummyExceptionEnum.ILLEGAL_ARGUMENTS, name + " invalid");
        if (i != -1) {
            List<Integer> list = new ArrayList<>();
            String substring = name.substring(i + 1, j);
            try {
                String[] split = substring.split("-");
                if (split.length != 1 && split.length != 2)
                    throw new YummyException(YummyExceptionEnum.ILLEGAL_ARGUMENTS, name + " array index invalid");
                if (split.length == 1) {
                    int index = Integer.parseInt(substring);
                    list.add(index);
                } else {
                    int i1 = Integer.parseInt(split[0]);
                    int i2 = Integer.parseInt(split[1]);
                    if (i1 >= i2)
                        throw new YummyException(YummyExceptionEnum.ILLEGAL_ARGUMENTS, name + " invalid");
                    for (int k = i1; k <= i2; k++)
                        list.add(k);
                }
                List<Integer> arrayNode = getArrayNode(name, j + 1);
                if (arrayNode != null)
                    list.addAll(arrayNode);
                list.sort(Comparator.naturalOrder());
            } catch (NumberFormatException e) {
                throw new YummyException(YummyExceptionEnum.ILLEGAL_ARGUMENTS, name + " array index invalid");
            }
            return list;
        }
        return null;
    }

    /**
     * parse all paths and values to node list, for using.
     *
     * @param paths paths
     * @param values values
     * @param autoCreate whether auto create
     * @return node list
     * @throws YummyException YummyException
     */
    private static List<Node> parseNodes(String[] paths, Object[] values, boolean autoCreate) throws YummyException {
        List<Node> roots = new ArrayList<>();
        Map<String, Node> maps = new HashMap<>();
        for (int i = 0; i < paths.length; i++) {
            String namePath = paths[i];
            String[] names = namePath.split("\\.");
            String realName = getRealName(names[0]);
            List<Integer> arrayNode = getArrayNode(names[0], 0);
            Node node;
            if (maps.containsKey(realName)) {
                node = maps.get(realName);
            } else {
                node = new Node();
                roots.add(node);
                maps.put(realName, node);
                node.setAutoCreate(autoCreate);
                node.setName(realName);
            }
            if (arrayNode != null) {
                node.setArray(true);
                for (Integer index : arrayNode)
                    parseNodes2(node, index, 1, names, values[i], autoCreate);
            } else {
                parseNodes2(node, -1, 1, names, values[i], autoCreate);
            }
        }
        return roots;
    }

    @SuppressWarnings("unchecked")
    private static void parseNodes2(Node node, int arrayIndex, int nameIndex, String[] names,
                                    Object value, boolean autoCreated) throws YummyException {
        if (nameIndex == names.length) {
            if (arrayIndex != -1) {
                if (value instanceof Function) {
                    node.addFunction(arrayIndex, (Function<Object, Object>) value);
                } else {
                    node.addFunction(arrayIndex, o -> value);
                }
            } else {
                if (value instanceof Function) {
                    node.setFunction((Function<Object, Object>) value);
                } else {
                    node.setFunction(o -> value);
                }
            }
        } else {
            String name = names[nameIndex];
            String realName = getRealName(name);
            List<Integer> arrayNode = getArrayNode(name, 0);
            if (arrayIndex != -1) {
                Node arrayChild = node.getArrayChild(arrayIndex, realName);
                if (arrayChild == null) {
                    arrayChild = new Node();
                    arrayChild.setAutoCreate(autoCreated);
                    arrayChild.setName(realName);
                    node.addArrayChildNode(arrayIndex, arrayChild);
                }
                if (arrayNode != null) {
                    arrayChild.setArray(true);
                    for (Integer index : arrayNode)
                        parseNodes2(arrayChild, index, nameIndex + 1, names, value, autoCreated);
                } else {
                    parseNodes2(arrayChild, -1, nameIndex + 1, names, value, autoCreated);
                }
            } else {
                Node child = node.getChild(realName);
                if (child == null) {
                    child = new Node();
                    child.setAutoCreate(autoCreated);
                    child.setName(realName);
                    node.addChildNode(child);
                }
                if (arrayNode != null) {
                    child.setArray(true);
                    for (Integer index : arrayNode)
                        parseNodes2(child, index, nameIndex + 1, names, value, autoCreated);
                } else {
                    parseNodes2(child, -1, nameIndex + 1, names, value, autoCreated);
                }
            }
        }
    }
}
