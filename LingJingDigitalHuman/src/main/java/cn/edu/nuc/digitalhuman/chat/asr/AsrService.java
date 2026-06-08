package cn.edu.nuc.digitalhuman.chat.asr;

import org.springframework.web.multipart.MultipartFile;

public interface AsrService {

    /**
     * 语音识别：将音频文件转写为文字
     * @param audioFile 上传的音频文件
     * @return 转写文本，失败返回 null
     */
    String transcribe(MultipartFile audioFile);

    /**
     * 语音识别：将音频字节数组转写为文字
     * @param audioData 音频字节数组
     * @param fileName 文件名（用于推断格式）
     * @return 转写文本，失败返回 null
     */
    String transcribe(byte[] audioData, String fileName);
}
