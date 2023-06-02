package com.ashlikun.media.simple.data

/**
 * 作者　　: 李坤
 * 创建时间: 2020/4/20　17:48
 * 邮箱　　：496546144@qq.com
 *
 *
 * 功能介绍：
 */
class DouyingData {
    var status_code = 0
    var extra: ExtraData? = null
    var aweme_list: List<AwemeListData>? = null

    class ExtraData {
        var now: Long = 0
        var fatal_item_ids: Any? = null
    }

    class AwemeListData {
        var aweme_id: String? = null
        var desc: String? = null
        var create_time = 0
        var author: AuthorData? = null
        var music: MusicData? = null
        var video: VideoData? = null
        var share_url: String? = null
        var user_digged = 0
        var statistics: StatisticsData? = null
        var status: StatusData? = null
        var rate = 0
        var is_top = 0
        var label_top: LabelTopData? = null
        var share_info: ShareInfoDataX? = null
        var distance: String? = null
        var is_vr = false
        var is_ads = false
        var duration = 0
        var aweme_type = 0
        var is_fantasy = false
        var cmt_swt = false
        var image_infos: Any? = null
        var risk_infos: RiskInfosData? = null
        var is_relieve = false
        var sort_label: String? = null
        var position: Any? = null
        var uniqid_position: Any? = null
        var comment_list: Any? = null
        var author_user_id: Long = 0
        var bodydance_score = 0
        var is_hash_tag = 0
        var is_pgcshow = false
        var region: String? = null
        var vr_type = 0
        var collect_stat = 0
        var label_top_text: Any? = null
        var group_id: String? = null
        var prevent_download = false
        var nickname_position: Any? = null
        var challenge_position: Any? = null
        var item_comment_settings = 0
        var descendants: DescendantsData? = null
        var with_promotional_music = false
        var xigua_task: XiguaTaskData? = null
        var long_video: Any? = null
        var item_duet = 0
        var item_react = 0
        var desc_language: String? = null
        var interaction_stickers: Any? = null
        var ad_link_type = 0
        var misc_info: String? = null
        var origin_comment_ids: Any? = null
        var commerce_config_data: Any? = null
        var enable_top_view = false
        var distribute_type = 0
        var video_control: VideoControlData? = null
        var aweme_control: AwemeControlData? = null
        var anchors: Any? = null
        var hybrid_label: Any? = null
        var poi_info: PoiInfoData? = null
        var cha_list: List<ChaListData>? = null
        val url: String
            get() {
                if (video != null) {
                    try {
                        if (video!!.dynamic_cover != null) {
                            return video!!.dynamic_cover!!.url_list!![0]
                        } else if (video!!.play_addr != null) {
                            return video!!.play_addr!!.url_list!![0]
                        } else if (video!!.play_addr_lowbr != null) {
                            return video!!.play_addr_lowbr!!.url_list!![0]
                        } else if (video!!.download_addr != null) {
                            return video!!.download_addr!!.url_list!![0]
                        }
                    } catch (e: IndexOutOfBoundsException) {
                    }
                }
                return ""
            }

