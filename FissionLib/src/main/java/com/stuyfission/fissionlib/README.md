# FissionLib

This library is designed to streamline and simplify various aspects of robot code.

The flow is as follows:
Mechanism
&darr;
Hardware (all robot hardware extends the Mechanism class)
&darr;
Implement methods: (init(), loop(), telemetry())
&darr;
OpMode

### Mechanism
All hardware mechanisms on the bot (ex: intake, arm, slides) should extend the Mechanism class
#### init(HardwareMap hwMap)
Where hardware is initialized from the hardware map on the robot phone. Ex:
`servo = hwMap.get(Servo.class, "servo");`
#### loop(Gamepad gamepad)
Where gamepad inputs are passed to control hardware. Ex:
```
if (gamepad.y) {
    servo.lift();
}
```
#### loop(Gamepad gamepad1, Gamepad gamepad2)
If using multiple gamepads use this method
#### telemetry(Telemetry telemetry)
Telemetry is defined and updated inside opModes, but you can still define what data you want added inside this method. Ex:
```
telemetry.addData("servo position", servo.getPosition());
```
inside an opMode:
```
while (opModeIsActive() && !isStopRequested()) {
    myServo.loop(gamepad1);
    myServo.telemetry(telemetry);
    telemetry.update();
}
```
