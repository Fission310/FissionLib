package com.stuyfission.fissionlib.motion;

import com.acmerobotics.roadrunner.control.PIDFController;
import com.acmerobotics.roadrunner.profile.MotionProfile;
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator;
import com.acmerobotics.roadrunner.profile.MotionState;
import com.acmerobotics.roadrunner.control.PIDCoefficients;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;


public abstract class MotionProfiledDcMotor implements DcMotorEx {

    protected MotionProfiledDcMotor motor;

    private Double WHEEL_RADIUS;
    private Double GEAR_RATIO;
    private Double TICKS_PER_REV;

    private Double MAX_VEL;
    private Double MAX_ACCEL;

    private MotionProfile profile;
    private ElapsedTime profileTimer = new ElapsedTime();

    private PIDCoefficients coeffs;
    private double kF;
    private PIDFController controller;

    public void initialize(HardwareMap hwMap, String deviceName) {
        motor = (MotionProfiledDcMotor) hwMap.get(DcMotorEx.class, deviceName);
        motor.setMode(RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    // Motion profile specific
    public void setWheelConstants(double WHEEL_RADIUS, double GEAR_RATIO, double TICKS_PER_REV) {
        this.WHEEL_RADIUS = WHEEL_RADIUS;
        this.GEAR_RATIO = GEAR_RATIO;
        this.TICKS_PER_REV = TICKS_PER_REV;
    }

    public void setMotionConstraints(double MAX_VEL, double MAX_ACCEL) {
        this.MAX_VEL = MAX_VEL;
        this.MAX_ACCEL = MAX_ACCEL;
    }

    public void setPIDCoefficients(double kP, double kI, double kD, double kF) {
        this.coeffs = new PIDCoefficients(kP, kI, kD);
        this.kF = kF;

        this.controller = new PIDFController(coeffs, 0, 0, 0, (position, velocity) -> kF);
    }

    public final double encoderTicksToInches(double ticks) {
        try {
            return WHEEL_RADIUS * 2 * Math.PI * GEAR_RATIO * ticks / TICKS_PER_REV;
        } catch (NullPointerException e) {
            RobotLog.setGlobalErrorMsg("%s wheel constants not set. make sure to setConstants(double, double, double) "
                    + getClass().getSimpleName());
        }
        return 0.0;
    }

    public double getPosition() {
        return encoderTicksToInches(motor.getCurrentPosition());
    }

    public double getVelocity() {
        return encoderTicksToInches(motor.getVelocity());
    }

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

    public void setTargetPosition(double targetPosition) {
        profile = generateProfile(targetPosition);
        profileTimer.reset();
    }

    public void update() {
        MotionState state = profile.get(profileTimer.seconds());

        controller.setTargetPosition(state.getX());
        controller.setTargetVelocity(state.getV());
        controller.setTargetAcceleration(state.getA());

        double power = controller.update(getPosition(), getVelocity());

        motor.setPower(power);
    }

}
