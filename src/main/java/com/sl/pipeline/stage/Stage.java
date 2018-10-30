package com.sl.pipeline.stage;

import com.sl.pipeline.exception.StageException;

/**
 * stage 接口 每个stage 代表一个pipeline中基本单位
 */
public interface Stage {

    /**
     * stage 名称 默认为common
     * @return
     */
    default String name(){return "common";};

    /**
     * 初始化stage，确保每个stage 能够正确初始化
     * @param stageContext {@link StageContext}
     */
    void  init(StageContext stageContext);

    /**
     * 预处理,必要的设置在{@link #process(Object)} 之前
     * @throws StageException 预处理可能会抛出异常,确保容错{@link com.sl.pipeline.tolerance.FaultToleranceType }级别能够
     * 正确处理该异常
     */
    void preProcess() throws StageException;

    /**
     * 核心处理逻辑,真正的处理业务逻辑放在此方法中
     * @param o 待处理对象
     * @throws StageException 处理过程中可能会抛出异常,确保容错{@link com.sl.pipeline.tolerance.FaultToleranceType }级别能够
     * 正确处理该异常
     */
    void process(Object o) throws StageException;

    /**
     * 后置处理,必要的终结操作放在此方法里面
     * @throws StageException 处理过程中可能会抛出异常,确保容错{@link com.sl.pipeline.tolerance.FaultToleranceType }级别能够
     * 正确处理该异常
     */
    void postProcess() throws StageException;

    /**
     * 必要的释放操作,销毁资源 在{@link #preProcess()} ,{@link #postProcess()},{@link #postProcess()} 处理过程中
     * 一旦发生了异常，应该在finally中销毁必要的资源,比如流资源 等
     */
    void release();
}
