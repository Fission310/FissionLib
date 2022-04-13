package com.stuyfission.fissionlib.motion;

import com.acmerobotics.roadrunner.control.PIDCoefficients;
import com.acmerobotics.roadrunner.control.PIDFController;
import com.acmerobotics.roadrunner.profile.MotionProfile;
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator;
import com.acmerobotics.roadrunner.profile.MotionState;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

public class MotionProfiledDcMotor implements DcMotorSimple {
    /**
     * Motor definition for class
     */
    protected DcMotorEx motor;

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
     * Specifically for linear slides, multiplies MAX_VEL and MAX_ACCEL when retracting
     * retracting is defined as setting target position to a position less than current position
     */
    private Double RETRACTION_MULTIPLIER;


    /**
     * PID controller for motion profile
     */
    private PIDFController PIDcontroller;

    public MotionProfiledDcMotor(HardwareMap hwMap, String deviceName) {
        motor = hwMap.get(DcMotorEx.class, deviceName);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        RETRACTION_MULTIPLIER = 1.0;
    }


    /**
     * Initializes MotionProfiledDcMotor from the hardware map and sets motor modes needed for
     * motion profiling
     *
     * @param hwMap robot's hardware map
     * @param deviceName name on hardware map
     */
    public void initialize(HardwareMap hwMap, String deviceName) {
        motor = hwMap.get(DcMotorEx.class, deviceName);
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
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
     * @param kP proportional gain
     * @param kI integral gain
     * @param kD derivative gain
     * @param kF kF
     */
    public void setPIDCoefficients(double kP, double kI, double kD, double kF) {
        PIDCoefficients coeffs = new PIDCoefficients(kP, kI, kD);
        this.PIDcontroller = new PIDFController(coeffs, 0, 0, 0, (position, velocity) -> kF);
    }

    /**
     * Sets the retraction multiplier
     * @param multiplier {@link Double RETRACTION_MULTIPLIER}
     */
    public void setRetractionMultiplier(double multiplier) {
        this.RETRACTION_MULTIPLIER = multiplier;
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
    public MotionProfile generateProfile(double targetPosition, Double maxVel, Double maxAccel) {
        MotionState start = new MotionState(getPosition(), getVelocity(), 0, 0);
        MotionState goal = new MotionState(targetPosition, 0, 0, 0);

        try {
            return MotionProfileGenerator.generateSimpleMotionProfile(start, goal, maxVel, maxAccel);
        } catch (NullPointerException e) {
            RobotLog.setGlobalErrorMsg("%s motion constraints not set. make sure to setMotionConstraints(double, double) "
                    + getClass().getSimpleName());
        }
        return MotionProfileGenerator.generateSimpleMotionProfile(start, goal, 0, 0);
    }

    /**
     * Uses {@link #generateProfile(double, Double, Double) generateProfile} to set the motor's target position
     * using the motion profile
     *
     * @param targetPosition inches
     */
    public void setTargetPosition(double targetPosition) {
        if (targetPosition < getPosition()) { profile = generateProfile(targetPosition, MAX_VEL * RETRACTION_MULTIPLIER, MAX_ACCEL * RETRACTION_MULTIPLIER); }
        else { profile = generateProfile(targetPosition, MAX_VEL, MAX_ACCEL); }
        profileTimer.reset();
    }

    public void setTargetPosition(double targetPosition, double retractionMultiplier) {
        if (targetPosition < getPosition()) { profile = generateProfile(targetPosition, MAX_VEL * retractionMultiplier, MAX_ACCEL * RETRACTION_MULTIPLIER); }
        else { profile = generateProfile(targetPosition, MAX_VEL, MAX_ACCEL); }
        profileTimer.reset();
    }

    /**
     * Overrides DcMotor method setTargetPosition(int) to utilize motion profile
     *
     * @param targetPosition inches
     */
    public void setTargetPosition(int targetPosition) {
        setTargetPosition((double) targetPosition);
    }

    /**
     * Motion profiles are time-based, this method must be called
     * inside the {@link com.stuyfission.fissionlib.util.Mechanism#loop(Gamepad) loop} of the class
     * otherwise the profile will not work
     */
    public void update() {
        MotionState state = profile.get(profileTimer.seconds());

        PIDcontroller.setTargetPosition(state.getX());
        PIDcontroller.setTargetVelocity(state.getV());
        PIDcontroller.setTargetAcceleration(state.getA());

        double power = PIDcontroller.update(getPosition(), getVelocity());

        motor.setPower(power);
    }


    // =======================
    // pass thru functionality
    // =======================

    /**
     * Returns an indication of the manufacturer of this device.
     *
     * @return the device's manufacturer
     */
    @Override
    public Manufacturer getManufacturer() {
        return motor.getManufacturer();
    }

    /**
     * Returns a string suitable for display to the user as to the type of device.
     * Note that this is a device-type-specific name; it has nothing to do with the
     * name by which a user might have configured the device in a robot configuration.
     *
     * @return device manufacturer and name
     */
    @Override
    public String getDeviceName() {
        return motor.getDeviceName();
    }

    /**
     * Get connection information about this device in a human readable format
     *
     * @return connection info
     */
    @Override
    public String getConnectionInfo() {
        return motor.getConnectionInfo();
    }

    /**
     * Version
     *
     * @return get the version of this device
     */
    @Override
    public int getVersion() {
        return motor.getVersion();
    }

    /**
     * Resets the device's configuration to that which is expected at the beginning of an OpMode.
     * For example, motors will reset the their direction to 'forward'.
     */
    @Override
    public void resetDeviceConfigurationForOpMode() {
        motor.resetDeviceConfigurationForOpMode();
    }

    /**
     * Closes this device
     */
    @Override
    public void close() {
        motor.close();
    }

    /**
     * Sets the logical direction in which this motor operates.
     *
     * @param direction the direction to set for this motor
     * @see #getDirection()
     */
    @Override
    public void setDirection(Direction direction) {
        motor.setDirection(direction);
    }

    /**
     * Returns the current logical direction in which this motor is set as operating.
     *
     * @return the current logical direction in which this motor is set as operating.
     * @see #setDirection(Direction)
     */
    @Override
    public Direction getDirection() {
        return motor.getDirection();
    }

    /**
     * Sets the power level of the motor, expressed as a fraction of the maximum
     * possible power / speed supported according to the run mode in which the
     * motor is operating.
     *
     * <p>Setting a power level of zero will brake the motor</p>
     *
     * @param power the new power level of the motor, a value in the interval [-1.0, 1.0]
     * @see #getPower()
     * @see DcMotor#setMode(DcMotor.RunMode)
     * @see DcMotor#setPowerFloat()
     */
    @Override
    public void setPower(double power) {
        motor.setPower(power);
    }

    /**
     * Returns the current configured power level of the motor.
     *
     * @return the current level of the motor, a value in the interval [0.0, 1.0]
     * @see #setPower(double)
     */
    @Override
    public double getPower() {
        return motor.getPower();
    }

    public boolean profileDone(){
        return profileTimer.seconds() > profile.duration();
    }
}
