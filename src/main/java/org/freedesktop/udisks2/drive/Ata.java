/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freedesktop.udisks2.drive;

import org.freedesktop.udisks2.DBusInterface;

/**
 *
 * @author aploese
 */
public class Ata implements DBusInterface {
    
    private boolean aamEnabled;
    private boolean aamSupported;
    private int aamVendorRecommendedValue;
    private boolean apmEnabled;
    private boolean apmSupported;
    private boolean pmEnabled;
    private boolean pmSupported;
    private long securityEnhancedEraseUnitMinutes;
    private long securityEraseUnitMinutes;
    private boolean securityFrozen;
    private boolean smartEnabled;
    private boolean smartFailing;
    private long smartNumAttributesFailedInThePast;
    private long smartNumAttributesFailing;
    private long smartNumBadSectors;
    private int smartPowerOnSeconds;
    private int smartSelftestPercentRemaining;
    private String smartSelftestStatus;
    private boolean smartSupported;
    private double smartTemperature;
    private long smartUpdated;
    private boolean writeCacheEnabled;
    private boolean writeCacheSupported;

}
