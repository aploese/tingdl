/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freedesktop.udisks2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.freedesktop.udisks2.drive.Ata;

/**
 *
 * @author aploese
 */
public class Parser {

    private void setProperty() {
        nameBuilder.setCharAt(0, Character.toLowerCase(nameBuilder.charAt(0)));
        String fieldName = nameBuilder.toString();
        try {
            Field f = currentInterface.getClass().getDeclaredField(fieldName);
            final boolean accessible = f.isAccessible();
            try {
                f.setAccessible(true);

                if (f.getType().equals(String.class)) {
                    f.set(currentInterface, valueBuilder.toString());
                } else if (f.getType().equals(Boolean.class) || f.getType().equals(boolean.class)) {
                    f.set(currentInterface, Boolean.valueOf(valueBuilder.toString()));
                } else if (f.getType().equals(Integer.class) || f.getType().equals(int.class)) {
                    f.set(currentInterface, Integer.valueOf(valueBuilder.toString()));
                } else if (f.getType().equals(Long.class) || f.getType().equals(long.class)) {
                    f.set(currentInterface, Long.valueOf(valueBuilder.toString()));
                } else if (f.getType().equals(Double.class) || f.getType().equals(double.class)) {
                    f.set(currentInterface, Double.valueOf(valueBuilder.toString()));
                } else if (f.getType().equals(String[].class)) {
                    f.set(currentInterface, new String[]{valueBuilder.toString()});
                } else {
                    LOG.log(Level.SEVERE, "Internal error cant handle datatype \"{0}\"", f.getType());
                    throw new RuntimeException("Cant handle type: " + f.getType());
                }

            } finally {
                f.setAccessible(accessible);
            }
        } catch (NoSuchFieldException | SecurityException e) {
            LogRecord lr = new LogRecord(Level.SEVERE, "No field for value: \"{0}\" of \"{1}\"");
            lr.setParameters(new Object[]{nameBuilder.toString(), currentInterface});
            lr.setThrown(e);
            LOG.log(lr);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            LogRecord lr = new LogRecord(Level.SEVERE, "cant set value: \"{0}\" of \"{1}\"");
            lr.setParameters(new Object[]{nameBuilder.toString(), currentInterface});
            lr.setThrown(e);
            LOG.log(lr);
            throw new RuntimeException(e);
        }
    }

    private void addToProperty() {
        nameBuilder.setCharAt(0, Character.toLowerCase(nameBuilder.charAt(0)));
        String fieldName = nameBuilder.toString();
        try {
            Field f = currentInterface.getClass().getDeclaredField(fieldName);
            final boolean accessible = f.isAccessible();
            try {
                f.setAccessible(true);

                if (f.getType().equals(String[].class)) {
                    String[] value = (String[]) f.get(currentInterface);
                    value = Arrays.copyOf(value, value.length + 1);
                    value[value.length - 1] = valueBuilder.toString();
                    f.set(currentInterface, value);
                } else {
                    throw new RuntimeException();
                }

            } finally {
                f.setAccessible(accessible);
            }
        } catch (NoSuchFieldException | SecurityException e) {
            LogRecord lr = new LogRecord(Level.SEVERE, "No field for value: \"{0}\" of \"{1}\"");
            lr.setParameters(new Object[]{nameBuilder.toString(), currentInterface});
            lr.setThrown(e);
            LOG.log(lr);
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            LogRecord lr = new LogRecord(Level.SEVERE, "cant set value: \"{0}\" of \"{1}\"");
            lr.setParameters(new Object[]{nameBuilder.toString(), currentInterface});
            lr.setThrown(e);
            LOG.log(lr);
            throw new RuntimeException(e);
        }
    }

    private enum State {

        COLLECT_PATH,
        TRY_COLLECT_PATH_OR_VALUE_OR_INTERFACE,
        COLLECT_INTERFACE,
        COLLECT_NAME_OF_VALUE,
        TRY_COLLECT_VALUE,
        COLLECT_VALUE,
        COLLECT_MULTILINE_VALUE;

    }

