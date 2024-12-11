package com.stuyfission.fissionlib.input;

import java.util.HashMap;

import com.qualcomm.robotcore.hardware.Gamepad;

public class GamepadWrapper {
    private Gamepad gamepad;
    private HashMap<Input, Boolean> clicked;

    public GamepadWrapper(Gamepad gamepad) {
        this.gamepad = gamepad;
        this.clicked = new HashMap<>();
        for (Input input : Input.values()) {
            clicked.put(input, false);
        }
    }

    public void update() {
        for (Input input : Input.values()) {
            if (!isPressed(input)) {
                clicked.put(input, false);
            }
        }
    }

    public boolean isClicked(Input button) {
        if (isPressed(button)) {
            if (!clicked.get(button)) {
                clicked.put(button, true);
                return true;
            }
        } else {
            clicked.put(button, false);
        }
        return false;
    }

    public boolean isPressed(Input button) {
        switch (button) {
            case DPAD_UP:
                return gamepad.dpad_up;
            case DPAD_DOWN:
                return gamepad.dpad_down;
            case DPAD_LEFT:
                return gamepad.dpad_left;
            case DPAD_RIGHT:
                return gamepad.dpad_right;
            case A:
                return gamepad.a;
            case B:
                return gamepad.b;
            case X:
                return gamepad.x;
            case Y:
                return gamepad.y;
            case START:
                return gamepad.start;
            case BACK:
                return gamepad.back;
            case LEFT_BUMPER:
                return gamepad.left_bumper;
            case RIGHT_BUMPER:
                return gamepad.right_bumper;
            case LEFT_STICK_BUTTON:
                return gamepad.left_stick_button;
            case RIGHT_STICK_BUTTON:
                return gamepad.right_stick_button;
            case LEFT_TRIGGER:
                return gamepad.left_trigger > 0;
            case RIGHT_TRIGGER:
                return gamepad.right_trigger > 0;
            case NONE:
                return false;
            default:
                return false;
        }
    }
}
