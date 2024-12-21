package com.stuyfission.fissionlib.command;

import java.util.ArrayList;

public class CommandSequence {

    private Runnable commandRunnable;
    private Thread commandThread;
    public boolean hasCompleted;

    protected CommandSequence(ArrayList<CommandImpl> commands) {
        commandRunnable = () -> {
            for (CommandImpl command : commands) {
                command.run();
                while (!command.completed) { }
            }
            hasCompleted = true;
        };

        commandThread = new Thread(commandRunnable);
    }

    public void run() {
        hasCompleted = false;
        commandThread = new Thread(commandRunnable);
        commandThread.start();
    }

    public void trigger() {
        if (this.hasCompleted) {
            this.run();
        }
    }
}