    private StringBuilder nameBuilder;
    private StringBuilder valueBuilder;
    private State state;
    private int leadingWhiteSpaces;
    private TreeNode rootNode;
    private TreeNode currentNode;
    private DBusInterface currentInterface;
    private final static Logger LOG = Logger.getLogger(Parser.class.getName());

    public Parser() {
        nameBuilder = new StringBuilder();
        valueBuilder = new StringBuilder();
        state = State.COLLECT_PATH;
    }

    public TreeNode parse(InputStream is) throws IOException {
        nameBuilder.setLength(0);
        valueBuilder.setLength(0);
        state = State.COLLECT_PATH;
        InputStreamReader r = new InputStreamReader(is);
        int i;
        while (-1 != (i = r.read())) {
            final char c = (char) i;
            switch (state) {
                case COLLECT_PATH:
                    switch (c) {
                        case '/':
                            addTreeNode();
                            break;
                        case ':':
                            break;
                        case '\n':
                            addTreeNode();
                            LOG.log(Level.FINE, "current path \"{0}\"", currentNode.getPath());
                            state = State.TRY_COLLECT_PATH_OR_VALUE_OR_INTERFACE;
                            break;
                        default:
                            nameBuilder.append(c);
                    }
                    break;
                case TRY_COLLECT_PATH_OR_VALUE_OR_INTERFACE:
                    switch (c) {
                        case '/':
                            if (leadingWhiteSpaces == 0) {
                                LOG.log(Level.FINER, "start collecting path");
                                currentNode = rootNode;
                                nameBuilder.setLength(0);
                                state = State.COLLECT_PATH;
                            } else {
                                if (leadingWhiteSpaces == 2) {
                                    state = State.COLLECT_INTERFACE;
                                    nameBuilder.setLength(0);
                                    nameBuilder.append(c);
                                } else if (leadingWhiteSpaces == 4) {
                                    state = State.COLLECT_NAME_OF_VALUE;
                                    nameBuilder.setLength(0);
                                    nameBuilder.append(c);
                                } else if (leadingWhiteSpaces > 4) {
                                    state = State.COLLECT_MULTILINE_VALUE;
                                    valueBuilder.setLength(0);
                                    valueBuilder.append(c);
                                } else {
                                    LOG.log(Level.SEVERE, "cant handle input token! name=\"{0}\" token: {1}", new Object[]{nameBuilder.toString(), c});
                                    throw new RuntimeException();
                                }
                            }
                        break;
                        case '\n':
                            leadingWhiteSpaces = 0;
                            currentInterface = null;
                            currentNode = rootNode;
                            break;
                        case ' ':
                            leadingWhiteSpaces++;
                            break;
                        default:
                            if (leadingWhiteSpaces == 2) {
                                state = State.COLLECT_INTERFACE;
                                nameBuilder.setLength(0);
                                nameBuilder.append(c);
                            } else if (leadingWhiteSpaces == 4) {
                                state = State.COLLECT_NAME_OF_VALUE;
                                nameBuilder.setLength(0);
                                nameBuilder.append(c);
                            } else if (leadingWhiteSpaces > 4) {
                                state = State.COLLECT_MULTILINE_VALUE;
                                valueBuilder.setLength(0);
                                valueBuilder.append(c);
                            } else {
                                LOG.log(Level.SEVERE, "cant handle input token! name=\"{0}\" token: {1}", new Object[]{nameBuilder.toString(), c});
                                throw new RuntimeException();
                            }
                    }
                    break;
                case COLLECT_INTERFACE:
                    switch (c) {
                        case ':':
                            break;
                        case '\n':
                            switch (nameBuilder.toString()) {
                                case "org.freedesktop.UDisks2.Manager":
                                    currentInterface = new Manager();
                                    currentNode.addInterface(currentInterface);
                                    break;
                                case "org.freedesktop.UDisks2.Block":
                                    currentInterface = new Block();
                                    currentNode.addInterface(currentInterface);
                                    break;
                                case "org.freedesktop.UDisks2.Loop":
                                    currentInterface = new Loop();
                                    currentNode.addInterface(currentInterface);
                                    break;
                                case "org.freedesktop.UDisks2.PartitionTable":
                                    currentInterface = new PartitionTable();
                                    currentNode.addInterface(currentInterface);
                                    break;
                                case "org.freedesktop.UDisks2.Filesystem":
                                    currentInterface = new Filesystem();
                                    currentNode.addInterface(currentInterface);
                                    break;
                                case "org.freedesktop.UDisks2.Partition":
                                    currentInterface = new Partition();
                                    currentNode.addInterface(currentInterface);
                                    break;
                                case "org.freedesktop.UDisks2.Swapspace":
                                    currentInterface = new Swapspace();
                                    currentNode.addInterface(currentInterface);
                                    break;
                                case "org.freedesktop.UDisks2.Drive":
                                    currentInterface = new Drive();
                                    currentNode.addInterface(currentInterface);
                                    break;
                                case "org.freedesktop.UDisks2.Drive.Ata":
                                    currentInterface = new Ata();
                                    currentNode.addInterface(currentInterface);
                                    break;

                                default:
                                    LOG.log(Level.SEVERE, "unknown interface \"{0}\"", nameBuilder.toString());
                                    throw new RuntimeException("Unknown interface " + nameBuilder.toString());
                            }
                            LOG.log(Level.FINE, "current interface \"{0}\"", currentInterface.getClass().getName());
                            leadingWhiteSpaces = 0;
                            state = State.TRY_COLLECT_PATH_OR_VALUE_OR_INTERFACE;
                        default:
                            nameBuilder.append(c);
                    }
                    break;
                case COLLECT_NAME_OF_VALUE:
                    switch (c) {
                        case ':':
                            leadingWhiteSpaces = 0;
                            valueBuilder.setLength(0);
                            LOG.log(Level.FINER, "current property name \"{0}\"", nameBuilder.toString());
                            state = State.TRY_COLLECT_VALUE;
                            break;
                        default:
                            nameBuilder.append(c);
                    }
                    break;
                case TRY_COLLECT_VALUE:
                    switch (c) {
                        case ' ':
                            leadingWhiteSpaces++;
                            break;
                        case '\n':
                            LOG.log(Level.FINER, "current property value \"{0}\"", valueBuilder.toString());
                            //TODO no value special case ???
                            setProperty();
                            leadingWhiteSpaces = 0;
                            state = State.TRY_COLLECT_PATH_OR_VALUE_OR_INTERFACE;
                            break;
                        default:
                            state = State.COLLECT_VALUE;
                            valueBuilder.append(c);
                    }
                    break;
                case COLLECT_VALUE:
                    switch (c) {
                        case '\n':
                            LOG.log(Level.FINER, "current property value \"{0}\"", valueBuilder.toString());
                            setProperty();
                            leadingWhiteSpaces = 0;
                            state = State.TRY_COLLECT_PATH_OR_VALUE_OR_INTERFACE;
                            break;
                        default:
                            valueBuilder.append(c);
                    }
                    break;
                case COLLECT_MULTILINE_VALUE:
                    switch (c) {
                        case '\n':
                            LOG.log(Level.FINER, "next line to add to property value \"{0}\"", valueBuilder.toString());
                            addToProperty();
                            leadingWhiteSpaces = 0;
                            state = State.TRY_COLLECT_PATH_OR_VALUE_OR_INTERFACE;
                            break;
                        default:
                            valueBuilder.append(c);
                    }
                    break;
                default:
                    LOG.log(Level.SEVERE, "Unknown state=\"{0}\"", state);
                    throw new RuntimeException();

            }
        }
        return rootNode;
    }

    private void addTreeNode() {
        final String name = nameBuilder.toString();
        nameBuilder.setLength(0);
        if ((name.length() == 0) && (rootNode == null)) {
            rootNode = new TreeNode("");
            currentNode = rootNode;
            LOG.log(Level.FINER, "Root node added");
        } else {
            TreeNode node = null;
            if (currentNode != null) {
                node = currentNode.getChildNode(name);
            }
            if (node == null) {
                node = new TreeNode(name);
                currentNode.addChildNode(node);
                LOG.log(Level.FINER, "node \"{0}\" added", name);
            } else {
                LOG.log(Level.FINER, "node \"{0}\" found", name);
            }
            currentNode = node;
        }
    }
}
