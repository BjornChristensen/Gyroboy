import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.Port;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3IRSensor;
import lejos.hardware.sensor.SensorMode;
import lejos.utility.Delay;
/**
 * IR_RemoteControl uses the IR sensor and and beacon as a remote control
 * for the GyroBoy. It runs in a separate thread.
 * It can receive the following commands:
 * Command  Button on beacon						  Action
 *   1		  Top left					 						Increase forward speed
 *   2			Bottom left       						Turn left while holding key down
 *   3			Top right				 		  				Decrease forward speed
 *   4			Bottom right      						Turn right while holding key down
 *   5      Top left+right		 						Stop
 *   8      Bottom left + Bottom right	 	Pirouette
 *   9			Top middle				 						Follow the beacon
 *  10      Top left + Bottom left			  Circle left
 *  11			Top right + Bottom right			Circle right
 *    
 * @author Bjørn Christensen, 20/3-2017
 */
public class IR_RemoteControl extends Thread {
	private EV3IRSensor ir;
	private final int channel = 1;
	private GyroBoy gyroboy;
	
	/**
	 * @param boy - reference to the GyroBoy object to remotely control.
	 * @param port - sensor port that the IR sensor is plugged into.
	 */
	IR_RemoteControl(GyroBoy boy, Port port) {
		gyroboy=boy;
		ir = new EV3IRSensor(port);
	}
	
	public void run() {
		SensorMode seeker = ir.getMode("Seek");	// used when following the beacon
		float [] vals = new float[8];					  // buffer for sensor readings
		boolean pirouette=false;								// Gyroboy is doing a pirouette or a circle

		while (!Button.ESCAPE.isDown()) {
			Delay.msDelay(100);
			int command=ir.getRemoteCommand(channel-1);
			switch (command) {
			case 0:	// No command: Stop turn
				if (!pirouette)
					gyroboy.turn(0);		// turn only when holding button down
				break;
			case 1: // Top left: Forward
				gyroboy.increaseSpeed(2);
				pirouette=false;
				break;
			case 2: // Bottom left: Turn left
				gyroboy.turn(-10);
				pirouette=false;
				break;
			case 3: // Top right: Backward
					gyroboy.increaseSpeed(-2);
					pirouette=false;
				break;
			case 4: // Bottom right: Turn right
				gyroboy.turn(10);
				pirouette=false;
				break;
			case 5: // Top left + Top right: Full brake
				gyroboy.setSpeed(0);
				pirouette=false;
				break;
			case 6: // Top left + Bottom right
				break;
			case 7: // Top right + Bottom left
				break;
			case 8: // Bottom left + Bottom right: Pirouette
				pirouette=true;
				gyroboy.setSpeed(0);
				gyroboy.turn(50);
				break;
			case 9: // Top center: Follow Beacon
				pirouette=false;
				seeker.fetchSample(vals, 0);
				int bearing = (int)vals[channel*2-2];		 // negative => turn left, positive => turn right
				int distance = (int)vals[channel*2-1];   // distance in cm to IR Beacon
//				LCD.drawString("B: " + bearing + " R: " + distance + "cm   ", 0, 5);
				if (distance<50)
					gyroboy.increaseSpeed(-1);
				else 
					gyroboy.increaseSpeed(1);
				gyroboy.turn(bearing);
				break;
			case 10: // Top left + Bottom left: Circle left
				pirouette=true;
				gyroboy.setSpeed(5);
				gyroboy.turn(-5);
				break;
			case 11: // Top right + Bottom right: Circle right
				pirouette=true;
				gyroboy.setSpeed(5);
				gyroboy.turn(5);
				break;
			}
		}
		ir.close();
	}
	
}
