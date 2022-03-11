package com.stuyfission.fissionlib.motion;

import com.acmerobotics.roadrunner.control.PIDFController;
import com.acmerobotics.roadrunner.profile.MotionProfile;
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator;
import com.acmerobotics.roadrunner.profile.MotionState;
import com.acmerobotics.roadrunner.control.PIDCoefficients;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

/**
 * MotionProfiledDcMotor is a class that implements the DcMotorEx class and adds on motion
 * profiling functionality to a motor. It is designed to make utilizing motion profiling more
 * straightforward and to de-clutter other classes.
 *
 * @version 1.0
 * @since 1.1.0-alpha
 *
 * @author Paul Serbanescu (paulserbanescu3@gmail.com)
 */
public abstract class MotionProfiledDcMotor implements DcMotorEx {

    /**
     * Motor definition for class
     */
    protected MotionProfiledDcMotor motor;

    /**
     * Empirical wheel constants
     */
    private Double WHEEL_RADIUS;
    private Double GEAR_RATIO;
    private Double TICKS_PER_REV;

    /**
     * Motion constraints - determined through experimentation
     */
    private Double MAX_VEL;
    private Double MAX_ACCEL;

    /**
     * Motion profile
     */
    private MotionProfile profile;
    private final ElapsedTime profileTimer = new ElapsedTime();

    /**
     * PID controller for motion profile
     */
    private PIDFController controller;

    /**
     * Initializes MotionProfiledDcMotor from the hardware map and sets motor modes needed for
     * motion profiling
     *
     * @param hwMap robot's hardware map
     * @param deviceName name on hardware map
     */
    public void initialize(HardwareMap hwMap, String deviceName) {
        motor = (MotionProfiledDcMotor) hwMap.get(DcMotorEx.class, deviceName);
        motor.setMode(RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    /**
     * Sets constants to be used in {@link #encoderTicksToInches(double) encoderTicksToInches} to
     * convert motor movement which is reported in ticks to inches.
     *
     * @param WHEEL_RADIUS radius of wheel (spool, etc.) motor is attached to
     * @param GEAR_RATIO ratio of input (motor) speed to output (wheel) speed
     * @param TICKS_PER_REV found in your motor's spec sheet
     */
    public void setWheelConstants(double WHEEL_RADIUS, double GEAR_RATIO, double TICKS_PER_REV) {
        this.WHEEL_RADIUS = WHEEL_RADIUS;
        this.GEAR_RATIO = GEAR_RATIO;
        this.TICKS_PER_REV = TICKS_PER_REV;
    }

    /**
     * Defines the maximum speed and acceleration your motor will perform (inches/s)
     *
     * @param MAX_VEL (in/s)
     * @param MAX_ACCEL (in/s^2)
     * */
    public void setMotionConstraints(double MAX_VEL, double MAX_ACCEL) {
        this.MAX_VEL = MAX_VEL;
        this.MAX_ACCEL = MAX_ACCEL;
    }

    /**
     * Sets PID gains to be used by the PIDF controller
     *
     * @param kP - proportional gain
     * @param kI - integral gain
     * @param kD - derivative gain
     * @param kF - kF
     */
    public void setPIDCoefficients(double kP, double kI, double kD, double kF) {
        PIDCoefficients coeffs = new PIDCoefficients(kP, kI, kD);
        this.controller = new PIDFController(coeffs, 0, 0, 0, (position, velocity) -> kF);
    }

    /**
     * Converts motor ticks to inches of rotation
     *
     * @param ticks motor ticks
     * @return inches
     */
    private final double encoderTicksToInches(double ticks) {
        try {
            return WHEEL_RADIUS * 2 * Math.PI * GEAR_RATIO * ticks / TICKS_PER_REV;
        } catch (NullPointerException e) {
            RobotLog.setGlobalErrorMsg("%s wheel constants not set. make sure to setConstants(double, double, double) "
                    + getClass().getSimpleName());
        }
        return 0.0;
    }

    /**
     * Returns motor position
     *
     * @return inches away from starting position
     */
    public double getPosition() {
        return encoderTicksToInches(motor.getCurrentPosition());
    }

    /**
     * Returns motor velocity
     *
     * @return motor velocity in (in/s)
     */
    public double getVelocity() {
        return encoderTicksToInches(motor.getVelocity());
    }

    /**
     * Generates a motion profile
     *
     * @param targetPosition inches
     * @return motion profile
     */
    public MotionProfile generateProfile(double targetPosition) {
        MotionState start = new MotionState(getPosition(), getVelocity(), 0, 0);
        MotionState goal = new MotionState(targetPosition, 0, 0, 0);

        try {
            return MotionProfileGenerator.generateSimpleMotionProfile(start, goal, MAX_VEL, MAX_ACCEL);
        } catch (NullPointerException e) {
            RobotLog.setGlobalErrorMsg("%s motion constraints not set. make sure to setMotionConstraints(double, double) "
                    + getClass().getSimpleName());
        }
        return MotionProfileGenerator.generateSimpleMotionProfile(start, goal, 0, 0);
    }

    /**
     * Uses {@link #generateProfile(double) generateProfile} to set the motor's target position
     * using the motion profile
     *
     * @param targetPosition inches
     */
    public void setTargetPosition(double targetPosition) {
        profile = generateProfile(targetPosition);
        profileTimer.reset();
    }

    /**
     * Motion profiles are time-based, this method must be called
     * inside the {@link com.stuyfission.fissionlib.util.Mechanism#loop(Gamepad) loop} of the class
     * otherwise the profile will not work
     */
    public void update() {
        MotionState state = profile.get(profileTimer.seconds());

        controller.setTargetPosition(state.getX());
        controller.setTargetVelocity(state.getV());
        controller.setTargetAcceleration(state.getA());

        double power = controller.update(getPosition(), getVelocity());

        motor.setPower(power);
    }

}
