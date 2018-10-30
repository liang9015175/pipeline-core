package com.sl.pipeline.tolerance;

import lombok.Getter;

/**
 * 容错策略
 */
@Getter
public enum FaultToleranceType {
    /**
     * 零容忍:一旦有异常{@link com.sl.pipeline.exception.StageException}
     * 被 {@link com.sl.pipeline.stage.Stage}抛出,该stage就不再接受上流 {@link com.sl.pipeline.feeder.Feeder }来的target
     * 直到自己处理完自己队列里面的所有target,此操作不会影响其他的stage操作
     */
    NO("零容忍"),
    /**
     * 默化处理: 一旦有异常{@link com.sl.pipeline.exception.StageException}
     * 被 {@link com.sl.pipeline.stage.Stage}抛出 仅仅打印日志,操作继续进行,此级别慎用,以免造成不必要的损失
     */
    ALL("所有容忍"),
    /**
     * 受检容忍: 一旦有异常{@link com.sl.pipeline.exception.StageException}
     * 被 {@link com.sl.pipeline.stage.Stage}抛出 ,记录异常到异常栈中,同时打印错误级别日志,需要开发人员后续自定义处理异常栈中的信息
     */
    CHECKED("受检容忍");

    private String desc;

    FaultToleranceType(String desc){
        this.desc=desc;
    }
}
