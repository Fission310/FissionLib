package com.stuyfission.fissionlib.command;

import com.stuyfission.fissionlib.input.GamepadStatic;

public class CommandSequenceTrigger {

    private CommandSequence commandSequence;
    public GamepadStatic.INPUT triggerCondition;

    public CommandSequenceTrigger(CommandSequence commandSequence) {
        this.commandSequence = commandSequence;
        this.triggerCondition = GamepadStatic.INPUT.NONE;
    }

    public CommandSequenceTrigger(CommandSequence commandSequence, GamepadStatic.INPUT triggerCondition) {
        this.commandSequence = commandSequence;
        this.triggerCondition = triggerCondition;
    }

    public void trigger() {
        if (commandSequence.hasCompleted) {
            commandSequence.run();
        }
    }

}
