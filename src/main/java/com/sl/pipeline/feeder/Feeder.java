package com.sl.pipeline.feeder;

/**
 * Feeder 是多个stage之间交换数据的通道，常用在上下两个stage之间传递,当每一个stage
 * 在完成自己的业务方法之后，由相关联的{@link com.sl.pipeline.stage.StageDriver} 传递处理之后的对象到下一个stage
 */
public interface Feeder {
    /**
     * 提供一个默认的空Feeder,通常可以被用于在pipeline的末端，提供一个空的Feeder
     */
    Feeder VOID= object -> {

    };

    /**
     * 将上一个stage 处理过后的对象传输到交流通道中,此通道可以是一个hashMap,linkList,blockedList
     * 或者redis缓存等地方放队列
     * @param object    上一个stage 处理过后的对象
     */
    void feed(Object object);
}
