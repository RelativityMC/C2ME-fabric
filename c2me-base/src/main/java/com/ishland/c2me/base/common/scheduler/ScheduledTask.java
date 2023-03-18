package com.ishland.c2me.base.common.scheduler;

public interface ScheduledTask {

    public boolean trySchedule();

    public void addPostAction(Runnable postAction);

    public long centerPos();

}
