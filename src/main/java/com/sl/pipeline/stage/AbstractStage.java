package com.sl.pipeline.stage;

import com.sl.pipeline.exception.StageException;
import com.sl.pipeline.feeder.Feeder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractStage implements Stage{
    /**
     * stageContext上下文,用于多个stage之间交换信息
     */
    private StageContext stageContext;
    /**
     * 下游stage Feeder,当该stage完成业务方法之后,变化将处理对象通过此feeder传递给下一个
     * 此下游feeder的初始化执行懒加载过程
     */
    private Feeder downstreamFeeder;

    @Override
    public void init(StageContext stageContext) {
        this.stageContext=stageContext;
    }

    @Override
    public void preProcess() throws StageException {
        //什么都不做,仅做初始化动作
    }

    @Override
    public void process(Object o) throws StageException {
        this.emit(o);
    }

    @Override
    public void postProcess() throws StageException {
        //什么都不做,仅做初始化动作
    }

    @Override
    public void release() {
        //什么都不做,仅做初始化动作
    }

    /**
     * 传递待处理对象给下一个stage
     * @param o 待处理对象
     */
    public final void emit(Object o){
       if(this.downstreamFeeder==null){
           this.downstreamFeeder=stageContext.getDownstreamFeeder(this);
       }
       this.downstreamFeeder.feed(o);
    }

    /**
     * 传递待处理对象给下一个分支的第一个stage
     * @param branch    分支名称
     * @param o 待处理对象
     */
    public final void emit(String branch,Object o){
        stageContext.getBranchFeeder(branch).feed(o);
    }
}
