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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import net.sf.tingdl.config.Book;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

/**
 *
 * @author aploese
 */
public class SaveToFileResponseHandler implements ResponseHandler<Long> {

   private static final Logger LOG = Logger.getLogger(SaveToFileResponseHandler.class.getName());
   
   private boolean isTing() {
        return destinations.contains(DestinationType.TING);
    }

    private boolean isBackup() {
        return destinations.contains(DestinationType.BACKUP);
    }

    /**
     * @return the tingPath
     */
    public File getTingPath() {
        return tingPath;
    }

    /**
     * @param tingPath the tingPath to set
     */
    public void setTingPath(File tingPath) {
        this.tingPath = tingPath;
    }

    /**
     * @return the destinations
     */
    public Set<DestinationType> getDestinations() {
        return destinations;
    }

    /**
     * @param destinations the destinations to set
     */
    public void setDestinations(Set<DestinationType> destinations) {
        this.destinations.clear();
        this.destinations.addAll(destinations);
    }

    /**
     * @return the fileType
     */
    public FileType getFileType() {
        return fileType;
    }

    /**
     * @param fileType the fileType to set
     */
    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    /**
     * @return the book
     */
    public Book getBook() {
        return book;
    }

    /**
     * @param book the book to set
     */
    public void setBook(Book book) {
        this.book = book;
    }
         
  public static enum FileType {

        ARCHIVE,
        THUMB;
    }
    private Set<DestinationType> destinations = EnumSet.noneOf(DestinationType.class);
    private FileType fileType;
    private Book book;
    private File tingPath;
    private MessageDigest md;
    private long fileLength;
    private ProgressIndicator progressIndicator;

    public SaveToFileResponseHandler() {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the response body as a String if the response was successful (a
     * 2xx status code). If no response body exists, this returns null. If the
     * response was unsuccessful (>= 300 status code), throws an
     * {@link HttpResponseException}.
     * @param response
     * @return the file length
     * @throws org.apache.http.client.HttpResponseException
     */
   @Override
    public Long handleResponse(final HttpResponse response)
            throws HttpResponseException, IOException {
        md.reset();
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        if (statusLine.getStatusCode() >= 300) {
            EntityUtils.consume(entity);
            throw new HttpResponseException(statusLine.getStatusCode(),
                    statusLine.getReasonPhrase());
        }
        progressIndicator = new ProgressIndicator(entity.getContentLength());
        fileLength = 0;
        switch (fileType) {
            case ARCHIVE:
                    progressIndicator.printSaveFile(getBook().getArchiveName());
                    break;
            case THUMB:
                    progressIndicator.printSaveFile(getBook().getThumName());
                    break;
            default:
                throw new RuntimeException();
        }
        
        try (InputStream is = entity.getContent()) {
            byte[] data = new byte[8192];

            FileOutputStream ost = isTing() ? new FileOutputStream(getTingFile()) : null;
            FileOutputStream osb = isBackup() ? new FileOutputStream(getBackupFile()) : null;
            try {
                int length;
                while ((length = is.read(data)) > -1) {
                    md.update(data, 0, length);
                    if (ost != null) {
                        ost.write(data, 0, length);
                    }
                    if (osb != null) {
                        osb.write(data, 0, length);
                    }
                    fileLength += length;
                    progressIndicator.updateProgress(fileLength);
                }
                if (ost != null) {
                    ost.close();
                }
                if (osb != null) {
                    osb.close();
                }
            } catch (Exception ex) {
                // Newline to close Progress
                System.out.println('\n');
                // What to do??
                throw new RuntimeException(ex);
            }
        }
        // Newline to close Progress
        System.out.println('\n');
        return fileLength;
    }

    public boolean checkMd5Sum() {
        byte[] dig = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte i : dig) {
            sb.append(String.format("%02x", i));
        }
        final String md5Sum = sb.toString();
        switch (fileType) {
            case ARCHIVE:
                if (!book.getFileMD5().equals(md5Sum)) {
                    System.err.printf("ERROR MD5 File: %s md5: %s %s length: %d bytes\n", getBook().getArchiveName(), getBook().getFileMD5(), md5Sum, fileLength);
                    return false;
                } else {
                    System.out.printf("File: %s md5: %s %s length: %d bytes\n", getBook().getArchiveName(), getBook().getFileMD5(), md5Sum, fileLength);
                    return true;
                }
            case THUMB:
                if (!book.getThumbMD5().equals(md5Sum)) {
                    System.err.printf("ERROR MD5 File: %s md5: %s %s length: %d bytes\n", getBook().getThumName(), getBook().getThumbMD5(), md5Sum, fileLength);
                    return false;
                } else {
                    System.out.printf("File: %s md5: %s %s length: %d bytes\n", getBook().getThumName(), getBook().getThumbMD5(), md5Sum, fileLength);
                    return true;
                }
            default:
                throw new RuntimeException();
        }
    }

    /**
     * @return the backupFile
     */
    public File getBackupFile() {
        switch (fileType) {
            case ARCHIVE:
                return getBook().getArchiveFile(getBook().getBackupDir());
            case THUMB:
                return getBook().getThumbFile(getBook().getBackupDir());
            default:
                throw new RuntimeException();
        }
    }

    /**
     * @return the tingFile
     */
    public File getTingFile() {
        switch (fileType) {
            case ARCHIVE:
                return getBook().getArchiveFile(tingPath);
            case THUMB:
                return getBook().getThumbFile(tingPath);
            default:
                throw new RuntimeException();
        }
    }
}