/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.tingdl.dl;

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
public class ProgressIndicatorTest {
    
    public ProgressIndicatorTest() {
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
     * Test of updateProgress method, of class ProgressIndicator.
     */
    @Test
    public void testUpdateProgress() {
        System.out.println("updateProgress");
        ProgressIndicator instance = new ProgressIndicator(1024*1024);
        instance.updateProgress(1024);
//        fail("The test case is a prototype.");
    }

}
