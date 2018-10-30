package com.sl.pipeline.stage;

import com.sl.pipeline.feeder.Feeder;
import com.sl.pipeline.listener.StageEventListener;

import java.util.Collection;
import java.util.EventObject;

/**
 * stageContext stage上下文，为一个pipeline中的所有stage 提供上下文信息以及交流通道
 * 通常情况下 stageContext 由pipeline 提供，多个stage共享
 */
public interface StageContext {
    /**
     * 注册一个{@link StageEventListener} 到上下文中，当{@link #raise(java.util.EventObject)} 被调用的时候
     * @param listener  被注册到上下文的监听器
     */
    void registerListener(StageEventListener listener);

    /**
     * 获取已注册在上下文环境中的事件监听器
     * @return {@link StageEventListener} 集合
     */
    Collection<StageEventListener> getRegisterListeners();

    /**
     * 挂起事件 通知任何注册在此stageContext中的事件监听器
     * @param event 事件对象
     */
    void raise(EventObject event);

    /**
     * 获取特定分支的第一个stage的feeder 如果分支不存在则默认返回null
     * @param branch 分支名称
     * @return  该分支上第一个stage的feeder
     */
    Feeder getBranchFeeder(String branch);

    /**
     * 获取下游stage的feeder,主要用于当本stage结束之后,传递对象到下个stage
     * @param stage {@link Stage} 下游分支
     * @return {@link Feeder} 下游stage 的喂养者
     */
    Feeder getDownstreamFeeder(Stage stage);

    /**
     * 获取全局变量,在初始化stageContext中，我们建议上下文在初始化时候定义一些基础环境变量,以方便多个stage之间共享变量
     * @param key 环境变量key
     * @return  环境变量value
     */
    Object getEnv(String key);
}
