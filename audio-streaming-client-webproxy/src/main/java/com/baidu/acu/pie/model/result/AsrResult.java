package com.baidu.acu.pie.model.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * asr返回结果
 */
@Data
public class AsrResult {
    private String asrResult;
    private String audioId;
    @JsonProperty("isCompleted")
    private boolean isCompleted;
    @JsonProperty("isFinished")
    private boolean isFinished;
}
