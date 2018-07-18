/*
 * Copyright 2018 Bakumon. https://github.com/Bakumon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.bakumon.moneykeeper.api;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.Date;
import java.util.List;

/**
 * 坚果云文件结构
 *
 * @author Bakumon https://bakumon.me
 */
public class DavFileList {
    @ElementList(name = "multistatus", entry = "response", inline = true, required = false)
    private List<ResponseBean> list;

    public List<ResponseBean> getList() {
        return list;
    }

    public void setList(List<ResponseBean> list) {
        this.list = list;
    }

    @Root(name = "response", strict = false)
    public static class ResponseBean {
        @Element(required = false)
        private String href;

        @Path("propstat")
        @Element(required = false)
        private String status;

        @Path("propstat/prop")
        @Element(required = false, name = "getlastmodified")
        private String lastModified;

        @Path("propstat/prop")
        @Element(required = false, name = "getcontentlength")
        private String contentLength;

        @Path("propstat/prop")
        @Element(required = false)
        private String owner;

        @Path("propstat/prop")
        @Element(required = false, name = "getcontenttype")
        private String contentType;

        @Path("propstat/prop")
        @Element(required = false, name = "displayname")
        private String displayName;

        @Path("propstat/prop")
        @Element(required = false, name = "getetag")
        private String etag;


        public Date getLastModifiedDate() {
            return new Date(lastModified);
        }


        public String getHref() {
            return href;
        }

        public void setHref(String href) {
            this.href = href;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getLastModified() {
            return lastModified;
        }

        public void setLastModified(String lastModified) {
            this.lastModified = lastModified;
        }

        public String getContentLength() {
            return contentLength;
        }

        public void setContentLength(String contentLength) {
            this.contentLength = contentLength;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEtag() {
            return etag;
        }

        public void setEtag(String etag) {
            this.etag = etag;
        }

        public boolean isDir() {
            return "httpd/unix-directory".equals(contentType);
        }
    }
}
