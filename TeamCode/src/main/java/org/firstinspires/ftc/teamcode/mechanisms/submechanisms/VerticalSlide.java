package org.firstinspires.ftc.teamcode.mechanisms.submechanisms;

import static org.firstinspires.ftc.teamcode.Settings.Hardware.VerticalSlide.IDLE_POWER;
import static org.firstinspires.ftc.teamcode.Settings.Hardware.VerticalSlide.MOVEMENT_POWER;

import androidx.annotation.NonNull;

import com.qualcomm.hardware.rev.RevTouchSensor;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.Settings;

public class VerticalSlide implements ViperSlide {
    public final DcMotor verticalMotorLeft;
    public final DcMotor verticalMotorRight;
    private final RevTouchSensor touchSensor;
    private double encoderTarget;
    private VerticalPosition currentPosition;
    private int currentPositionValue;

    private double currentOffset;

    public VerticalSlide(DcMotor verticalMotorLeft, DcMotor verticalMotorRight, RevTouchSensor verticalMotorTouchSensor) {
        this.verticalMotorLeft = verticalMotorLeft;
        this.verticalMotorRight = verticalMotorRight;
        this.touchSensor = verticalMotorTouchSensor;
    }

    // Sets target position
    @Override
    public void setPosition(double position) {
        int targetPosition = (int) position;
        verticalMotorLeft.setTargetPosition(targetPosition);
        verticalMotorRight.setTargetPosition(targetPosition);
    }

    // Converts position name to double
    public void setPosition(@NonNull VerticalPosition position) {
        this.currentPosition = position;
        encoderTarget = position.getValue();
        this.setPosition(encoderTarget); // Use the value associated with the enum
    }

    public void extend() {
        // Move to the next position in the enum, looping back to the start if needed
        VerticalPosition[] positions = VerticalPosition.values();
        currentPositionValue = (currentPositionValue + 1) % positions.length;
        encoderTarget = positions[currentPositionValue].getValue();
        setPosition(encoderTarget);
    }

    @Override
    public void retract() {
        // Move to the previous position in the enum, looping back if needed
        VerticalPosition[] positions = VerticalPosition.values();
        currentPositionValue = (currentPositionValue - 1 + positions.length) % positions.length;
        encoderTarget = positions[currentPositionValue].getValue();
        setPosition(encoderTarget);
    }

    @Override
    public void max() {
        setPosition(VerticalPosition.HIGH_BASKET);
    }

    @Override
    public void increment() {
        if (encoderTarget - currentOffset < VerticalPosition.HIGH_BASKET.getValue()) {
            encoderTarget += Settings.Hardware.VerticalSlide.INCREMENTAL_MOVEMENT_POWER;
        }
        setPosition(encoderTarget);
    }

    public void increment(int encoderTicks) {
        if (encoderTarget - currentOffset < VerticalPosition.HIGH_BASKET.getValue()) {
            encoderTarget += encoderTicks;
        }
        setPosition(encoderTarget);
    }

    @Override
    public void decrement() {
        if (!Settings.Hardware.VerticalSlide.ENABLE_LOWER_LIMIT || !touchSensor.isPressed() || encoderTarget - currentOffset > VerticalPosition.TRANSFER.getValue()) {
            encoderTarget -= Settings.Hardware.VerticalSlide.INCREMENTAL_MOVEMENT_POWER;
        }
        setPosition(encoderTarget);
    }

    public boolean isTouchingSensor() {
        return touchSensor.isPressed();
    }

    public void checkMotors() {
        if (Math.abs(verticalMotorRight.getCurrentPosition() - encoderTarget) < 5) {
            verticalMotorRight.setPower(IDLE_POWER);
            verticalMotorLeft.setPower(IDLE_POWER);
        } else if (encoderTarget > verticalMotorRight.getCurrentPosition()) {
            verticalMotorRight.setPower(MOVEMENT_POWER);
            verticalMotorLeft.setPower(MOVEMENT_POWER);
        } else {
            verticalMotorRight.setPower(-MOVEMENT_POWER);
            verticalMotorLeft.setPower(-MOVEMENT_POWER);
        }
    }

    public void setToZero() {
        currentOffset = encoderTarget;
    }

    public void reset() {
        verticalMotorLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        verticalMotorRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    }

    public void init() {
        verticalMotorRight.setDirection(DcMotor.Direction.REVERSE);

        setPosition(VerticalPosition.TRANSFER);

        verticalMotorLeft.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        verticalMotorRight.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        this.currentPosition = VerticalPosition.TRANSFER;
        encoderTarget = verticalMotorRight.getTargetPosition();
    }
}
