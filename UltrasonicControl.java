import lejos.hardware.Button;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/**
 * UltrasonicControl uses the ultrasonic sensor to keep GyroBoy from bumping into things.
 * When GyroBoy is closer than 20 cm from any obstacle it will stop and buzz.
 * UltrasonicControl runs in a separate thread.
 * @author Bjørn Christensen, 20/3-2017
 */
public class UltrasonicControl extends Thread {
	private EV3UltrasonicSensor us=new EV3UltrasonicSensor(SensorPort.S4);
	private SampleProvider sp=us.getDistanceMode();
	private float[] sample=new float[sp.sampleSize()];
	private GyroBoy gyroboy;
	
	/**
	 * @param boy - reference to the GyroBoy object to control.
	 */
	UltrasonicControl(GyroBoy boy) {
		gyroboy=boy;
	}
	
	
	/**
	 * Continuously read the distance to the closest object.
	 * If distance is less than 20 cm stop and buzz.
	 */
	public void run() {
		boolean buzzReady=true;				// Only one buzz pr. stop !

		while (!Button.ESCAPE.isDown()) {
			Delay.msDelay(100);					// 
			float distance=getDistance();
			if (distance<0.2) {					// distance<20 cm => stop gyroboy and buzz
				gyroboy.setSpeed(0);
				if (buzzReady) { 
					Sound.buzz(); 
					gyroboy.moveArms(); 
					buzzReady=false; 
				}
			}
			if (distance>0.4) buzzReady=true;		// distance>40 cm => reset buzzReady
		}
	}
	
	/**
	 * return the distance in meters to any object.
	 */
	private float getDistance() {
		sp.fetchSample(sample, 0);
		return sample[0];
	}

}