        class AuthorData {
            var uid: String? = null
            var short_id: String? = null
            var nickname: String? = null
            var gender = 0
            var signature: String? = null
            var avatar_larger: AvatarLargerData? = null
            var avatar_thumb: AvatarThumbData? = null
            var avatar_medium: AvatarMediumData? = null
            var birthday: String? = null
            var is_verified = false
            var follow_status = 0
            var aweme_count = 0
            var following_count = 0
            var follower_count = 0
            var favoriting_count = 0
            var total_favorited = 0
            var is_block = false
            var hide_search = false
            var constellation = 0
            var location: String? = null
            var hide_location = false
            var weibo_verify: String? = null
            var custom_verify: String? = null
            var unique_id: String? = null
            var bind_phone: String? = null
            var special_lock = 0
            var need_recommend = 0
            var is_binded_weibo = false
            var weibo_name: String? = null
            var weibo_schema: String? = null
            var weibo_url: String? = null
            var story_open = false
            var story_count = 0
            var has_facebook_token = false
            var has_twitter_token = false
            var fb_expire_time = 0
            var tw_expire_time = 0
            var has_youtube_token = false
            var youtube_expire_time = 0
            var room_id: Long = 0
            var live_verify = 0
            var authority_status = 0
            var verify_info: String? = null
            var shield_follow_notice = 0
            var shield_digg_notice = 0
            var shield_comment_notice = 0
            var school_name: String? = null
            var school_poi_id: String? = null
            var school_type = 0
            var share_info: ShareInfoData? = null
            var with_commerce_entry = false
            var verification_type = 0
            var enterprise_verify_reason: String? = null
            var is_ad_fake = false
            var followers_detail: Any? = null
            var region: String? = null
            var account_region: String? = null
            var sync_to_toutiao = 0
            var commerce_user_level = 0
            var live_agreement = 0
            var platform_sync_info: Any? = null
            var with_shop_entry = false
            var is_discipline_member = false
            var secret = 0
            var has_orders = false
            var prevent_download = false
            var show_image_bubble = false
            var unique_id_modify_time = 0
            var video_icon: VideoIconData? = null
            var ins_id: String? = null
            var google_account: String? = null
            var youtube_channel_id: String? = null
            var youtube_channel_title: String? = null
            var apple_account = 0
            var with_dou_entry = false
            var with_fusion_shop_entry = false
            var is_phone_binded = false
            var accept_private_policy = false
            var twitter_id: String? = null
            var twitter_name: String? = null
            var user_canceled = false
            var has_email = false
            var is_gov_media_vip = false
            var live_agreement_time = 0
            var status = 0
            var create_time = 0
            var avatar_uri: String? = null
            var follower_status = 0
            var neiguang_shield = 0
            var comment_setting = 0
            var duet_setting = 0
            var reflow_page_gid = 0
            var reflow_page_uid = 0
            var user_rate = 0
            var download_setting = 0
            var download_prompt_ts = 0
            var react_setting = 0
            var live_commerce = false
            var language: String? = null
            var has_insights = false
            var share_qrcode_uri: String? = null
            var item_list: Any? = null
            var user_mode = 0
            var user_period = 0
            var has_unread_story = false
            var new_story_cover: Any? = null
            var is_star = false
            var cv_level: String? = null
            var ad_cover_url: Any? = null
            var comment_filter_status = 0
            var avatar_168x168: Avatar168x168Data? = null
            var avatar_300x300: Avatar300x300Data? = null
            var relative_users: Any? = null
            var cha_list: Any? = null
            var sec_uid: String? = null
            var need_points: Any? = null
            var homepage_bottom_toast: Any? = null
            var geofencing: List<*>? = null
            var cover_url: List<CoverUrlData>? = null
            var type_label: List<*>? = null

            class AvatarLargerData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class AvatarThumbData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class AvatarMediumData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class ShareInfoData {
                var share_url: String? = null
                var share_weibo_desc: String? = null
                var share_desc: String? = null
                var share_title: String? = null
                var share_qrcode_url: ShareQrcodeUrlData? = null
                var share_title_myself: String? = null
                var share_title_other: String? = null

                class ShareQrcodeUrlData {
                    var uri: String? = null
                    var width = 0
                    var height = 0
                    var url_list: List<String>? = null
                }
            }

            class VideoIconData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<*>? = null
            }

            class Avatar168x168Data {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class Avatar300x300Data {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class CoverUrlData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }
        }

