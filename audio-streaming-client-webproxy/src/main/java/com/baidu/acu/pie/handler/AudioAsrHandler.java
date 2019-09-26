package com.baidu.acu.pie.handler;

import com.baidu.acu.pie.client.AsrClient;
import com.baidu.acu.pie.client.AsrClientFactory;
import com.baidu.acu.pie.constant.RequestType;
import com.baidu.acu.pie.model.AsrConfig;
import com.baidu.acu.pie.model.RecognitionResult;
import com.baidu.acu.pie.model.RequestMetaData;
import com.baidu.acu.pie.model.StreamContext;
import com.baidu.acu.pie.model.info.AudioData;
import com.baidu.acu.pie.model.response.ServerResponse;
import com.baidu.acu.pie.model.result.AsrResult;
import com.baidu.acu.pie.service.AudioHandlerService;
import com.baidu.acu.pie.utils.WebSocketUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * asr解析处理
 */
@Slf4j
public class AudioAsrHandler implements Runnable {

    private Session session;
    private Queue<AudioData> queue;
    private AudioHandlerService audioHandlerService;
    private StreamContext streamContext;
    private AsrClient asrClient;
    private String audioId = null;
    private volatile boolean asrFinish = false;
    private volatile AsrResult asrTempResult = null;


    public AudioAsrHandler(Session session, AudioHandlerService audioHandlerService) {
        this.session = session;
        this.audioHandlerService = audioHandlerService;

        queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * 设置asr识别结束，供外层调用
     */
    public void setAsrFinish() {
        asrFinish = true;
    }

    /**
     * 向队列中传送数据
     */
    public synchronized void offer(AudioData audioData) {
        queue.add(audioData);

    }

    @Override
    public void run() {
        asyncRecognition();
    }

    /**
     * 开启识别过程
     */
    private void asyncRecognition() {
        asrClient = createAsrClient();
        initStreamContext();
        try {
            // 10秒等待时间
            int waitingCount = 0;
            while (waitingCount <= 100) {

                if (!queue.isEmpty()) {
                    AudioData audioData = queue.poll();
                    waitingCount = 0;
                    send(audioData);
                } else {
                    if (!asrFinish) {
                        Thread.sleep(100);
                    }
                    waitingCount ++;
                }
            }
            streamContext.complete();
            streamContext.getFinishLatch().await();
            sendFinishResult();

        } catch (Exception e) {
            log.info("asr recognition occur exception:" + e.getMessage());
        } finally {
            asrClient.shutdown();
            audioHandlerService.unRegisterClient(session);
        }
    }

    /**
     * 初始化initStreamContext
     */
    private void initStreamContext() {
        RequestMetaData requestMetaData = createRequestMeta();
        streamContext = asrClient.asyncRecognize(it -> {
            log.info(DateTime.now().toString() + Thread.currentThread().getId() + " receive fragment: " + it);
            handleRecognitionResult(it);
        }, requestMetaData);
    }

    /**
     * 创建asr客户端
     */
    private AsrClient createAsrClient() {
        // asrConfig构造后就不可修改
        // TODO 后期优化，暂时写死
        AsrConfig asrConfig = AsrConfig.builder()
                .build();

        return AsrClientFactory.buildClient(asrConfig);
    }

    private RequestMetaData createRequestMeta() {
        RequestMetaData requestMetaData = new RequestMetaData();
        requestMetaData.setSendPackageRatio(1);
        requestMetaData.setSleepRatio(1);
        requestMetaData.setTimeoutMinutes(120);
        requestMetaData.setEnableFlushData(true);

        return requestMetaData;
    }

    /**
     * 处理识别结果,并发送给客户端
     * 为了将asr识别结束标志也一并发送出去，当前结果先缓存，发送上一次的结果
     */
    private void handleRecognitionResult (RecognitionResult result) {

        if(asrTempResult == null) {
            asrTempResult = new AsrResult();
            asrTempResult.setAsrResult(result.getResult());
            asrTempResult.setCompleted(result.isCompleted());
            asrTempResult.setAudioId(audioId);
            return;
        }
        WebSocketUtil.sendMsgToClient(session, ServerResponse.successStrResponse(asrTempResult, RequestType.ASR));
        asrTempResult = new AsrResult();
        asrTempResult.setAsrResult(result.getResult());
        asrTempResult.setCompleted(result.isCompleted());
        asrTempResult.setAudioId(audioId);
    }

    /**
     * asr解析结束时，发送缓存数据以及finish标志
     */
    private void sendFinishResult() {
        if (asrTempResult != null) {
            asrTempResult.setFinished(true);
            WebSocketUtil.sendMsgToClient(session, ServerResponse.successStrResponse(asrTempResult, RequestType.ASR));
            asrTempResult = null;
        }

    }

    /**
     * 向asr发送数据逻辑，可能包含二进制数组和inputStream两种方式
     * 若是音频id形式，那么每次处理完该音频后（），再处理下一条音频
     */
    private void send(AudioData audioData) throws IOException, InterruptedException {
        if (audioData.getAudioId() != null) {
            audioId = audioData.getAudioId();
            byte[] data = new byte[320];
            while (audioData.getInputStream().read(data) != -1) {
                streamContext.send(data);
            }
            streamContext.complete();
            streamContext.getFinishLatch().await();
            sendFinishResult();
            audioId = null;
            initStreamContext();
            return;
        }
        streamContext.send(audioData.getAudioBytes());
    }
}
