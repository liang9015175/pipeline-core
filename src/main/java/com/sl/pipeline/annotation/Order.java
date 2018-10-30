package com.sl.pipeline.annotation;

/**
 * 定义pipeline中每个stage的执行顺序
 * 注意此order必须在同一个pipeline中,如果出现一个pipeline中多个stage的执行顺序是一样的
 * 则优先此对象的hash值,如果hash值也一样,则随机
 * 优先顺序: order-hash-随机
 * 最佳实践: 1.建议为每个order定义唯一不同的order值,比如为所有用户的stage定义为1XX  订单:2XX等
 *          2.可以扩展stageDriver 提供排序器 {@link java.util.Comparator}
 *
 */
public @interface Order {
    /**
     *  执行顺序
     * @return 执行顺序
     */
    int value();

    /**
     * 下一个要执行的
     * @return
     */
    int next();

}
