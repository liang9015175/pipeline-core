package com.sl.pipeline.stage;

import com.sl.pipeline.exception.ProcessingException;
import com.sl.pipeline.exception.StageException;
import com.sl.pipeline.feeder.Feeder;
import com.sl.pipeline.tolerance.FaultToleranceType;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DedicatedThreadDriver extends AbstractStageDriver {
    /**
     * 从上游feeder检索待处理对象的超时时间
     */
    private long timeout;
    /**
     * 处理stage的工作线程
     */
    private Thread workThread;
    /**
     * 阻塞队列
     */
    private BlockingDeque<Object> deque;

    private final Feeder feeder= object -> {
       try {
           this.deque.put(object);
       }catch (Exception e){
           throw new RuntimeException("Unexpected exception takes when put object:"+object+"to the queue of :"+stage);
       }
       synchronized (this){
           this.notifyAll();
       }
    };

    public DedicatedThreadDriver(Stage stage, StageContext stageContext, BlockingDeque<Object> deque, long timeout, FaultToleranceType faultToleranceType) {
        super(stage, stageContext);
        this.deque=deque;
        this.timeout=timeout;
        this.faultToleranceType=faultToleranceType;

    }

    @Override
    public synchronized void start() throws StageException {
        if(this.currentState==State.STOPPED){
            log.debug("stage:{} going start to work",stage.name());
            this.workThread=new WorkerThread(stage);
            workThread.start();
            try{
                //确保stage可以正确初始化并运行
                while (!(this.currentState==State.RUNNING)){
                    this.wait();
                }
            }catch (InterruptedException e){
                throw new StageException("worker thread unexpectedly interrupted while waiting for thread startup",e);
            }

        }else {
            throw new IllegalStateException("Attempt to start Driver in stage:"+currentState);
        }

    }

    /**
     * 优雅停机
     *
     * @throws StageException
     */
    @Override
    public void finish() throws StageException {
        if(currentState==State.STOPPED){
            throw new IllegalStateException("this driver is already stopped");
        }
        try{
            //确保当前线程是running/error
            while (!(this.currentState==State.RUNNING||currentState==State.ERROR))  {
                  this.wait();
            }
            //当设置此状态之后
            //如果工作子线程没有发生异常,子线程会跑完所有的任务-然后状态设置为finished
            //如果工作子线程发生了异常，子线程状态为error
            testAndSetState(State.RUNNING,State.STOP_REQUESTED);

            //确保当前stage是finished或者error
            while (!(this.currentState==State.FINISHED||currentState==State.ERROR)){
                    this.wait();
            }
            //再次工作子线程介入,要求子线程必须完成工作,才会停止
            this.workThread.join();
        }catch (InterruptedException ignored){
             throw new StageException("worker thread unexpectedly interrupted while waiting for graceful shudown",ignored);
        }
        //stage停止
        setState(State.STOPPED);
    }

    @Override
    public Feeder getFeeder() {
        return this.feeder;
    }


    private class WorkerThread extends Thread{
        private Stage stage;
        WorkerThread(Stage stage){
            this.stage=stage;
        }
        public void run(){
            setState(StageDriver.State.STARTED);
            try{
                log.debug("preProcess for stage:{} going run...",stage.name());
                //TODO 为什么这里没有执行init 方法
                stage.preProcess();
                log.debug("preProcess for stage:{} finished",stage.name());
                testAndSetState(StageDriver.State.STARTED, StageDriver.State.RUNNING);
                while (currentState != StageDriver.State.ERROR) {
                    try {
                        //阻塞，直到从队列里取出元素
                        Object poll = deque.poll(timeout, TimeUnit.MICROSECONDS);
                        if (poll == null) {
                            //没有取出任何元素，且当前stage的状态为停止接受任何对象,则让当前stage停止
                            if (currentState == StageDriver.State.STOP_REQUESTED) break;
                        } else {
                            try {
                                //处理业务
                                log.debug("process for stage:{} going run...", stage.name());
                                stage.process(poll);
                                log.debug("process for stage:{} finished...", stage.name());
                            } catch (Exception e) {
                                //发生任何异常则记录异常到异常栈中
                                ProcessingException exception = new ProcessingException(stage, e);
                                recordProcessingException(exception);
                                /*
                                  根据容错策略，如果是零容忍,则抛出异常,stage停止工作
                                  这里需要注意,一旦发生了零容忍,则stage不会执行{@link Stage#postProcess()}
                                  而直接执行了{@link Stage#release()}释放操作,也就是立即释放资源停止
                                 */
                                if (faultToleranceType == FaultToleranceType.NO) throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException("work thread unexpectedly interrupted while waiting data for stage:" + stage, e);
                    }
                }
                log.debug("postProcess for stage:{} going run...",stage.name());
                stage.postProcess();
                log.debug("postProcess for stage:{} finished...",stage.name());
            }catch (Exception e){
                //记录错误到异常栈
                recordFatalError(e);
                //设置stage状态为ERROR
                setState(StageDriver.State.ERROR);
            }finally {
                log.debug("release for stage:{} going run...",stage.name());
                //释放资源操作
                stage.release();
                log.debug("release for stage:{} finished ...",stage.name());
            }
            /*当没有任何待处理的任务且stage状态为接受到stop_requested 请求时候,stage结束
             *当有异常发生，导致一个致命错误的时候,此时stage的状态是ERROR 状态,将不会到达finished状态
             * */
            testAndSetState(StageDriver.State.STOP_REQUESTED, StageDriver.State.FINISHED);
        }
    }
}
