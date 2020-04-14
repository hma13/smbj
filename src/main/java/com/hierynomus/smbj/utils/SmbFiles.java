/*
 * Copyright (C)2016 - SMBJ Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hierynomus.smbj.utils;

import com.hierynomus.msdtyp.AccessMask;
import com.hierynomus.msfscc.FileAttributes;
import com.hierynomus.mssmb2.SMB2CreateDisposition;
import com.hierynomus.mssmb2.SMB2CreateOptions;
import com.hierynomus.mssmb2.SMB2ShareAccess;
import com.hierynomus.mssmb2.SMBApiException;
import com.hierynomus.smbj.io.InputStreamByteChunkProvider;
import com.hierynomus.smbj.share.Directory;
import com.hierynomus.smbj.share.DiskShare;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import static com.hierynomus.msdtyp.AccessMask.FILE_ADD_SUBDIRECTORY;
import static com.hierynomus.msdtyp.AccessMask.FILE_LIST_DIRECTORY;
import static com.hierynomus.msfscc.FileAttributes.FILE_ATTRIBUTE_DIRECTORY;
import static com.hierynomus.mssmb2.SMB2CreateDisposition.FILE_CREATE;
import static com.hierynomus.mssmb2.SMB2CreateOptions.FILE_DIRECTORY_FILE;
import static com.hierynomus.mssmb2.SMB2ShareAccess.ALL;
import static java.util.EnumSet.of;

public class SmbFiles {

    /**
     * Copies local file to a destination path on the share
     *
     * @param share     the share
     * @param destPath  the path to write to
     * @param source    the local File
     * @param overwrite true/false to overwrite existing file
     * @return the actual number of bytes that was written to the file
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static int copy(File source, DiskShare share, String destPath, boolean overwrite) throws IOException {
        int r = 0;
        if (source != null && source.exists() && source.canRead() && source.isFile()) {

            try (InputStream is = new java.io.FileInputStream(source)) {
                if (destPath != null && is != null) {
                    try (com.hierynomus.smbj.share.File f = share.openFile(
                        destPath,
                        EnumSet.of(AccessMask.GENERIC_WRITE),
                        EnumSet.of(FileAttributes.FILE_ATTRIBUTE_NORMAL),
                        EnumSet.of(SMB2ShareAccess.FILE_SHARE_WRITE),
                        (overwrite ? SMB2CreateDisposition.FILE_OVERWRITE_IF : SMB2CreateDisposition.FILE_CREATE),
                        EnumSet.noneOf(SMB2CreateOptions.class)
                    )) {
                        r = f.write(new InputStreamByteChunkProvider(is));
                    }
                }
            }
        }
        return r;
    }

    /**
     * Create a set of nested sub-directories in the given path, for example, 2345 \ 3456 \ 4453 \ 123123.txt
     */
    public void mkdirs(DiskShare diskShare, String path) throws SMBApiException {
        String currPath = path;
        StringBuilder currDir = new StringBuilder();
        while (path.startsWith("\\"))
            currPath = currPath.substring(1);

        if (currPath.indexOf( "\\") <= 0 && !diskShare.folderExists(currPath)) {
            diskShare.mkdir(currPath);
        }   else {
            for (String dir: currPath.split("\\\\")) {
                currDir = currDir.append(dir).append("\\");
                if (currDir.length() > 1 && !diskShare.folderExists(currDir.toString()))
                    diskShare.mkdir(currDir.toString() );
            }
        }
    }
}
