package com.baidu.acu.pie.service;

import com.baidu.acu.pie.constant.Constant;
import com.baidu.acu.pie.constant.RequestType;
import com.baidu.acu.pie.handler.AudioAsrHandler;
import com.baidu.acu.pie.model.info.AudioData;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.utils.JsonUtil;
import com.baidu.acu.pie.utils.WebSocketUtil;
import com.baidu.acu.pie.utils.WxUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AudioHandlerService {

    private final LoginHandlerService loginHandlerService;

    private Map<String, AudioAsrHandler> asrClients = new ConcurrentHashMap<>();

    // 记录录音asr前是否发起了start请求
    private Map<String, Boolean> asrStartSessions = new ConcurrentHashMap<>();

    /**
     * 处理asr数据请求
     */
    public void handle(Session session, String data) {

        if(!loginHandlerService.userExists(session)) {
            WebSocketUtil.sendMsgToClient(session,
                    ServerResponse.failureStrResponse(Constant.MUST_LOGIN_BEFORE_USE, RequestType.ASR));
            return;
        }

        String audioId = JsonUtil.parseJson(data, "audioId");
        if(audioId !=null) {
            handleAudioId(session, audioId);
            return;
        }

        // 若有录音asr 识别开始或者结束请求，则对应处理
        String operation = JsonUtil.parseJson(data, "operation");
        if(operation != null) {
            handleAsrOperation(session, operation);
        }


    }

    /**
     * 处理二进制数据解析
     */
    public void handle(Session session, byte[] audioBytes) {
        if(!loginHandlerService.userExists(session)) {
            WebSocketUtil.sendMsgToClient(session,
                    ServerResponse.failureStrResponse(Constant.MUST_LOGIN_BEFORE_USE, RequestType.ASR));
            return;
        }
        if (!asrStartSessions.containsKey(session.getId())) {
            WebSocketUtil.sendMsgToClient(session, ServerResponse.failureStrResponse(
                    "client must post start request first before recorder asr", RequestType.ASR));
            return;
        }
        byte[] copy = new byte[audioBytes.length];
        System.arraycopy(audioBytes, 0, copy, 0, audioBytes.length);
        AudioData audioData = new AudioData();
        audioData.setAudioBytes(copy);
        put(session, audioData);
    }

    /**
     * 取消asr客户端
     */
    public void unRegisterClient(Session session) {
        asrClients.remove(session.getId());
    }

    /**
     * 处理音频id请求
     */
    private void handleAudioId(Session session, String audioId) {
        try {
            InputStream inputStream = WxUtil.getAudioStream(audioId);
            AudioData audioData = new AudioData();
            audioData.setAudioId(audioId);
            audioData.setInputStream(inputStream);
            put(session, audioData);
        } catch (Exception e) {
            log.error(e.getMessage());
            WebSocketUtil.sendMsgToClient(session, ServerResponse.failureStrResponse(
                    "handle asr audioId " + audioId + " error:" + e.getMessage(), RequestType.ASR));
        }
    }

    /**
     * asr操作进行处理（开始或者结束 asr录音识别）
     */
    private void handleAsrOperation(Session session, String operation) {
        switch (operation) {
            case Constant.ASR_START:
                asrStartSessions.put(session.getId(), true);
                break;
            case Constant.ASR_STOP:
                asrStartSessions.remove(session.getId());
                if (asrClients.get(session.getId()) != null) {
                    asrClients.get(session.getId()).setAsrFinish();
                }
                break;

            default:
                WebSocketUtil.sendMsgToClient(session,
                        ServerResponse.failureStrResponse(Constant.REQUEST_IS_ILLEGAL, RequestType.ASR));
        }
    }

    /**
     * 将收到的音频数据，放到asr处理器中
     */
    private void put(Session session, AudioData audioData) {
        String id = session.getId();
        if (!asrClients.containsKey(id)) {
            registerClient(session);
        }
        AudioAsrHandler asr = asrClients.get(id);
        asr.offer(audioData);
    }

    /**
     * 将session注册至map中
     */
    private synchronized void registerClient(Session session) {
        String id = session.getId();
        if (asrClients.containsKey(id)) {
            return;
        }
        AudioAsrHandler asrHandler = new AudioAsrHandler(session, this);
        asrClients.put(session.getId(), asrHandler);
        new Thread(asrHandler).start();
    }

}
