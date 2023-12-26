package com.stuyfission.fissionlib.command;

import java.util.ArrayList;

public class AutoCommandMachine {

    private ArrayList<CommandSequence> commandSequences = new ArrayList<>();
    private int currentCommandIndex;
    private boolean hasCompleted = false;

    public AutoCommandMachine() { this.currentCommandIndex = 0; }

    public AutoCommandMachine addCommandSequence(CommandSequence commandSequence) {
        commandSequences.add(commandSequence);
        return this;
    }

    public AutoCommandMachine build() { return this; }

    public int getCurrentCommandIndex() { return currentCommandIndex; }

    public boolean hasCompleted() { return hasCompleted; }

    public void reset() { currentCommandIndex = 0; }

    public void run(boolean driveIsBusy) {
        CommandSequence currentCommand = commandSequences.get(currentCommandIndex);

        if (currentCommand.hasCompleted && !driveIsBusy) {
            currentCommand.trigger();
            if (currentCommandIndex == commandSequences.size()-1) {
                currentCommandIndex = 0;
                hasCompleted = true;
            } else {
                currentCommandIndex++;
            }
        }
    }
}
