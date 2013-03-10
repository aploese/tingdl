/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.tingdl.config;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author aploese
 */
public class TingConfigTest {
    
    public TingConfigTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of parseSerialNumber method, of class TingConfig.
     */
    @Test
    public void testParseSerialNumber() {
        System.out.println("parseSerialNumber");
        assertEquals(21474836987L, TingConfig.parseSerialNumber("05:00:00:01:fb"));
        assertEquals(8589937758L, TingConfig.parseSerialNumber("00:02:00:00:0c:5e"));
        assertEquals(4294970842L, TingConfig.parseSerialNumber("00:01:00:00:0d:da"));
    }

    /**
     * Test of formatSerialNumber method, of class TingConfig.
     */
    @Test
    public void testFormatSerialNumber() {
        System.out.println("formatSerialNumber");
        assertEquals("00:05:00:00:01:fb", TingConfig.formatSerialNumber(21474836987L));
        assertEquals("00:02:00:00:0c:5e", TingConfig.formatSerialNumber(8589937758L));
        assertEquals("00:01:00:00:0d:da", TingConfig.formatSerialNumber(4294970842L));
        
    }

}