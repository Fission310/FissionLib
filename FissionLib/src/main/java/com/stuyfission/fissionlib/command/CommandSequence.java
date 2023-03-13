package com.stuyfission.fissionlib.command;

import java.util.ArrayList;

public class CommandSequence {

    private ArrayList<CommandImpl> commands = new ArrayList<>();
    private Runnable commandRunnable;
    private Thread commandThread;
    public boolean hasCompleted;

    public CommandSequence() {
        hasCompleted = true;
    }

    public CommandSequence addCommand(Command command) {
        CommandImpl commandImpl = new CommandImpl(command);
        commands.add(commandImpl);
        return this;
    }

    public CommandSequence build() {
        commandRunnable = () -> {
            for (CommandImpl command : commands) {
                command.run();
                while (!command.completed) { }
            }
            hasCompleted = true;
        };

        commandThread = new Thread(commandRunnable);

        return this;
    }

    public void run() {
        hasCompleted = false;
        commandThread = new Thread(commandRunnable);
        commandThread.start();
    }
    
}
