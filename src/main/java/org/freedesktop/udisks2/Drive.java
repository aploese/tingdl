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
public class Drive implements DBusInterface {
    private boolean canPowerOff;
    private String configuration;
    private String connectionBus;              
    private boolean  ejectable;
    private String id;
    private String media;                      
    private boolean mediaAvailable;
    private boolean mediaChangeDetected;
    private String[] mediaCompatibility;
    private boolean mediaRemovable;
    private String model;
    private boolean optical;
    private boolean opticalBlank;
    private long opticalNumAudioTracks;
    private long opticalNumDataTracks;
    private long opticalNumSessions;
    private long opticalNumTracks;
    private boolean removable;
    private String revision;
    private int rotationRate;
    private String seat;
    private String serial;
    private String siblingId;                  
    private long size;
    private String sortKey;
    private long timeDetected;
    private long timeMediaDetected;
    private String vendor;                     
    private String wWN;           
}
