package org.firstinspires.ftc.teamcode.systems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.teamcode.Settings;

import java.util.HashMap;
import java.util.Map;

public class Drivetrain {

    public final DcMotor frontLeftMotor;
    public final DcMotor frontRightMotor;
    public final DcMotor rearLeftMotor;
    public final DcMotor rearRightMotor;
    public State state = State.DEFAULT;

    public final Map<String, DcMotor> motors = new HashMap<>();

    public Drivetrain(HardwareMap hardwareMap) {
        frontLeftMotor = hardwareMap.get(DcMotor.class, Settings.Hardware.IDs.FRONT_LEFT_MOTOR);
        frontRightMotor = hardwareMap.get(DcMotor.class, Settings.Hardware.IDs.FRONT_RIGHT_MOTOR);
        rearLeftMotor = hardwareMap.get(DcMotor.class, Settings.Hardware.IDs.REAR_LEFT_MOTOR);
        rearRightMotor = hardwareMap.get(DcMotor.class, Settings.Hardware.IDs.REAR_RIGHT_MOTOR);

        // IF A WHEEL IS GOING THE WRONG DIRECTION CHECK WIRING red/black
        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
        rearLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        rearRightMotor.setDirection(DcMotor.Direction.REVERSE);

        frontLeftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        frontRightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rearLeftMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rearRightMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        motors.put(Settings.Hardware.IDs.FRONT_LEFT_MOTOR, frontLeftMotor);
        motors.put(Settings.Hardware.IDs.FRONT_RIGHT_MOTOR, frontRightMotor);
        motors.put(Settings.Hardware.IDs.REAR_LEFT_MOTOR, rearLeftMotor);
        motors.put(Settings.Hardware.IDs.REAR_RIGHT_MOTOR, rearRightMotor);
    }

    /**
     * Implements mecanum drive calculations and motor control
     *
     * @param drivePower  Forward/backward power (-1.0 to 1.0)
     * @param strafePower Left/right strafe power (-1.0 to 1.0)
     * @param rotation    Rotational power (-1.0 to 1.0)
     **/
    public void mecanumDrive(double drivePower, double strafePower, double rotation, double flip, boolean asDeadeye) {
        if (state == State.DEADEYE_ENABLED && !asDeadeye) {
            return; // deadeye is handling driving, do not manually drive
        }
        // Adjust the values for strafing and rotation
        strafePower *= Settings.Teleop.strafe_power_coefficient;
        double frontLeft = (drivePower + strafePower) * flip + rotation;
        double frontRight = (drivePower - strafePower) * flip - rotation;
        double rearLeft = (drivePower - strafePower) * flip + rotation;
        double rearRight = (drivePower + strafePower) * flip - rotation;

        frontLeftMotor.setPower(frontLeft);
        frontRightMotor.setPower(frontRight);
        rearLeftMotor.setPower(rearLeft);
        rearRightMotor.setPower(rearRight);
    }

    // mecanum drive assumes you are not driving as deadeye
    public void mecanumDrive(double drivePower, double strafePower, double rotation, double flip) {
        mecanumDrive(drivePower, strafePower, rotation, flip, false);
    }

    public void interpolateToOffset(double offsetX, double offsetY, double offsetHeading) {
        double drivePower = -offsetY;
        double strafePower = Range.clip((-offsetX * 1.2) / Settings.Assistance.inverseLateralMultiplier, -1, 1);
        // offsetHeading is -Pi/2 to pi/2, where 0 is the target heading
        double rotation = Range.clip(offsetHeading, -Math.PI / 2, Math.PI / 2) / (Math.PI / 2);
        rotation = Math.abs(rotation) < Settings.Assistance.minimumRotationCorrectionThreshold ? 0 : rotation;
        mecanumDrive(drivePower, strafePower, rotation);
    }

    public void mecanumDrive(double drivePower, double strafePower, double rotation) {
        mecanumDrive(drivePower, strafePower, rotation, 1, true);
    }

    public enum State {
        DEFAULT,
        DEADEYE_ENABLED,
    }
}
