package com.ashlikun.media.simple.data;

import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2020/4/20　17:48
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */
public class DouyingData {

    public int status_code;
    public ExtraData extra;
    public List<AwemeListData> aweme_list;

    public static class ExtraData {
        public long now;
        public Object fatal_item_ids;
    }


    public static class AwemeListData {
        public String aweme_id;
        public String desc;
        public int create_time;
        public AuthorData author;
        public MusicData music;
        public VideoData video;
        public String share_url;
        public int user_digged;
        public StatisticsData statistics;
        public StatusData status;
        public int rate;
        public int is_top;
        public LabelTopData label_top;
        public ShareInfoDataX share_info;
        public String distance;
        public boolean is_vr;
        public boolean is_ads;
        public int duration;
        public int aweme_type;
        public boolean is_fantasy;
        public boolean cmt_swt;
        public Object image_infos;
        public RiskInfosData risk_infos;
        public boolean is_relieve;
        public String sort_label;
        public Object position;
        public Object uniqid_position;
        public Object comment_list;
        public long author_user_id;
        public int bodydance_score;
        public int is_hash_tag;
        public boolean is_pgcshow;
        public String region;
        public int vr_type;
        public int collect_stat;
        public Object label_top_text;
        public String group_id;
        public boolean prevent_download;
        public Object nickname_position;
        public Object challenge_position;
        public int item_comment_settings;
        public DescendantsData descendants;
        public boolean with_promotional_music;
        public XiguaTaskData xigua_task;
        public Object long_video;
        public int item_duet;
        public int item_react;
        public String desc_language;
        public Object interaction_stickers;
        public int ad_link_type;
        public String misc_info;
        public Object origin_comment_ids;
        public Object commerce_config_data;
        public boolean enable_top_view;
        public int distribute_type;
        public VideoControlData video_control;
        public AwemeControlData aweme_control;
        public Object anchors;
        public Object hybrid_label;
        public PoiInfoData poi_info;
        public List<ChaListData> cha_list;


        public String getUrl() {
            if (video != null) {
                try {
                    if (video.dynamic_cover != null) {
                        return video.dynamic_cover.url_list.get(0);
                    }else if (video.play_addr != null) {
                        return video.play_addr.url_list.get(0);
                    } else if (video.play_addr_lowbr != null) {
                        return video.play_addr_lowbr.url_list.get(0);
                    } else if (video.download_addr != null) {
                        return video.download_addr.url_list.get(0);
                    }
                } catch (IndexOutOfBoundsException e) {

                }
            }
            return "";
        }

        public String getText() {
            return desc;
        }

        public static class AuthorData {
            public String uid;
            public String short_id;
            public String nickname;
            public int gender;
            public String signature;
            public AvatarLargerData avatar_larger;
            public AvatarThumbData avatar_thumb;
            public AvatarMediumData avatar_medium;
            public String birthday;
            public boolean is_verified;
            public int follow_status;
            public int aweme_count;
            public int following_count;
            public int follower_count;
            public int favoriting_count;
            public int total_favorited;
            public boolean is_block;
            public boolean hide_search;
            public int constellation;
            public String location;
            public boolean hide_location;
            public String weibo_verify;
            public String custom_verify;
            public String unique_id;
            public String bind_phone;
            public int special_lock;
            public int need_recommend;
            public boolean is_binded_weibo;
            public String weibo_name;
            public String weibo_schema;
            public String weibo_url;
            public boolean story_open;
            public int story_count;
            public boolean has_facebook_token;
            public boolean has_twitter_token;
            public int fb_expire_time;
            public int tw_expire_time;
            public boolean has_youtube_token;
            public int youtube_expire_time;
            public long room_id;
            public int live_verify;
            public int authority_status;
            public String verify_info;
            public int shield_follow_notice;
            public int shield_digg_notice;
            public int shield_comment_notice;
            public String school_name;
            public String school_poi_id;
            public int school_type;
            public ShareInfoData share_info;
            public boolean with_commerce_entry;
            public int verification_type;
            public String enterprise_verify_reason;
            public boolean is_ad_fake;
            public Object followers_detail;
            public String region;
            public String account_region;
            public int sync_to_toutiao;
            public int commerce_user_level;
            public int live_agreement;
            public Object platform_sync_info;
            public boolean with_shop_entry;
            public boolean is_discipline_member;
            public int secret;
            public boolean has_orders;
            public boolean prevent_download;
            public boolean show_image_bubble;
            public int unique_id_modify_time;
            public VideoIconData video_icon;
            public String ins_id;
            public String google_account;
            public String youtube_channel_id;
            public String youtube_channel_title;
            public int apple_account;
            public boolean with_dou_entry;
            public boolean with_fusion_shop_entry;
            public boolean is_phone_binded;
            public boolean accept_private_policy;
            public String twitter_id;
            public String twitter_name;
            public boolean user_canceled;
            public boolean has_email;
            public boolean is_gov_media_vip;
            public int live_agreement_time;
            public int status;
            public int create_time;
            public String avatar_uri;
            public int follower_status;
            public int neiguang_shield;
            public int comment_setting;
            public int duet_setting;
            public int reflow_page_gid;
            public int reflow_page_uid;
            public int user_rate;
            public int download_setting;
            public int download_prompt_ts;
            public int react_setting;
            public boolean live_commerce;
            public String language;
            public boolean has_insights;
            public String share_qrcode_uri;
            public Object item_list;
            public int user_mode;
            public int user_period;
            public boolean has_unread_story;
            public Object new_story_cover;
            public boolean is_star;
            public String cv_level;
            public Object ad_cover_url;
            public int comment_filter_status;
            public Avatar168x168Data avatar_168x168;
            public Avatar300x300Data avatar_300x300;
            public Object relative_users;
            public Object cha_list;
            public String sec_uid;
            public Object need_points;
            public Object homepage_bottom_toast;
            public List<?> geofencing;
            public List<CoverUrlData> cover_url;
            public List<?> type_label;