        class MusicData {
            var id: Long = 0
            var id_str: String? = null
            var title: String? = null
            var author: String? = null
            var album: String? = null
            var cover_hd: CoverHdData? = null
            var cover_large: CoverLargeData? = null
            var cover_medium: CoverMediumData? = null
            var cover_thumb: CoverThumbData? = null
            var play_url: PlayUrlData? = null
            var schema_url: String? = null
            var source_platform = 0
            var start_time = 0
            var end_time = 0
            var duration = 0
            var extra: String? = null
            var user_count = 0
            var position: Any? = null
            var collect_stat = 0
            var status = 0
            var offline_desc: String? = null
            var owner_id: String? = null
            var owner_nickname: String? = null
            var is_original = false
            var mid: String? = null
            var binded_challenge_id = 0
            var redirect = false
            var is_restricted = false
            var author_deleted = false
            var is_del_video = false
            var is_video_self_see = false
            var owner_handle: String? = null
            var author_position: Any? = null
            var prevent_download = false
            var unshelve_countries: Any? = null
            var prevent_item_download_status = 0
            var sec_uid: String? = null
            var avatar_thumb: AvatarThumbDataX? = null
            var avatar_medium: AvatarMediumDataX? = null
            var avatar_large: AvatarLargeData? = null
            var preview_start_time = 0
            var preview_end_time = 0
            var is_commerce_music = false
            var is_original_sound = false
            var artists: Any? = null
            var lyric_short_position: Any? = null
            var mute_share = false
            var tag_list: Any? = null
            var is_author_artist = false
            var is_pgc = false
            var external_song_info: List<*>? = null

            class CoverHdData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class CoverLargeData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class CoverMediumData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class CoverThumbData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class PlayUrlData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class AvatarThumbDataX {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class AvatarMediumDataX {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class AvatarLargeData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }
        }

        class VideoData {
            var play_addr: PlayAddrData? = null
            var cover: CoverData? = null
            var height = 0
            var width = 0
            var dynamic_cover: DynamicCoverData? = null
            var origin_cover: OriginCoverData? = null
            var ratio: String? = null
            var download_addr: DownloadAddrData? = null
            var has_watermark = false
            var play_addr_lowbr: PlayAddrLowbrData? = null
            var duration = 0
            var download_suffix_logo_addr: DownloadSuffixLogoAddrData? = null
            var has_download_suffix_logo_addr = false
            var is_h265 = 0
            var cdn_url_expired = 0
            var bit_rate: List<BitRateData>? = null

            class PlayAddrData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_key: String? = null
                var data_size = 0
                var url_list: List<String>? = null
            }

            class CoverData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class DynamicCoverData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class OriginCoverData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class DownloadAddrData {
                var uri: String? = null
                var width = 0
                var height = 0
                var data_size = 0
                var url_list: List<String>? = null
            }

            class PlayAddrLowbrData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_key: String? = null
                var data_size = 0
                var url_list: List<String>? = null
            }

            class DownloadSuffixLogoAddrData {
                var uri: String? = null
                var width = 0
                var height = 0
                var data_size = 0
                var url_list: List<String>? = null
            }

            class BitRateData {
                var gear_name: String? = null
                var quality_type = 0
                var bit_rate = 0
                var play_addr: PlayAddrDataX? = null
                var is_h265 = 0

                class PlayAddrDataX {
                    var uri: String? = null
                    var width = 0
                    var height = 0
                    var url_key: String? = null
                    var data_size = 0
                    var url_list: List<String>? = null
                }
            }
        }

        class StatisticsData {
            var aweme_id: String? = null
            var comment_count = 0
            var digg_count = 0
            var download_count = 0
            var play_count = 0
            var share_count = 0
            var forward_count = 0
            var lose_count = 0
            var lose_comment_count = 0
            var whatsapp_share_count = 0
        }

        class StatusData {
            var aweme_id: String? = null
            var is_delete = false
            var allow_share = false
            var allow_comment = false
            var is_private = false
            var with_goods = false
            var private_status = 0
            var with_fusion_goods = false
            var in_reviewing = false
            var reviewed = 0
            var self_see = false
            var is_prohibited = false
            var download_status = 0
        }

        class LabelTopData {
            var uri: String? = null
            var width = 0
            var height = 0
            var url_list: List<String>? = null
        }

