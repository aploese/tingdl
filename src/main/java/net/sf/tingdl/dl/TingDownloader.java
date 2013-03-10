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
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Properties;
import net.sf.tingdl.config.Book;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author aploese
 */
public class TingDownloader {

    private static Logger LOG = LoggerFactory.getLogger(TingDownloader.class);

    public static String BUSY_PATH = "/public/server-busy/";
    public static String ADD_SN_PATH_TEMPLATE = "/%%d/public/add-sn/start_sn/%d/fw_version/%s/ting_version/%s/";
    public static String DESCRIPTION_PATH_TEMPLATE = "/book-files/get-description/id/%d/area/%s/sn/%d/";
    public static String THUMB_PATH_TEMPLATE = "/book-files/get/id/%s/type/thumb/area/%s/sn/%d/";
    public static String ARCHIVE_PATH_TEMPLATE = "/book-files/get/id/%s/area/%s/type/archive/sn/%d/";
    public static String CDFS_PATH_TEMPLATE = "/cdfs.php?action=version&OS=%s&fw=%sd&ting=%s&sn=%d";
    public static String FW_PATH_TEMPLATE = "/fw.php?action=version&OS=%s&fw=%s&ting=%s&sn=%d";
    private HttpClient httpclient;
    private HttpGet httpGet;
    private InetAddress address;
    private long serialNumber;

    public TingDownloader() {
        httpclient = new DefaultHttpClient();
        httpGet = new HttpGet();
    }

    private URI buildUriCdfs(String os, String fwVersion, String tingVersion) {
        return buildURI(CDFS_PATH_TEMPLATE, os, fwVersion, tingVersion, serialNumber);
    }

    private URI buildUriFw(String os, String fwVersion, String tingVersion) {
        return buildURI(FW_PATH_TEMPLATE, os, fwVersion, tingVersion, serialNumber);
    }

    private URI buildUriThumb(Book b) {
        return buildURI(THUMB_PATH_TEMPLATE, b.getId(), b.getLang(), serialNumber);
    }

    private URI buildUriArchive(Book b) {
        return buildURI(ARCHIVE_PATH_TEMPLATE, b.getId(), b.getLang(), serialNumber);
    }

    private URI buildUriDescription(Book b) {
        return buildURI(DESCRIPTION_PATH_TEMPLATE, b.getId(), b.getLang(), serialNumber);
    }

    private URI buildUriAddSn(long sn, String fwVersion, String tingVersion) {
        return buildURI(ADD_SN_PATH_TEMPLATE, sn, fwVersion, tingVersion.split(".")[3]);
    }

