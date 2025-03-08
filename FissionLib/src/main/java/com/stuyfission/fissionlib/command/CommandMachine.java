package com.stuyfission.fissionlib.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class CommandMachine {
    public class State {
        protected class Transition {
            protected State state;
            protected CommandSequence sequence;

            protected Transition(State state, CommandSequence sequence) {
                this.state = state;
                this.sequence = sequence;
            }
        }

        private HashMap<Supplier<Boolean>, Transition> transitions = new HashMap<>();
        private ArrayList<Command> loops = new ArrayList<>();

        public State() {
        }

        protected void addTransition(Supplier<Boolean> condition, State nextState, CommandSequence transition,
                boolean transitionAtEnd) { // TODO implement switch states after transitioning
            transitions.put(condition, new Transition(nextState, transition));
        }

        protected void addLoop(Command loop) {
            loops.add(loop);
        }

        protected State run() {
            for (Supplier<Boolean> predicate : transitions.keySet()) {
                if (predicate.get()) {
                    Transition transition = transitions.get(predicate);
                    transition.sequence.trigger();
                    return transition.state;
                }
            }
            for (Command loop : loops) {
                loop.run();
            }
            return this;
        }
    }

    private State currentState;

    public CommandMachine(State startState) {
        currentState = startState;
    }

    public CommandMachine addTransition(State initialState, State finalState, Supplier<Boolean> condition,
            CommandSequence transition, boolean transitionAtEnd) {
        initialState.addTransition(condition, finalState, transition, transitionAtEnd);
        return this;
    }

    public CommandMachine addLoop(State state, Command loop) {
        state.addLoop(loop);
        return this;
    }

    public void run() {
        currentState = currentState.run();
    }
}
