/**
 * The MIT License (MIT)
 *
 * Copyright (C) 2011-2016 Incapture Technologies LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package rapture.object.storage;

import rapture.common.RaptureURI;
import rapture.common.Scheme;
import rapture.common.exception.RaptureExceptionFactory;

/**
 * This class should never be used directly. It should only be accessed from the
 * autogenerated code from ApiGen
 *
 * @author bardhi
 */
public class StorageLocationFactory {

    public static RaptureURI createStorageLocation(String repoName, String prefix, String storagePath) {
        String docPath = docPath(prefix, storagePath);
        return RaptureURI.builder(Scheme.DOCUMENT, repoName).docPath(docPath).build();
    }

    /**
     * Given a {@link RaptureURI} address as well as the {@link Class} of the
     * object we wish to access using this address, return the storage location
     * that corresponds to the address. For example, passing in the arguments
     * job://authorityA/path/to/myJob and RaptureJob.class will return
     * document://sysconfig/job/authorityA/path/to/my/job
     *
     * @param addressURI
     * @return
     */
    public static RaptureURI createStorageURI(String repoName, String prefix, RaptureURI addressURI) {
        String docPath = docPath(prefix, addressURI.getFullPath());
        if (docPath.endsWith("/")) {
            docPath = docPath.substring(0, docPath.length() - 1);
        }
        return RaptureURI.builder(Scheme.DOCUMENT, repoName).docPath(docPath).build();

    }

    private static String docPath(String pathPrefix, String objectDocPath) {
        int colon = objectDocPath.indexOf(":/");
        StringBuilder sb = new StringBuilder();
        if (pathPrefix != null && pathPrefix.length() > 0) {
            sb.append(pathPrefix).append("/");
        }
        sb.append(objectDocPath.substring(colon + 1));
        String path = sb.toString();
        path = path.replaceAll("//+", "/");
        if (path.contains(":")) throw RaptureExceptionFactory.create("Illegal Document path " + path);
        return path;
    }

}