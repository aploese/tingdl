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
public class Manager implements DBusInterface {

    private String version;

    public String getVersion() {
        return version;
    }

}
