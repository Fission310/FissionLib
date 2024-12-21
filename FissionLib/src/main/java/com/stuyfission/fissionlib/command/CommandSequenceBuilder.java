package com.stuyfission.fissionlib.command;

import java.util.ArrayList;

public class CommandSequenceBuilder {

    private ArrayList<CommandImpl> commands;

    public CommandSequenceBuilder() {
        commands = new ArrayList<>();
    }

    public CommandSequenceBuilder addCommand(Command command) {
        CommandImpl commandImpl = new CommandImpl(command);
        commands.add(commandImpl);
        return this;
    }

    public CommandSequenceBuilder addWaitCommand(double seconds) {
        WaitCommand waitCommand = new WaitCommand(seconds);
        commands.add(waitCommand);
        return this;
    }

    public CommandSequence build() {
        return new CommandSequence(commands);
    }

}
