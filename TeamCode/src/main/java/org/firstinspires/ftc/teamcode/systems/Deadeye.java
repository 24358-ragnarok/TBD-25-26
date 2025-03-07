package org.firstinspires.ftc.teamcode.systems;

import com.pedropathing.localization.GoBildaPinpointDriver;

import org.firstinspires.ftc.teamcode.Settings;
import org.firstinspires.ftc.teamcode.input.Controller;
import org.firstinspires.ftc.teamcode.mechanisms.MechanismManager;

public class Deadeye {
    private final Controller controller;
    private final Drivetrain drivetrain;
    private final MechanismManager mechanisms;
    private final GoBildaPinpointDriver pinpoint;
    private double storedTx;

    public Deadeye(Controller controller, Drivetrain drivetrain, MechanismManager mechanisms, GoBildaPinpointDriver pinpoint) {
        this.controller = controller;
        this.drivetrain = drivetrain;
        this.mechanisms = mechanisms;
        this.pinpoint = pinpoint;
        this.storedTx = 0;
    }

    public void check() {
        boolean specimenDetected = mechanisms.intake.limelight.specimenDetected();
        boolean headingAligned = Math.abs(wrappedHeading()) < 10; // TODO make this a setting

        if (specimenDetected && headingAligned) {
            if (controller.isActive("deadeye")) {
                drivetrain.state = Drivetrain.State.DEADEYE_ENABLED;
                double Tx = mechanisms.intake.limelight.limelight.getLatestResult().getTx();
                controller.setLedColor(0, 0, 255, 1000);
                drivetrain.interpolateToOffset(
                        Tx,
                        Settings.Assistance.approachSpeed,
                        wrappedHeading());
                storedTx = Tx;
            } else {
                controller.setLedColor(0, 255, 0, 1000);
                controller.rumble(50);
                drivetrain.state = Drivetrain.State.DEFAULT;
            }
        } else if ((drivetrain.state == Drivetrain.State.DEADEYE_ENABLED) && controller.isActive("deadeye") && storedTx != 0) {
            controller.setLedColor(255, 0, 255, 1000);
            drivetrain.interpolateToOffset(
                    mechanisms.intake.limelight.limelight.getLatestResult().getTx(),
                    Settings.Assistance.approachSpeed,
                    wrappedHeading());
        } else {
            storedTx = 0;
            if (controller.isActive("deadeye")) {
                controller.setLedColor(0, 255, 255, 1000);
                drivetrain.interpolateToOffset(0, 0, wrappedHeading());
                drivetrain.state = Drivetrain.State.DEADEYE_ENABLED;
            } else {
                controller.setLedColor(255, 0, 0, 1000);
                drivetrain.state = Drivetrain.State.DEFAULT;
            }
        }
    }

    private double wrappedHeading() {
        return (pinpoint.getHeading() + Math.PI) % (2 * Math.PI) - Math.PI;
    }
}
