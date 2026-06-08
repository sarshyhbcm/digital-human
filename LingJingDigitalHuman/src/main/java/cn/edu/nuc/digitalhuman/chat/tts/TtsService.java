package cn.edu.nuc.digitalhuman.chat.tts;

public interface TtsService {

    /**
     * 语音合成：将文本合成为语音文件，返回可访问的URL
     * @param text 待合成的文本
     * @param speed 语速，0.5~2.0
     * @return 音频文件的访问URL，失败返回 null
     */
    String synthesize(String text, double speed);
}
