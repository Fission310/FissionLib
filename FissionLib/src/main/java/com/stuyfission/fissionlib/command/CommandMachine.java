package com.stuyfission.fissionlib.command;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.stuyfission.fissionlib.input.GamepadStatic;

import java.util.ArrayList;

public class CommandMachine {

    private ArrayList<CommandSequenceTrigger> commandSequences = new ArrayList<>();
    private int currentCommandIndex;

    public CommandMachine() {
        this.currentCommandIndex = 0;
    }

    public CommandMachine addCommandSequence(CommandSequence commandSequence, GamepadStatic.INPUT triggerCondition) {
        CommandSequenceTrigger commandSequenceTrigger = new CommandSequenceTrigger(commandSequence, triggerCondition);
        commandSequences.add(commandSequenceTrigger);
        return this;
    }

    public CommandMachine build() {
        return this;
    }

    public void run(Gamepad gamepad) {
        CommandSequenceTrigger currentCommand = commandSequences.get(currentCommandIndex);

        if (currentCommand.triggerCondition == GamepadStatic.gamepadToEnum(gamepad)) {
            currentCommand.trigger();

            if (currentCommandIndex == commandSequences.size()-1) {
                currentCommandIndex = 0;
            } else {
                currentCommandIndex++;
            }
        }
    }
}
