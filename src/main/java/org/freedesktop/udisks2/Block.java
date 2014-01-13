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
public class Block implements DBusInterface {

    private String configuration;
    private String  cryptoBackingDevice;
    private String  device;
    private Integer deviceNumber;
    private String  drive;
    private boolean hintAuto;
    private String  hintIconName;               
    private boolean hintIgnore;
    private String hintName;                   
    private boolean hintPartitionable;
    private String hintSymbolicIconName;       
    private boolean hintSystem;
    private String id;                         
    private String idLabel;                    
    private String idType;                     
    private String idUUID;                     
    private String idUsage;                  
    private String  idVersion;                  
    private String mDRaid;
    private String mDRaidMember;
    private String preferredDevice;
    private boolean readOnly;
    private Long size;
    private String[] symlinks;                   

    public String getConfiguration() {
        return configuration;
    }

    public String getCryptoBackingDevice() {
        return cryptoBackingDevice;
    }

    public String getDevice() {
        return device;
    }

    public Integer getDeviceNumber() {
        return deviceNumber;
    }

    public String getDrive() {
        return drive;
    }

    public boolean isHintAuto() {
        return hintAuto;
    }

    public String getHintIconName() {
        return hintIconName;
    }

    public boolean isHintIgnore() {
        return hintIgnore;
    }

    public String getHintName() {
        return hintName;
    }

    public boolean isHintPartitionable() {
        return hintPartitionable;
    }

    public String getHintSymbolicIconName() {
        return hintSymbolicIconName;
    }

    public boolean isHintSystem() {
        return hintSystem;
    }

    public String getId() {
        return id;
    }

    public String getIdLabel() {
        return idLabel;
    }

    public String getIdType() {
        return idType;
    }

    public String getIdUUID() {
        return idUUID;
    }

    public String getIdUsage() {
        return idUsage;
    }

    public String getIdVersion() {
        return idVersion;
    }

    public String getmDRaid() {
        return mDRaid;
    }

    public String getmDRaidMember() {
        return mDRaidMember;
    }

    public String getPreferredDevice() {
        return preferredDevice;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public Long getSize() {
        return size;
    }

    public String[] getSymlinks() {
        return symlinks;
    }

}
