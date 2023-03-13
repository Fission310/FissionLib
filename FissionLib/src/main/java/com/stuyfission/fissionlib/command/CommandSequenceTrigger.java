package com.stuyfission.fissionlib.command;

public class CommandSequenceTrigger {

    private CommandSequence commandSequence;

    public CommandSequenceTrigger(CommandSequence commandSequence) {
        this.commandSequence = commandSequence;
    }

    public void trigger() {
        if (commandSequence.hasCompleted) {
            commandSequence.run();
        }
    }

}
