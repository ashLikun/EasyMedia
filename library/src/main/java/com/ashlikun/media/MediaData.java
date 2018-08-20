package com.ashlikun.media;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/18　15:33
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：视频播放数据源
 */

public class MediaData {
    Builder builder;

    protected MediaData(Builder builder) {
        this.builder = builder;
    }

    public String getUrl() {
        return builder.url;
    }

    //是否是本地文件
    public boolean isLocal() {
        if (builder.url != null) {
            return builder.url.startsWith("file");
        } else if (builder.uri != null) {
            return builder.url.startsWith("file");
        } else if (builder.fileDescriptor != null) {
            return true;
        }
        return false;
    }

    public String getTitle() {
        return builder.title;
    }

    public Uri getUri() {
        return builder.uri;
    }

    public AssetFileDescriptor getFileDescriptor() {
        return builder.fileDescriptor;
    }

    public Map<String, String> getHeaders() {
        return builder.headers;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (builder.uri != null) {
            sb.append(builder.uri);
        }
        if (builder.title != null) {
            sb.append(builder.title);
        }
        if (builder.url != null) {
            sb.append(builder.url);
        }
        if (builder.headers != null) {
            sb.append(builder.headers);
        }
        if (builder.fileDescriptor != null) {
            sb.append(builder.fileDescriptor);
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return super.equals(obj) || toString().equals(obj.toString());
    }

    public static class Builder {
        protected String url;
        protected String title;
        protected Uri uri;
        protected AssetFileDescriptor fileDescriptor;
        protected Map<String, String> headers;

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder uri(Uri uri) {
            this.uri = uri;
            return this;
        }

        public Builder fileDescriptor(AssetFileDescriptor fileDescriptor) {
            this.fileDescriptor = fileDescriptor;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder addHead(String key, String value) {
            if (key == null) {
                return this;
            }
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put(key, value);
            return this;
        }


        public MediaData builder() {
            return new MediaData(this);
        }
    }
}
