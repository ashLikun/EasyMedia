package com.ashlikun.media.simple.data;

import com.ashlikun.okhttputils.http.response.HttpResponse;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/8/20　15:48
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class HuoShanData extends HttpResponse {
    public int result;
    public String pcursor;
    public int new_notify;
    public String llsid;
    @SerializedName("host-name")
    public String hostname;
    public List<FeedsData> feeds;

    public static class FeedsData {

        public ExtParamsData ext_params;

        public List<HeadurlsData> headurls;
        public String caption;
        @SerializedName(value = "cover_urls", alternate = {"main_mv_urls"})
        public List<AudioUrlsData> main_mv_urls;
        public List<AudioUrlsData> cover_thumbnail_urls;
        public List<AudioUrlsData> main_mv_urls_h265;

        public String getText() {
            return caption;
        }

        public static class ExtParamsData {
            public int mtype;
            public String color;
            public int w;
            public int sound;
            public int h;
            public int interval;
            public int video;
        }


        public int getWidth() {
            return ext_params != null ? ext_params.w : -2;
        }

        public String getImageUrl() {
            return (cover_thumbnail_urls != null && !cover_thumbnail_urls.isEmpty()) ? cover_thumbnail_urls.get(0).url : "";
        }

        public String getUrl() {
            return main_mv_urls != null ? main_mv_urls.get(0).url : "";
        }

        public float getHeight() {
            return ext_params != null ? ext_params.h : -2;
        }
    }

    public static class HeadurlsData {
        public String cdn;
        public String url;
        public String urlPattern;
    }

    public static class AudioUrlsData {
        public String cdn;
        public String url;
    }
}
