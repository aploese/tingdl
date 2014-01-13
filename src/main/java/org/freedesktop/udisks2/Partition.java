/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freedesktop.udisks2;

/**
 *
 * @author aploese
 */
public class Partition implements DBusInterface {
  
    private int flags;
    private boolean isContained;
    private boolean isContainer;
    private String name;               
    private int number;
    private long offset;
    private long size;
    private String table;
    private String type;
    private String uUID;               
}
