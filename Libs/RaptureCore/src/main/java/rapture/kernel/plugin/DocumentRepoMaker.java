/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2011-2016 Incapture Technologies LLC
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
package rapture.kernel.plugin;

import org.apache.commons.lang.StringUtils;

import rapture.common.CallingContext;
import rapture.common.PluginTransportItem;
import rapture.common.RaptureURI;
import rapture.common.exception.RaptureExceptionFactory;
import rapture.common.impl.jackson.JacksonUtil;
import rapture.common.model.DocumentRepoConfig;
import rapture.kernel.Kernel;

public class DocumentRepoMaker implements RaptureInstaller {
    @Override
    public void install(CallingContext context, RaptureURI uri, PluginTransportItem item) {
        String uriString = uri.toString();
        DocumentRepoConfig config = JacksonUtil.objectFromJson(item.getContent(), DocumentRepoConfig.class);
        String realConfig = config.getDocumentRepo().getConfig();
        if (Kernel.getDoc().docRepoExists(context, uriString)) {
            DocumentRepoConfig remote = Kernel.getDoc().getDocRepoConfig(context, uriString);
            if (remote == null) {
                throw RaptureExceptionFactory.create("Unable to load config for existing repository: " + uriString);
            }
            if (!StringUtils.equals(format(remote.getDocumentRepo().getConfig()), format(realConfig))) {
                throw RaptureExceptionFactory.create("Repository already exists with differing configuration: " + uriString);
            }
        } else {
            Kernel.getDoc().createDocRepo(context, uri.toString(), realConfig);
            Kernel.getDoc().getTrusted().updateDocumentRepo(context, config);
        }
    }

    @Override
    public void remove(CallingContext context, RaptureURI uri, PluginTransportItem item) {
        String uriString = uri.toString();
        if (Kernel.getDoc().docRepoExists(context, uriString)) {
            Kernel.getDoc().deleteDocRepo(context, uriString);
        }
    }

    private String format(String in) {
        return in.replace(" ", "").replace("\t", "").replace("\n", "");
    }
}
