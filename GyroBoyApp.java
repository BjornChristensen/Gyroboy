import lejos.hardware.BrickFinder;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.port.SensorPort;

/**
 * GyroBoyApp is the main application for GyroBoy.
 * It has a GyroBoy object to balance and drive the robot around.
 * It has an IR_RemoteControl object to control GyroBoy with an IR Beacon.
 * It has an UltrasonicControl object to prevent GyroBoy from bumping into things.
 * GyroBoy and all controllers run in separate threads.
 * 
 * @author Bjørn Christensen, 20/3-2017
*/
public class GyroBoyApp {

	public static void main(String[] args) {
    GraphicsLCD g = BrickFinder.getDefault().getGraphicsLCD();
    final int SW = g.getWidth();
    final int SH = g.getHeight();
    g.setFont(Font.getLargeFont());
    g.drawString("GyroBoy", SW/2, 40, GraphicsLCD.BASELINE|GraphicsLCD.HCENTER);

    GyroBoy gyroboy = new GyroBoy();
		gyroboy.start();
		gyroboy.setSpeed(2);
		
		IR_RemoteControl ir=new IR_RemoteControl(gyroboy, SensorPort.S1);
		ir.start();

		UltrasonicControl usc=new UltrasonicControl(gyroboy);
		usc.start();
	}
}
