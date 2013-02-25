/*
 * ting-dl - Open Source downloader for TING - http://tingdl.sf.net
 * Copyright (C) 2013  Arne Plöse.
 *
 * This file is part of ting-dl.
 *
 * Ting-dl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Papaya is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ting-dl.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package net.sf.tingdl.dl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Set;
import net.sf.tingdl.config.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aploese
 */
public class TingDownloadJob {

    private static Logger LOG = LoggerFactory.getLogger(TingDownloadJob.class);
    private Book book;
    private Set<DestinationType> destinationTypes = EnumSet.noneOf(DestinationType.class);
    private Set<DestinationType> destinationUpdated = EnumSet.noneOf(DestinationType.class);

    public int getBookId() {
        return book.getId();
    }

    /**
     * @return the book
     */
    public Book getBook() {
        return book;
    }

    public String getBookArea() {
        return book.getLang();
    }

    /**
     * @return the backup
     */
    public boolean isBackup() {
        return destinationTypes.contains(DestinationType.BACKUP);
    }

    /**
     * @param backup the backup to set
     */
    public void setBackup(boolean backup) {
        if (backup) {
            this.destinationTypes.add(DestinationType.BACKUP);
        } else {
            this.destinationTypes.remove(DestinationType.BACKUP);
        }
    }

    /**
     * @return the ting
     */
    public boolean isTing() {
        return destinationTypes.contains(DestinationType.TING);
    }

    /**
     * @param ting the ting to set
     */
    public void setTing(boolean ting) {
        if (ting) {
            this.destinationTypes.add(DestinationType.TING);
        } else {
            this.destinationTypes.remove(DestinationType.TING);
        }
    }

    /**
     * @return the tingUpdated
     */
    public boolean isTingUpdated() {
        return destinationUpdated.contains(DestinationType.TING);
    }

    /**
     * @param tingUpdated the tingUpdated to set
     */
    public void setTingUpdated(boolean tingUpdated) {
        if (tingUpdated) {
            this.destinationUpdated.add(DestinationType.TING);
        } else {
            this.destinationUpdated.remove(DestinationType.TING);
        }
    }

    /**
     * @return the backupUpdated
     */
    public boolean isBackupUpdated() {
        return destinationUpdated.contains(DestinationType.BACKUP);
    }

    /**
     * @param backupUpdated the backupUpdated to set
     */
    public void setBackupUpdated(boolean backupUpdated) {
        if (backupUpdated) {
            this.destinationUpdated.add(DestinationType.BACKUP);
        } else {
            this.destinationUpdated.remove(DestinationType.BACKUP);
        }
    }

    /**
     * @param book the book to set
     */
    public void setBook(Book book) {
        this.book = book;
    }

    boolean updateNeeded(Book b) {
        return this.book.updateNeeded(b);
    }

    /**
     * @return the destinationTypes
     */
    public Set<DestinationType> getDestinationTypes() {
        return destinationTypes;
    }

    public static int copyFile(File src, File dest, int bufferSize) throws IOException {
        try (InputStream is = new FileInputStream(src)) {
            byte[] data = new byte[bufferSize];
            int fileLength = 0;

            try (FileOutputStream os = new FileOutputStream(dest)) {
                int length = 0;
                while ((length = is.read(data)) > -1) {
                    os.write(data, 0, length);
                    fileLength += length;
                }
            }
            return fileLength;
        }
    }

    public boolean copyFromBackup(File tingPath) {
        try {
            System.out.printf("Copy %s from %s to %s\n", book.getArchiveName(), book.getBackupDir(), tingPath);
            copyFile(book.getArchiveFile(book.getBackupDir()), book.getArchiveFile(tingPath), 8192);
            System.out.printf("Copy %s from %s to %s\n", book.getThumName(), book.getBackupDir(), tingPath);
            copyFile(book.getThumbFile(book.getBackupDir()), book.getThumbFile(tingPath), 8192);
            book.writeToFile(tingPath);
            return true;
        } catch (IOException ex) {
            // What to do??
            throw new RuntimeException(ex);
        }
    }
}
