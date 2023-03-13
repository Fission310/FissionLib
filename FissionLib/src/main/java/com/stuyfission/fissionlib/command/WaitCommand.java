package com.stuyfission.fissionlib.command;

public class WaitCommand extends CommandImpl {
    
    private long waitDuration;

    // duration in seconds
    public WaitCommand(double waitDuration) {
        this.waitDuration = (long) (waitDuration * 1e9);
        super.completed = false;
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        while ((startTime + waitDuration) > System.nanoTime()) {
            super.completed = false;
        }

        super.completed = true;
    }

}
