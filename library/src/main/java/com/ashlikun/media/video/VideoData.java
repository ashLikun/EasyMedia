package com.ashlikun.media.video;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/18　15:33
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：视频播放数据源
 */

public class VideoData implements Parcelable {
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

    public boolean equalsUrl(VideoData obj) {
        if (obj == null) {
            return false;
        }
        return TextUtils.equals(getUri().toString(), obj.getUri().toString()) || TextUtils.equals(getUrl(), obj.getUrl()) || TextUtils.equals(getFileDescriptor().toString(), obj.getFileDescriptor().toString());
    }


    public static class Builder implements Parcelable {
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


        public VideoData builder() {
            return new VideoData(this);
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.url);
            dest.writeString(this.title);
            dest.writeParcelable(this.uri, flags);
            dest.writeParcelable(this.fileDescriptor, flags);
            dest.writeInt(this.headers.size());
            for (Map.Entry<String, String> entry : this.headers.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }

        public Builder() {
        }

        protected Builder(Parcel in) {
            this.url = in.readString();
            this.title = in.readString();
            this.uri = in.readParcelable(Uri.class.getClassLoader());
            this.fileDescriptor = in.readParcelable(AssetFileDescriptor.class.getClassLoader());
            int headersSize = in.readInt();
            this.headers = new HashMap<String, String>(headersSize);
            for (int i = 0; i < headersSize; i++) {
                String key = in.readString();
                String value = in.readString();
                this.headers.put(key, value);
            }
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel source) {
                return new Builder(source);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.builder, flags);
    }

    protected VideoData(Parcel in) {
        this.builder = in.readParcelable(Builder.class.getClassLoader());
    }

    public static final Creator<VideoData> CREATOR = new Creator<VideoData>() {
        @Override
        public VideoData createFromParcel(Parcel source) {
            return new VideoData(source);
        }

        @Override
        public VideoData[] newArray(int size) {
            return new VideoData[size];
        }
    };
}
