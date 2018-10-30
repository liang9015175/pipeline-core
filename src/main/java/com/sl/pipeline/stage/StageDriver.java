package com.sl.pipeline.stage;

import com.sl.pipeline.exception.ProcessingException;
import com.sl.pipeline.exception.StageException;
import com.sl.pipeline.feeder.Feeder;

import java.util.List;

/**
 * StageDriver stage驱动器 主要用户控制stage的生命周期(初始化-执行-结束-释放)等
 * 同时不同的stageDriver可以驱动stage在不同的方式,比如以多线程方式/单线程
 */
public interface StageDriver {
    enum State{
        /**
         * 资源已经被释放,且stage所有的活动都已经终止,或者该stage 根本就没有启动,这是默认状态
         */
        STOPPED,
        /**
         * stage 已经初始化完毕,并且已经准备开始执行{@link Stage#preProcess()}方法
         */
        STARTED,
        /**
         * stage 正在执行{@link Stage#process(Object)}处理业务
         */
        RUNNING,
        /**
         * 停止接受来自上流的传输对象，一旦stage进入此状态，该stage就会一次完成process，postProcess 方法,然后shutDown
         */
        STOP_REQUESTED,
        /**
         * postProcess 已经完成,stage 执行shutdown
         */
        FINISHED,
        /**
         * 在执行的过程中错误发生，引起此stage 进入此状态,注意 错误发生之后,stage 无法从错误状态再 重启
         * 在执行过程中发生的异常，并不会引起error 状态,错误状态指的事stage 在初始化或者结束的时候,造成的异常，而并非处理异常
         * 如果发生错误,则会被记录到错误列表，通过getFatalErrors 可以获取
         */
        ERROR;
    }

    /**
     * 此方法被{@link Stage} 调用,用于开始一个处理流程
     * 依次执行{@link Stage#preProcess()},{@link Stage#process(Object)} ，{@link Stage#postProcess()} 方法
     *
     * @throws StageException  在执行的过程中,可能会抛出异常,需要根据容错{@link com.sl.pipeline.tolerance.FaultToleranceType} 进行异常的捕获
     * 和相应的处理
     */
    void start() throws StageException;

    /**
     *  在调用此方法后,stage并不会立刻停止,而是会阻塞知道stage 队列里所有的元素处理完,并且标记为{@link State#STOP_REQUESTED}之后 才会执行
     *  @see Stage#release() 方法进行资源的释放
     * @throws StageException 在执行的过程中,可能会抛出异常,需要根据容错{@link com.sl.pipeline.tolerance.FaultToleranceType} 进行异常的捕获
     * 和相应的处理
     */
    void finish() throws StageException;

    /**
     * 获取两个stageDriver之间的交流通道
     */
    Feeder getFeeder();

    /**
     * 获取该stageDriver管理的stage
     * @return 当前stage
     */
    Stage getStage();

    /**
     * 获取当前stage的状态
     * @return 当前状态
     */
    State getState();

    /**
     * 获取stage错误集合
     * @return  错误集合
     */
    List<Throwable> getFatalErrors();

    /**
     * 获取异常列表
     * @return 异常李彪
     */
    List<ProcessingException>    getProcessingExceptions();

}
