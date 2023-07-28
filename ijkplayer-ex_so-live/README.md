1:花屏

ijkplayer\extra\ffmpeg\libavformat\udp.c
FFmpeg发送的数据会比较大，超过了FFmpeg默认最大值，需要扩大接收端的接收缓冲区
修改了UDP_MAX_PKT_SIZE = 655360
//就是
player.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "buffer_size", bufferSize.toLong())

2：延迟
ijkplayer\ijkmedia\ijkplayer\ff_ffplay.c    video_refresh 方法

关闭音视频同步，降低延迟
player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "sync-audio-video-disable", 1)

3:硬解码在后续播放过程中宽高改变的时候不回调

ijkplayer\ijkmedia\ijkplayer\android\pipeline\ffpipenode_android_mediacodec_vdec.c   drain_output_buffer_l 方法


//硬解码宽高改变的时候直接回调
player.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-size-call", 1)