            public static class AvatarLargerData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class AvatarThumbData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class AvatarMediumData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class ShareInfoData {
                public String share_url;
                public String share_weibo_desc;
                public String share_desc;
                public String share_title;
                public ShareQrcodeUrlData share_qrcode_url;
                public String share_title_myself;
                public String share_title_other;

                public static class ShareQrcodeUrlData {
                    public String uri;
                    public int width;
                    public int height;
                    public List<String> url_list;
                }
            }

            public static class VideoIconData {
                public String uri;
                public int width;
                public int height;
                public List<?> url_list;
            }

            public static class Avatar168x168Data {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class Avatar300x300Data {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class CoverUrlData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }
        }

        public static class MusicData {
            public long id;
            public String id_str;
            public String title;
            public String author;
            public String album;
            public CoverHdData cover_hd;
            public CoverLargeData cover_large;
            public CoverMediumData cover_medium;
            public CoverThumbData cover_thumb;
            public PlayUrlData play_url;
            public String schema_url;
            public int source_platform;
            public int start_time;
            public int end_time;
            public int duration;
            public String extra;
            public int user_count;
            public Object position;
            public int collect_stat;
            public int status;
            public String offline_desc;
            public String owner_id;
            public String owner_nickname;
            public boolean is_original;
            public String mid;
            public int binded_challenge_id;
            public boolean redirect;
            public boolean is_restricted;
            public boolean author_deleted;
            public boolean is_del_video;
            public boolean is_video_self_see;
            public String owner_handle;
            public Object author_position;
            public boolean prevent_download;
            public Object unshelve_countries;
            public int prevent_item_download_status;
            public String sec_uid;
            public AvatarThumbDataX avatar_thumb;
            public AvatarMediumDataX avatar_medium;
            public AvatarLargeData avatar_large;
            public int preview_start_time;
            public int preview_end_time;
            public boolean is_commerce_music;
            public boolean is_original_sound;
            public Object artists;
            public Object lyric_short_position;
            public boolean mute_share;
            public Object tag_list;
            public boolean is_author_artist;
            public boolean is_pgc;
            public List<?> external_song_info;

            public static class CoverHdData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class CoverLargeData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class CoverMediumData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class CoverThumbData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class PlayUrlData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class AvatarThumbDataX {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class AvatarMediumDataX {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class AvatarLargeData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }
        }

        public static class VideoData {
            public PlayAddrData play_addr;
            public CoverData cover;
            public int height;
            public int width;
            public DynamicCoverData dynamic_cover;
            public OriginCoverData origin_cover;
            public String ratio;
            public DownloadAddrData download_addr;
            public boolean has_watermark;
            public PlayAddrLowbrData play_addr_lowbr;
            public int duration;
            public DownloadSuffixLogoAddrData download_suffix_logo_addr;
            public boolean has_download_suffix_logo_addr;
            public int is_h265;
            public int cdn_url_expired;
            public List<BitRateData> bit_rate;

            public static class PlayAddrData {
                public String uri;
                public int width;
                public int height;
                public String url_key;
                public int data_size;
                public List<String> url_list;
            }

            public static class CoverData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class DynamicCoverData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class OriginCoverData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class DownloadAddrData {
                public String uri;
                public int width;
                public int height;
                public int data_size;
                public List<String> url_list;
            }

            public static class PlayAddrLowbrData {
                public String uri;
                public int width;
                public int height;
                public String url_key;
                public int data_size;
                public List<String> url_list;
            }