        class ShareInfoDataX {
            var share_url: String? = null
            var share_weibo_desc: String? = null
            var share_desc: String? = null
            var share_title: String? = null
            var bool_persist = 0
            var share_title_myself: String? = null
            var share_title_other: String? = null
            var share_link_desc: String? = null
            var share_signature_url: String? = null
            var share_signature_desc: String? = null
            var share_quote: String? = null
        }

        class RiskInfosData {
            var vote = false
            var warn = false
            var risk_sink = false
            var type = 0
            var content: String? = null
        }

        class DescendantsData {
            var notify_msg: String? = null
            var platforms: List<String>? = null
        }

        class XiguaTaskData {
            var is_xigua_task = false
        }

        class VideoControlData {
            var allow_download = false
            var share_type = 0
            var show_progress_bar = 0
            var draft_progress_bar = 0
            var allow_duet = false
            var allow_react = false
            var prevent_download_type = 0
            var allow_dynamic_wallpaper = false
            var timer_status = 0
        }

        class AwemeControlData {
            var can_forward = false
            var can_share = false
            var can_comment = false
            var can_show_comment = false
        }

        class PoiInfoData {
            var poi_id: String? = null
            var poi_name: String? = null
            var type_code: String? = null
            var user_count = 0
            var item_count = 0
            var share_info: ShareInfoDataX? = null
            var cover_hd: CoverHdDataX? = null
            var cover_large: CoverLargeDataX? = null
            var cover_medium: CoverMediumDataX? = null
            var cover_thumb: CoverThumbDataX? = null
            var distance: String? = null
            var address_info: AddressInfoData? = null
            var icon_type = 0
            var collect_stat = 0
            var poi_longitude = 0.0
            var poi_latitude = 0.0
            var expand_type = 0
            var icon_on_map: IconOnMapData? = null
            var icon_on_entry: IconOnEntryData? = null
            var icon_on_info: IconOnInfoData? = null
            var show_type = 0
            var poi_subtitle_type = 0
            var voucher_release_areas: Any? = null
            var poi_frontend_type: Any? = null
            var poi_backend_type: PoiBackendTypeData? = null
            var is_admin_area = false
            var view_count: String? = null
            var icon_service_type_list: Any? = null

            class CoverHdDataX {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class CoverLargeDataX {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class CoverMediumDataX {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class CoverThumbDataX {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class AddressInfoData {
                var province: String? = null
                var city: String? = null
                var district: String? = null
                var address: String? = null
                var simple_addr: String? = null
                var city_code: String? = null
                var country: String? = null
                var country_code: String? = null
            }

            class IconOnMapData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class IconOnEntryData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class IconOnInfoData {
                var uri: String? = null
                var width = 0
                var height = 0
                var url_list: List<String>? = null
            }

            class PoiBackendTypeData {
                var code: String? = null
                var name: String? = null
            }
        }

        class ChaListData {
            var cid: String? = null
            var cha_name: String? = null
            var desc: String? = null
            var schema: String? = null
            var author: AuthorDataX? = null
            var user_count = 0
            var share_info: ShareInfoDataX? = null
            var type = 0
            var sub_type = 0
            var is_pgcshow = false
            var collect_stat = 0
            var is_challenge = 0
            var view_count = 0
            var is_commerce = false
            var hashtag_profile: String? = null
            var cha_attrs: Any? = null
            var banner_list: Any? = null
            var connect_music: List<*>? = null

            class AuthorDataX {
                var followers_detail: Any? = null
                var platform_sync_info: Any? = null
                var geofencing: Any? = null
                var cover_url: Any? = null
                var item_list: Any? = null
                var new_story_cover: Any? = null
                var type_label: Any? = null
                var ad_cover_url: Any? = null
                var relative_users: Any? = null
                var cha_list: Any? = null
                var need_points: Any? = null
                var homepage_bottom_toast: Any? = null
            }
        }
    }
}