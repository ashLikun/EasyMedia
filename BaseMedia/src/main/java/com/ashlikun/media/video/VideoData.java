package com.ashlikun.media.video;

import android.net.Uri;
import android.text.TextUtils;

import java.io.FileDescriptor;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/18　15:33
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：视频播放数据源
 */

public class VideoData implements Serializable {
    Builder builder;

    protected VideoData(Builder builder) {
        this.builder = builder;
    }

    public String getUrl() {
        return builder.url;
    }

    /**
     * 是否是本地文件
     *
     * @return
     */
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

    public FileDescriptor getFileDescriptor() {
        return builder.fileDescriptor;
    }

    public IMediaDataSource getIMediaDataSource() {
        return builder.source;
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

    public boolean isLooping() {
        return builder.isLooping;
    }

    public boolean isCache() {
        return builder.isCache;
    }

    public String cacheDir() {
        return builder.cacheDir;
    }

    public String overrideExtension() {
        return builder.overrideExtension;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        return super.equals(obj) || (obj instanceof VideoData && equalsUrl((VideoData) obj));
    }

    public boolean equalsUrl(VideoData obj) {
        if (obj == null) {
            return false;
        }
        return TextUtils.equals(getUri() == null ? "noCurr" : getUri().toString(), obj.getUri() == null ? "noObj" : obj.getUri().toString()) ||
                TextUtils.equals(getUrl() == null ? "noCurr" : getUrl(), obj.getUrl() == null ? "noObj" : obj.getUrl()) ||
                TextUtils.equals(getFileDescriptor() == null ? "noCurr" : getFileDescriptor().toString(), obj.getFileDescriptor() == null ? "noObj" : obj.getFileDescriptor().toString());
    }


    public static class Builder implements Serializable {
        protected String url;
        protected String title;
        protected Uri uri;
        //是否循环
        protected boolean isLooping = VideoUtils.isLooping;
        //是否使用缓存
        protected boolean isCache = VideoUtils.isCache;
        //缓存目录
        protected String cacheDir = VideoUtils.cacheDir;
        //类型覆盖 exo用的
        private String overrideExtension;
        protected IMediaDataSource source;
        protected FileDescriptor fileDescriptor;
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

        public Builder fileDescriptor(FileDescriptor fileDescriptor) {
            this.fileDescriptor = fileDescriptor;
            return this;
        }

        public Builder isLooping(boolean isLooping) {
            this.isLooping = isLooping;
            return this;
        }

        public Builder cacheDir(String cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }

        public Builder overrideExtension(String overrideExtension) {
            this.overrideExtension = overrideExtension;
            return this;
        }

        public Builder isCache(boolean isCache) {
            this.isCache = isCache;
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


        public VideoData builder() {
            return new VideoData(this);
        }

    }
}