            public static class DownloadSuffixLogoAddrData {
                public String uri;
                public int width;
                public int height;
                public int data_size;
                public List<String> url_list;
            }

            public static class BitRateData {
                public String gear_name;
                public int quality_type;
                public int bit_rate;
                public PlayAddrDataX play_addr;
                public int is_h265;

                public static class PlayAddrDataX {
                    public String uri;
                    public int width;
                    public int height;
                    public String url_key;
                    public int data_size;
                    public List<String> url_list;
                }
            }
        }

        public static class StatisticsData {
            public String aweme_id;
            public int comment_count;
            public int digg_count;
            public int download_count;
            public int play_count;
            public int share_count;
            public int forward_count;
            public int lose_count;
            public int lose_comment_count;
            public int whatsapp_share_count;
        }

        public static class StatusData {
            public String aweme_id;
            public boolean is_delete;
            public boolean allow_share;
            public boolean allow_comment;
            public boolean is_private;
            public boolean with_goods;
            public int private_status;
            public boolean with_fusion_goods;
            public boolean in_reviewing;
            public int reviewed;
            public boolean self_see;
            public boolean is_prohibited;
            public int download_status;
        }

        public static class LabelTopData {
            public String uri;
            public int width;
            public int height;
            public List<String> url_list;
        }

        public static class ShareInfoDataX {
            public String share_url;
            public String share_weibo_desc;
            public String share_desc;
            public String share_title;
            public int bool_persist;
            public String share_title_myself;
            public String share_title_other;
            public String share_link_desc;
            public String share_signature_url;
            public String share_signature_desc;
            public String share_quote;
        }

        public static class RiskInfosData {
            public boolean vote;
            public boolean warn;
            public boolean risk_sink;
            public int type;
            public String content;
        }

        public static class DescendantsData {
            public String notify_msg;
            public List<String> platforms;
        }

        public static class XiguaTaskData {
            public boolean is_xigua_task;
        }

        public static class VideoControlData {
            public boolean allow_download;
            public int share_type;
            public int show_progress_bar;
            public int draft_progress_bar;
            public boolean allow_duet;
            public boolean allow_react;
            public int prevent_download_type;
            public boolean allow_dynamic_wallpaper;
            public int timer_status;
        }

        public static class AwemeControlData {
            public boolean can_forward;
            public boolean can_share;
            public boolean can_comment;
            public boolean can_show_comment;
        }

        public static class PoiInfoData {
            public String poi_id;
            public String poi_name;
            public String type_code;
            public int user_count;
            public int item_count;
            public ShareInfoDataX share_info;
            public CoverHdDataX cover_hd;
            public CoverLargeDataX cover_large;
            public CoverMediumDataX cover_medium;
            public CoverThumbDataX cover_thumb;
            public String distance;
            public AddressInfoData address_info;
            public int icon_type;
            public int collect_stat;
            public double poi_longitude;
            public double poi_latitude;
            public int expand_type;
            public IconOnMapData icon_on_map;
            public IconOnEntryData icon_on_entry;
            public IconOnInfoData icon_on_info;
            public int show_type;
            public int poi_subtitle_type;
            public Object voucher_release_areas;
            public Object poi_frontend_type;
            public PoiBackendTypeData poi_backend_type;
            public boolean is_admin_area;
            public String view_count;
            public Object icon_service_type_list;

            public static class CoverHdDataX {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class CoverLargeDataX {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class CoverMediumDataX {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class CoverThumbDataX {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class AddressInfoData {
                public String province;
                public String city;
                public String district;
                public String address;
                public String simple_addr;
                public String city_code;
                public String country;
                public String country_code;
            }

            public static class IconOnMapData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class IconOnEntryData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class IconOnInfoData {
                public String uri;
                public int width;
                public int height;
                public List<String> url_list;
            }

            public static class PoiBackendTypeData {
                public String code;
                public String name;
            }
        }

        public static class ChaListData {
            public String cid;
            public String cha_name;
            public String desc;
            public String schema;
            public AuthorDataX author;
            public int user_count;
            public ShareInfoDataX share_info;
            public int type;
            public int sub_type;
            public boolean is_pgcshow;
            public int collect_stat;
            public int is_challenge;
            public int view_count;
            public boolean is_commerce;
            public String hashtag_profile;
            public Object cha_attrs;
            public Object banner_list;
            public List<?> connect_music;

            public static class AuthorDataX {
                public Object followers_detail;
                public Object platform_sync_info;
                public Object geofencing;
                public Object cover_url;
                public Object item_list;
                public Object new_story_cover;
                public Object type_label;
                public Object ad_cover_url;
                public Object relative_users;
                public Object cha_list;
                public Object need_points;
                public Object homepage_bottom_toast;
            }
        }
    }
}