    private URI buildURI(String pathTemplate, Object... pathArgs) {
        try {
            return new URI("http", address.getHostAddress(), String.format(pathTemplate, pathArgs), null);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public InetAddress getServerIp(String host) {
        InetAddress[] addresses = null;
        int trys = 3;
        try {
            while (trys-- > 0) {
                addresses = InetAddress.getAllByName(host);
                for (InetAddress addr : addresses) {
                    address = addr;
                    httpGet.setURI(buildURI(BUSY_PATH));
                    ResponseHandler<String> responseHandler = new BasicResponseHandler();

                    try {
                        if ("BUSY".equals(httpclient.execute(httpGet, responseHandler))) {
                            System.out.printf("ting @%s is busy", addr.toString());
                        } else {
                            return address;
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }

                }
                try {
                    // wait 10 s for clearing the lookuocache of InetAddress
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        } catch (UnknownHostException ex) {
            throw new RuntimeException(ex);
        }
        return addresses[0];
    }

    public long addSerialNumber(long sn, String fwVersion, String tingVersion) {
        httpGet.setURI(buildUriAddSn(sn, fwVersion, tingVersion));
        ResponseHandler<String> responseHandler = new BasicResponseHandler();
        try {
            serialNumber = Integer.parseInt(httpclient.execute(httpGet, responseHandler));
            return serialNumber;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }

    /**
     * @return the serialNumber
     */
    public long getSerialNumber() {
        return serialNumber;
    }

    /**
     * @param serialNumber the serialNumber to set
     */
    public void setSerialNumber(long serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void downloadBooks(Collection<TingDownloadJob> jobs, File tingPath) {
        BookResponseHandler bookHandler = new BookResponseHandler();
        SaveToFileResponseHandler fileHandler = new SaveToFileResponseHandler();
        fileHandler.setTingPath(tingPath);
        for (TingDownloadJob job : jobs) {
            httpGet.setURI(buildUriDescription(job.getBook()));
            bookHandler.setId(job.getBookId());
            bookHandler.setArea(job.getBookArea());
            try {
                Book book = httpclient.execute(httpGet, bookHandler);
                if (job.updateNeeded(book)) {
                    // UpdateBook
                    job.setBook(book);
                    fileHandler.setBook(book);
                    fileHandler.setDestinations(job.getDestinationTypes());
                    fileHandler.setFileType(SaveToFileResponseHandler.FileType.ARCHIVE);

                    httpGet.setURI(buildUriArchive(book));
                    int length = httpclient.execute(httpGet, fileHandler);
                    fileHandler.checkMd5Sum();

                    fileHandler.setFileType(SaveToFileResponseHandler.FileType.THUMB);
                    httpGet.setURI(buildUriThumb(book));

                    length = httpclient.execute(httpGet, fileHandler);
                    fileHandler.checkMd5Sum();

                    if (job.isBackup()) {
                        book.writeToFile(book.getBackupDir());
                        job.setBackupUpdated(true);
                    }
                    if (job.isTing()) {
                        book.writeToFile(tingPath);
                        job.setTingUpdated(true);
                    }
                    System.out.printf("Book: %s updated\n", book.getArchiveFile(tingPath));
                } else {
                    System.out.printf("Book: %s up to date\n", book.getArchiveFile(tingPath));
                    // Copy over if not there ...
                    if (job.isTing() && !job.getBook().getDescriptionFile(tingPath).exists()) {
                        job.copyFromBackup(tingPath);
                        job.setTingUpdated(true);
                    }
                }
            } catch (HttpResponseException ex) {
                LOG.error("Statuscode; {0} URI: {1}", ex.getStatusCode(), httpGet.getURI().toString());
            } catch (Exception ex) {
                LOG.error("HTTP error", ex);
                continue;
            }
        }
    }

    public Properties checkCdfsVersion(String os, String fwVersion, String tingVersion) throws IOException {
        httpGet.setURI(buildUriCdfs(os, fwVersion, tingVersion));
        VersionCheckResponseHandler versionCheckResponseHandler = new VersionCheckResponseHandler();
        return httpclient.execute(httpGet, versionCheckResponseHandler);
    }

    public void updateCdfsVersion(Properties cdfsProps) {
        String cdfsVersion = cdfsProps.getProperty("version");
        String cdfsFile = cdfsProps.getProperty("name");
        String cdfsFileMd5 = cdfsProps.getProperty("md5sum");
        /* TODO
         httpGet.setURI(buildURI(cdfsFile));

         fileHandler.setFilename(String.format("%scdfs-%s.iso", outDir, cdfsVersion));
         int length = httpclient.execute(httpGet, fileHandler);
         if (!cdfsFileMd5.equals(fileHandler.getMd5Sum())) {
         System.out.println(String.format("ERROR MD5 File: %s md5: %s %s length: %d", fileHandler.getFilename(), cdfsFileMd5, fileHandler.getMd5Sum(), length));
         } else {
         System.out.println(String.format("File: %s md5: %s %s length: %d", fileHandler.getFilename(), cdfsFileMd5, fileHandler.getMd5Sum(), length));

         }
         */
    }

    public Properties checkFwVersion(String os, String fwVersion, String tingVersion) throws IOException {
        httpGet.setURI(buildUriFw(os, fwVersion, tingVersion));
        VersionCheckResponseHandler versionCheckResponseHandler = new VersionCheckResponseHandler();
        return httpclient.execute(httpGet, versionCheckResponseHandler);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public synchronized void close() {
        if (httpclient != null) {
            httpclient.getConnectionManager().shutdown();
            httpclient = null;
        }
    }
}