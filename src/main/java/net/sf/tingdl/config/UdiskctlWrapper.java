/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.tingdl.config;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.freedesktop.udisks2.Block;
import org.freedesktop.udisks2.DBusInterface;
import org.freedesktop.udisks2.Filesystem;
import org.freedesktop.udisks2.Parser;
import org.freedesktop.udisks2.TreeNode;

/**
 *
 * @author aploese
 */
public class UdiskctlWrapper {
    
    private final static UdiskctlWrapper wrapper = new UdiskctlWrapper();
    
    private Block tingDevice;
    private Filesystem tingFilesystem;

    public UdiskctlWrapper() {
        refresh();
    }

    public boolean isAvailable() {
        return tingDevice != null;
    }

    public boolean isMounted() {
        return tingFilesystem != null ? tingFilesystem.getMountPoints() != null : false;
    }

    public String getMountedDir() {
        return tingFilesystem.getMountPoints()[0];
    }

    public Block getTingDevice() {
        return tingDevice;
    }

    public Filesystem getFileSystem() {
        return tingFilesystem;
    }

    public boolean uMountTing() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/usr/bin/udisksctl", "unmount", "--no-user-interaction", "-b", tingDevice.getDrive()});
            process.waitFor();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(UdiskctlWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        refresh();
        return isMounted();
    }

    public String mountTing() {
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/usr/bin/udisksctl",
                "unmount", "--no-user-interaction", "-b", tingDevice.getDrive()});
            process.waitFor();
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(UdiskctlWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        refresh();
        return getMountedDir();
    }

    public void refresh() {
        Parser parser = new Parser();

        try {
            Process process = Runtime.getRuntime().exec(new String[]{"/usr/bin/udisksctl", "dump"});
            TreeNode root = parser.parse(process.getInputStream());
            Collection<TreeNode> blockNodes = root.getNodesOfPath("/org/freedesktop/UDisks2/block_devices");
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
            if (tingNode == null) {
                tingDevice = null;
                tingFilesystem = null;

            } else {
                tingDevice = tingNode.getChildInterface(Block.class
                );
                tingFilesystem = tingNode.getChildInterface(Filesystem.class);
            }
        } catch (IOException ex) {
            Logger.getLogger(UdiskctlWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
