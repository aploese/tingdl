/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.freedesktop.udisks2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Handler;

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
public class ParserTest {

    public ParserTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        LogManager.getLogManager().readConfiguration(ParserTest.class.getResourceAsStream("/logging.properties"));
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
     * Test of parse method, of class Parser.
     */
    @Test
    public void testParse() throws Exception {
        System.out.println("parse");
        InputStream is = ParserTest.class.getResourceAsStream("/udisksctl_dump.txt");
        Parser instance = new Parser();
        TreeNode expResult = new TreeNode("");
        TreeNode result = instance.parse(is);
        Collection<TreeNode> blockNodes = result.getNodesOfPath("/org/freedesktop/UDisks2/block_devices");
        TreeNode tingNode = null;
        
        for (TreeNode tn : blockNodes) {
            for (DBusInterface i : tn.getInterfaces()) {
                if (i instanceof Block) {
                    final Block blockDevice = (Block) i;
                    if (blockDevice.getIdLabel().equals("Ting")) {
                        tingNode = tn;
                        break;
                    }
            }
            }
        }

        Block tingDevice = tingNode.getChildInterface(Block.class) ;
        Filesystem tingFilesystem = tingNode.getChildInterface(Filesystem.class);

        assertEquals("/dev/sdc", tingDevice.getDevice());
        assertArrayEquals(new String[]{"/media/Ting"}, tingFilesystem.getMountPoints());
    }

}
