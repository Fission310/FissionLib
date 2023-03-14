package com.stuyfission.fissionlib.input;

import com.qualcomm.robotcore.hardware.Gamepad;

public class GamepadStatic {

//    private Gamepad gamepad;

    public enum INPUT {
        NONE,
        DPAD_UP,
        DPAD_DOWN,
        DPAD_LEFT,
        DPAD_RIGHT,
        A,
        B,
        X,
        Y,
        START,
        BACK,
        LEFT_BUMPER,
        RIGHT_BUMPER,
        LEFT_STICK_BUTTON,
        RIGHT_STICK_BUTTON
    }

    public GamepadStatic() {}

    public static INPUT gamepadToEnum(Gamepad gamepad) {
        if (gamepad.dpad_up) return setDPadUp();
        if (gamepad.dpad_down) return setDPadDown();
        if (gamepad.dpad_left) return setDpadLeft();
        if (gamepad.dpad_right) return setDpadRight();
        if (gamepad.a) return setA();
        if (gamepad.b) return setB();
        if (gamepad.x) return setX();
        if (gamepad.y) return setY();
        if (gamepad.start) return setStart();
        if (gamepad.back) return setBack();
        if (gamepad.left_bumper) return setLeftBumper();
        if (gamepad.right_bumper) return setRightBumper();
        if (gamepad.left_stick_button) return setLeftStickButton();
        if (gamepad.right_stick_button) return setRightStickButton();

        return INPUT.NONE;
    }

    public static INPUT setDPadUp() {
        return INPUT.DPAD_UP;
    }

    public static INPUT setDPadDown() {
        return INPUT.DPAD_DOWN;
    }

    public static INPUT setDpadLeft() {
        return INPUT.DPAD_LEFT;
    }

    public static INPUT setDpadRight() {
        return INPUT.DPAD_RIGHT;
    }

    public static INPUT setA() {
        return INPUT.A;
    }

    public static INPUT setB() {
        return INPUT.B;
    }

    public static INPUT setX() {
        return INPUT.X;
    }

    public static INPUT setY() {
        return INPUT.Y;
    }

    public static INPUT setStart() {
        return INPUT.START;
    }

    public static INPUT setBack() {
        return INPUT.BACK;
    }

    public static INPUT setLeftBumper() {
        return INPUT.LEFT_BUMPER;
    }

    public static INPUT setRightBumper() {
        return INPUT.RIGHT_BUMPER;
    }

    public static INPUT setLeftStickButton() {
        return INPUT.LEFT_STICK_BUTTON;
    }

    public static INPUT setRightStickButton() {
        return INPUT.RIGHT_STICK_BUTTON;
    }

}
