/*
 * ff_ffplaye_options.h
 *
 * Copyright (c) 2015 Bilibili
 * Copyright (c) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * This file is part of ijkPlayer.
 *
 * ijkPlayer is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * ijkPlayer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with ijkPlayer; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

#ifndef FFPLAY__FF_FFPLAY_OPTIONS_H
#define FFPLAY__FF_FFPLAY_OPTIONS_H

#define OPTION_OFFSET(x) offsetof(FFPlayer, x)
#define OPTION_INT(default__, min__, max__) \
    .type = AV_OPT_TYPE_INT, \
    { .i64 = default__ }, \
    .min = min__, \
    .max = max__, \
    .flags = AV_OPT_FLAG_DECODING_PARAM
#define OPTION_INT64(default__, min__, max__) \
    .type = AV_OPT_TYPE_INT64, \
    { .i64 = default__ }, \
    .min = min__, \
    .max = max__, \
    .flags = AV_OPT_FLAG_DECODING_PARAM
#define OPTION_DOUBLE(default__, min__, max__) \
    .type = AV_OPT_TYPE_DOUBLE, \
    { .dbl = default__ }, \
    .min = min__, \
    .max = max__, \
    .flags = AV_OPT_FLAG_DECODING_PARAM
#define OPTION_CONST(default__) \
    .type = AV_OPT_TYPE_CONST, \
    { .i64 = default__ }, \
    .min = INT_MIN, \
    .max = INT_MAX, \
    .flags = AV_OPT_FLAG_DECODING_PARAM

#define OPTION_STR(default__) \
    .type = AV_OPT_TYPE_STRING, \
    { .str = default__ }, \
    .min = 0, \
    .max = 0, \
    .flags = AV_OPT_FLAG_DECODING_PARAM

static const AVOption ffp_context_options[] = {
    // original options in ffplay.c
    // FFP_MERGE: x, y, s, fs
    { "an",                             "禁用音频",
        OPTION_OFFSET(audio_disable),   OPTION_INT(0, 0, 1) },
    { "vn",                             "禁用视频",
        OPTION_OFFSET(video_disable),   OPTION_INT(0, 0, 1) },
    // FFP_MERGE: sn, ast, vst, sst
    // TODO: ss
    { "nodisp",                         "禁用图形显示",
        OPTION_OFFSET(display_disable), OPTION_INT(0, 0, 1) },
    { "volume",                         "设置启动音量0=最小值100=最大值",
        OPTION_OFFSET(startup_volume),   OPTION_INT(100, 0, 100) },
    // FFP_MERGE: f, pix_fmt, stats
    { "fast",                           "不符合规范的优化",
        OPTION_OFFSET(fast),            OPTION_INT(0, 0, 1) },
    // FFP_MERGE: genpts, drp, lowres, sync, autoexit, exitonkeydown, exitonmousedown
    { "loop",                           "设置回放循环次数",
        OPTION_OFFSET(loop),            OPTION_INT(1, INT_MIN, INT_MAX) },
    { "infbuf",                         "不限制输入缓冲区大小（对于实时流很有用）",
        OPTION_OFFSET(infinite_buffer), OPTION_INT(0, 0, 1) },
    { "framedrop",                      "cpu太慢时丢弃帧",
        OPTION_OFFSET(framedrop),       OPTION_INT(0, -1, 120) },
    { "seek-at-start",                  "应查找玩家的设定偏移量",
        OPTION_OFFSET(seek_at_start),       OPTION_INT64(0, 0, INT_MAX) },
    { "subtitle",                       "解码字幕流",
        OPTION_OFFSET(subtitle),        OPTION_INT(0, 0, 1) },
    // FFP_MERGE: window_title
#if CONFIG_AVFILTER
    { "af",                             "音频滤波器",
        OPTION_OFFSET(afilters),        OPTION_STR(NULL) },
    { "vf0",                            "视频筛选器0",
        OPTION_OFFSET(vfilter0),        OPTION_STR(NULL) },
#endif
    { "rdftspeed",                      "rdft速度（毫秒）",
        OPTION_OFFSET(rdftspeed),       OPTION_INT(0, 0, INT_MAX) },
    // FFP_MERGE: showmode, default, i, codec, acodec, scodec, vcodec
    // TODO: autorotate

    { "find_stream_info",               "读取并解码流以用启发式方法填充缺失的信息" ,
        OPTION_OFFSET(find_stream_info),    OPTION_INT(1, 0, 1) },

    // extended options in ff_ffplay.c
    { "max-fps",                        "在fps大于最大fps的视频中丢弃帧",
        OPTION_OFFSET(max_fps),         OPTION_INT(31, -1, 121) },

    { "overlay-format",                 "fourcc of overlay format",
        OPTION_OFFSET(overlay_format),  OPTION_INT(SDL_FCC_RV32, INT_MIN, INT_MAX),
        .unit = "overlay-format" },
    { "fcc-_es2",                       "", 0, OPTION_CONST(SDL_FCC__GLES2), .unit = "overlay-format" },
    { "fcc-i420",                       "", 0, OPTION_CONST(SDL_FCC_I420), .unit = "overlay-format" },
    { "fcc-yv12",                       "", 0, OPTION_CONST(SDL_FCC_YV12), .unit = "overlay-format" },
    { "fcc-rv16",                       "", 0, OPTION_CONST(SDL_FCC_RV16), .unit = "overlay-format" },
    { "fcc-rv24",                       "", 0, OPTION_CONST(SDL_FCC_RV24), .unit = "overlay-format" },
    { "fcc-rv32",                       "", 0, OPTION_CONST(SDL_FCC_RV32), .unit = "overlay-format" },

    { "start-on-prepared",                  "准备好后自动开始播放",
        OPTION_OFFSET(start_on_prepared),   OPTION_INT(1, 0, 1) },

    { "video-pictq-size",                   "最大图片队列帧数",
        OPTION_OFFSET(pictq_size),          OPTION_INT(VIDEO_PICTURE_QUEUE_SIZE_DEFAULT,
                                                       VIDEO_PICTURE_QUEUE_SIZE_MIN,
                                                       VIDEO_PICTURE_QUEUE_SIZE_MAX) },

    { "max-buffer-size",                    "应预先读取最大缓冲区大小",
        OPTION_OFFSET(dcc.max_buffer_size), OPTION_INT(MAX_QUEUE_SIZE, 0, MAX_QUEUE_SIZE) },
    { "min-frames",                         "停止预读的最小帧",
        OPTION_OFFSET(dcc.min_frames),      OPTION_INT(DEFAULT_MIN_FRAMES, MIN_MIN_FRAMES, MAX_MIN_FRAMES) },
    { "first-high-water-mark-ms",           "第一次唤醒read_thread的机会",
        OPTION_OFFSET(dcc.first_high_water_mark_in_ms),
        OPTION_INT(DEFAULT_FIRST_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_FIRST_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_LAST_HIGH_WATER_MARK_IN_MS) },
    { "next-high-water-mark-ms",            "第二次唤醒read_thread的机会",
        OPTION_OFFSET(dcc.next_high_water_mark_in_ms),
        OPTION_INT(DEFAULT_NEXT_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_FIRST_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_LAST_HIGH_WATER_MARK_IN_MS) },
    { "last-high-water-mark-ms",            "唤醒read_thread的最后机会",
        OPTION_OFFSET(dcc.last_high_water_mark_in_ms),
        OPTION_INT(DEFAULT_LAST_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_FIRST_HIGH_WATER_MARK_IN_MS,
                   DEFAULT_LAST_HIGH_WATER_MARK_IN_MS) },

    { "packet-buffering",                   "暂停输出，直到在停止后读取了足够的数据包",
        OPTION_OFFSET(packet_buffering),    OPTION_INT(1, 0, 1) },
    { "sync-av-start",                      "同步av开始时间",
        OPTION_OFFSET(sync_av_start),       OPTION_INT(1, 0, 1) },
    { "iformat",                            "force format",
        OPTION_OFFSET(iformat_name),        OPTION_STR(NULL) },
    { "no-time-adjust",                     "从媒体流中返回玩家的实时时间，而不是调整后的时间",
        OPTION_OFFSET(no_time_adjust),      OPTION_INT(0, 0, 1) },
    { "preset-5-1-center-mix-level",        "为5.1声道预设中央混音级别",
        OPTION_OFFSET(preset_5_1_center_mix_level), OPTION_DOUBLE(M_SQRT1_2, -32, 32) },

    { "enable-accurate-seek",                      "实现精确寻道",
        OPTION_OFFSET(enable_accurate_seek),       OPTION_INT(0, 0, 1) },
    { "accurate-seek-timeout",                      "精确寻道超时",
        OPTION_OFFSET(accurate_seek_timeout),       OPTION_INT(MAX_ACCURATE_SEEK_TIMEOUT, 0, MAX_ACCURATE_SEEK_TIMEOUT) },
    { "skip-calc-frame-rate",                      "不计算实际帧速率",
        OPTION_OFFSET(skip_calc_frame_rate),       OPTION_INT(0, 0, 1) },
    { "get-frame-mode",                      "警告，此选项仅适用于get frame",
        OPTION_OFFSET(get_frame_mode),       OPTION_INT(0, 0, 1) },
    { "async-init-decoder",                  "异步创建解码器",
        OPTION_OFFSET(async_init_decoder),   OPTION_INT(0, 0, 1) },
    { "video-mime-type",                    "默认视频mime类型",
        OPTION_OFFSET(video_mime_type),     OPTION_STR(NULL) },

        // iOS only options
    { "videotoolbox",                       "VideoToolbox: enable",
        OPTION_OFFSET(videotoolbox),        OPTION_INT(0, 0, 1) },
    { "videotoolbox-max-frame-width",       "VideoToolbox: max width of output frame",
        OPTION_OFFSET(vtb_max_frame_width), OPTION_INT(0, 0, INT_MAX) },
    { "videotoolbox-async",                 "VideoToolbox: use kVTDecodeFrame_EnableAsynchronousDecompression()",
        OPTION_OFFSET(vtb_async),           OPTION_INT(0, 0, 1) },
    { "videotoolbox-wait-async",            "VideoToolbox: call VTDecompressionSessionWaitForAsynchronousFrames()",
        OPTION_OFFSET(vtb_wait_async),      OPTION_INT(1, 0, 1) },
    { "videotoolbox-handle-resolution-change",          "VideoToolbox: handle resolution change automatically",
        OPTION_OFFSET(vtb_handle_resolution_change),    OPTION_INT(0, 0, 1) },

    // Android only options
    { "mediacodec",                             "MediaCodec: enable H264 (deprecated by 'mediacodec-avc')",
        OPTION_OFFSET(mediacodec_avc),          OPTION_INT(0, 0, 1) },
    { "mediacodec-auto-rotate",                 "MediaCodec: auto rotate frame depending on meta",
        OPTION_OFFSET(mediacodec_auto_rotate),  OPTION_INT(0, 0, 1) },
    { "mediacodec-all-videos",                  "MediaCodec: enable all videos",
        OPTION_OFFSET(mediacodec_all_videos),   OPTION_INT(0, 0, 1) },
    { "mediacodec-avc",                         "MediaCodec: enable H264",
        OPTION_OFFSET(mediacodec_avc),          OPTION_INT(0, 0, 1) },
    { "mediacodec-hevc",                        "MediaCodec: enable HEVC",
        OPTION_OFFSET(mediacodec_hevc),         OPTION_INT(0, 0, 1) },
    { "mediacodec-mpeg2",                       "MediaCodec: enable MPEG2VIDEO",
        OPTION_OFFSET(mediacodec_mpeg2),        OPTION_INT(0, 0, 1) },
    { "mediacodec-mpeg4",                       "MediaCodec: enable MPEG4",
        OPTION_OFFSET(mediacodec_mpeg4),        OPTION_INT(0, 0, 1) },
    { "mediacodec-handle-resolution-change",                    "MediaCodec: handle resolution change automatically",
        OPTION_OFFSET(mediacodec_handle_resolution_change),     OPTION_INT(0, 0, 1) },
    { "opensles",                           "OpenSL ES：启用",
        OPTION_OFFSET(opensles),            OPTION_INT(0, 0, 1) },
    { "soundtouch",                           "SoundTouch:启用",
        OPTION_OFFSET(soundtouch_enable),            OPTION_INT(0, 0, 1) },
    { "mediacodec-sync",                 "mediacodec: 使用消息队列进行同步",
        OPTION_OFFSET(mediacodec_sync),           OPTION_INT(0, 0, 1) },
    { "mediacodec-default-name",          "mediacodec default name",
        OPTION_OFFSET(mediacodec_default_name),      OPTION_STR(NULL) },
    { "ijkmeta-delay-init",          "ijkmeta delay init",
        OPTION_OFFSET(ijkmeta_delay_init),      OPTION_INT(0, 0, 1) },
    { "render-wait-start",          "等待开始之后才绘制",
        OPTION_OFFSET(render_wait_start),      OPTION_INT(0, 0, 1) },

    { NULL }
};

#undef OPTION_STR
#undef OPTION_CONST
#undef OPTION_INT
#undef OPTION_OFFSET

#endif
