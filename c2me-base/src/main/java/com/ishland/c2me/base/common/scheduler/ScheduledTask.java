package com.ishland.c2me.base.common.scheduler;

public interface ScheduledTask {

    public boolean tryPrepare();

    public void runTask(Runnable postAction);

    public long centerPos();

    public boolean isAsync();

}
