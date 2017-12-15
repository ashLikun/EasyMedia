package com.ashlikun.media.simple.data;

import android.text.TextUtils;

import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/13　17:23
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class NeiHanData {
    public Group group;
    public int type;

    public String getVideoUrl() {
        if (group != null) {
            if (!TextUtils.isEmpty(group._$360p_video.getUri())) {
                return group._$360p_video.getUri();
            } else if (!TextUtils.isEmpty(group._$480p_video.getUri())) {
                return group._$480p_video.getUri();
            } else if (!TextUtils.isEmpty(group._$720p_video.getUri())) {
                return group._$720p_video.getUri();
            } else if (!TextUtils.isEmpty(group.getMp4_url())) {
                return group.getMp4_url();
            }
        }
        return "";
    }

    public String getText() {
        if (group != null) {
            return group.text;
        }
        return "";
    }

    public int getHeigth() {
        if (group != null) {
            if (group._$360p_video.getHeight() > 0) {
                return group._$360p_video.getHeight();
            } else if (group._$480p_video.getHeight() > 0) {
                return group._$480p_video.getHeight();
            } else if (group._$720p_video.getHeight() > 0) {
                return group._$720p_video.getHeight();
            } else if (group.getVideo_height() > 0) {
                return group.getVideo_height();
            }
        }
        return 0;
    }

    public int getWidth() {
        if (group != null) {
            if (group._$360p_video.getWidth() > 0) {
                return group._$360p_video.getWidth();
            } else if (group._$480p_video.getWidth() > 0) {
                return group._$480p_video.getWidth();
            } else if (group._$720p_video.getWidth() > 0) {
                return group._$720p_video.getWidth();
            } else if (group.getVideo_wh_ratio() > 0) {
                return group.getVideo_wh_ratio();
            }
        }
        return 0;
    }

    public String getImageUrl() {
        if (group != null && group.getLarge_cover() != null) {
            return group.getLarge_cover().getUri();
        }
        return "";
    }

    public static class Group {
        private String video_id;
        @com.google.gson.annotations.SerializedName("360p_video")
        private _$360pVideoBean _$360p_video;
        private String mp4_url;
        private String text;
        @com.google.gson.annotations.SerializedName("720p_video")
        private _$720pVideoBean _$720p_video;
        private double duration;
        @com.google.gson.annotations.SerializedName("480p_video")
        private _$480pVideoBean _$480p_video;
        private int create_time;
        private long id;
        private LargeCoverBean large_cover;
        private int video_wh_ratio;
        private String download_url;
        private int video_height;
        private int video_width;

        public static class _$360pVideoBean {
            /**
             * width : 480
             * url_list : [{"url":"http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/","expires":1513157167},{"url":"http://ic.snssdk.com/neihan/video/playback/1513156567.83/?video_id=d4a7221567104b2db919a78cc8631f23&quality=360p&line=1&is_gif=0&device_platform=android"}]
             * uri : 360p/d4a7221567104b2db919a78cc8631f23
             * height : 854
             */

            private int width;
            private int height;
            private java.util.List<UrlListBean> url_list;

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public String getUri() {
                if (url_list == null || url_list.size() == 0) {
                    return "";
                }
                return url_list.get(0).getUrl();
            }


            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public List<UrlListBean> getUrl_list() {
                return url_list;
            }

            public void setUrl_list(List<UrlListBean> url_list) {
                this.url_list = url_list;
            }

            public static class UrlListBean {
                /**
                 * url : http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/
                 * expires : 1513157167
                 */

                private String url;
                private int expires;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public int getExpires() {
                    return expires;
                }

                public void setExpires(int expires) {
                    this.expires = expires;
                }
            }
        }

        public static class _$720pVideoBean {
            /**
             * width : 480
             * url_list : [{"url":"http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/","expires":1513157167},{"url":"http://ic.snssdk.com/neihan/video/playback/1513156567.83/?video_id=d4a7221567104b2db919a78cc8631f23&quality=720p&line=1&is_gif=0&device_platform=android"}]
             * uri : 720p/d4a7221567104b2db919a78cc8631f23
             * height : 854
             */

            private int width;
            private String uri;
            private int height;
            private java.util.List<UrlListBeanX> url_list;

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public List<UrlListBeanX> getUrl_list() {
                return url_list;
            }

            public void setUrl_list(List<UrlListBeanX> url_list) {
                this.url_list = url_list;
            }

            public static class UrlListBeanX {
                /**
                 * url : http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/
                 * expires : 1513157167
                 */

                private String url;
                private int expires;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public int getExpires() {
                    return expires;
                }

                public void setExpires(int expires) {
                    this.expires = expires;
                }
            }
        }

        public static class _$480pVideoBean {
            /**
             * width : 480
             * url_list : [{"url":"http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/","expires":1513157167},{"url":"http://ic.snssdk.com/neihan/video/playback/1513156567.83/?video_id=d4a7221567104b2db919a78cc8631f23&quality=480p&line=1&is_gif=0&device_platform=android"}]
             * uri : 480p/d4a7221567104b2db919a78cc8631f23
             * height : 854
             */

            private int width;
            private String uri;
            private int height;
            private java.util.List<UrlListBeanXX> url_list;

            public int getWidth() {
                return width;
            }

            public void setWidth(int width) {
                this.width = width;
            }

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }

            public int getHeight() {
                return height;
            }

            public void setHeight(int height) {
                this.height = height;
            }

            public List<UrlListBeanXX> getUrl_list() {
                return url_list;
            }

            public void setUrl_list(List<UrlListBeanXX> url_list) {
                this.url_list = url_list;
            }

            public static class UrlListBeanXX {
                /**
                 * url : http://v3-nh.ixigua.com/8f64cfaf6e4c7f74c5c1adc2740117b3/5a30efd7/video/m/22011118f6f2192441e8c0aebbe98484adc1152c8880000aa739de4f296/
                 * expires : 1513157167
                 */

                private String url;
                private int expires;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public int getExpires() {
                    return expires;
                }

                public void setExpires(int expires) {
                    this.expires = expires;
                }
            }
        }


        public static class LargeCoverBean {
            /**
             * url_list : [{"url":"http://p3.pstatp.com/large/4c2100134d006e2a0172.webp"},{"url":"http://pb9.pstatp.com/large/4c2100134d006e2a0172.webp"},{"url":"http://pb1.pstatp.com/large/4c2100134d006e2a0172.webp"}]
             * uri : large/4c2100134d006e2a0172
             */

            private String uri;
            private java.util.List<UrlListBeanXXX> url_list;

            public String getUri() {
                return uri;
            }

            public void setUri(String uri) {
                this.uri = uri;
            }

            public List<UrlListBeanXXX> getUrl_list() {
                return url_list;
            }

            public void setUrl_list(List<UrlListBeanXXX> url_list) {
                this.url_list = url_list;
            }

            public static class UrlListBeanXXX {
                /**
                 * url : http://p3.pstatp.com/large/4c2100134d006e2a0172.webp
                 */

                private String url;

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }
            }
        }

        public String getVideo_id() {
            return video_id;
        }

        public _$360pVideoBean get_$360p_video() {
            return _$360p_video;
        }

        public String getMp4_url() {
            return mp4_url;
        }

        public String getText() {
            return text;
        }

        public _$720pVideoBean get_$720p_video() {
            return _$720p_video;
        }

        public double getDuration() {
            return duration;
        }

        public _$480pVideoBean get_$480p_video() {
            return _$480p_video;
        }

        public int getCreate_time() {
            return create_time;
        }

        public long getId() {
            return id;
        }

        public LargeCoverBean getLarge_cover() {
            return large_cover;
        }

        public int getVideo_wh_ratio() {
            return video_wh_ratio;
        }

        public String getDownload_url() {
            return download_url;
        }

        public int getVideo_height() {
            return video_height;
        }

        public int getVideo_width() {
            return video_width;
        }
    }
}
