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
public class Filesystem implements DBusInterface {

    private String[] mountPoints;

    public String[] getMountPoints() {
        return mountPoints;
    }

}
