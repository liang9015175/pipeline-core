package com.sl.pipeline.stage;

import com.sl.pipeline.exception.ProcessingException;
import com.sl.pipeline.tolerance.FaultToleranceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 抽象stageDriver,为其他stageDriver提供公共模板
 */
public abstract class AbstractStageDriver implements StageDriver {
    /**
     * 当前stage
     */
    protected Stage stage;
    /**
     * 上下文
     */
    private StageContext stageContext;
    /**
     * 当前的stage状态,默认是未启动的, 此状态为volatile 方便其他线程能够立即看到此对象状态的变化
     */
    protected volatile State currentState=State.STOPPED;
    /**
     * 发生异常的时候的容错级别
     */
    protected FaultToleranceType faultToleranceType=FaultToleranceType.NO;
    /**
     * 当前stage在处理过程中发生的异常
     */
    private List<ProcessingException>  processingExceptions=new ArrayList<>();
    /**
     * 当前stage 在处理过程中发生的错误
     */
    private List<Throwable> errors=new ArrayList<>();

    public AbstractStageDriver(Stage stage,StageContext stageContext){
        this.stage=stage;
        this.stageContext=stageContext;
    }
    public AbstractStageDriver(Stage stage, StageContext stageContext, FaultToleranceType faultToleranceType){
        this(stage, stageContext);
        this.faultToleranceType=faultToleranceType;
    }

    @Override
    public Stage getStage() {
        return this.stage;
    }

    @Override
    public State getState() {
        return this.currentState;
    }

    /**
     * 设置状态 当设置完之后要通知其他线程启动,竞争资源
     * @param state 要设置的状态
     */
    protected synchronized void setState(State state){
        this.currentState=state;
        notifyAll();

    }
    /**
     * 判断是否当前stageDriver处于此状态中
     * @param states  状态列表
     * @return  是否存在此状态中
     */
    protected synchronized boolean isInState(State... states){
       return Arrays.asList(states).stream().anyMatch(v->v==currentState);
    }

    /**
     * 设置当前stageDriver状态
     * @param testState 测试状态
     * @param nextState 要设置的状态
     * @return  是否设置成功
     */
    protected synchronized boolean testAndSetState(State testState,State nextState){
      if(currentState==testState){
          setState(nextState);
          return true;
      }else {
          return false;
      }
    }

    /**
     * 获取容错类型
     * @return 容错类型
     */
    public FaultToleranceType getFaultToleranceType() {
        return this.faultToleranceType;
    }

    @Override
    public List<Throwable> getFatalErrors() {
        return this.errors;
    }

    /**
     * 记录错误
     * @param throwable 错误
     */
    protected void recordFatalError(Throwable throwable){
        this.errors.add(throwable);
    }

    /**
     * 记录异常
     * @param exception 异常
     */
    protected void recordProcessingException(ProcessingException exception){
        this.processingExceptions.add(exception);
    }

    @Override
    public List<ProcessingException> getProcessingExceptions() {
        return processingExceptions;
    }
}
