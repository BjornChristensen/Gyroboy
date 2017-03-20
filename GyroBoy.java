import lejos.hardware.Button;
import lejos.hardware.Key;
import lejos.hardware.KeyListener;
import lejos.hardware.Sound;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.EV3MediumRegulatedMotor;
import lejos.hardware.motor.UnregulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3GyroSensor;
import lejos.robotics.SampleProvider;
import lejos.utility.Delay;

/**
 * The GyroBoy class represents the robot with it motors and gyro sensor.
 * This class will keep the balance of the robot. It is done in the run() method.
 * It also provides methods to set the speed and direction of the robot and move the arms.
 * 
 * @author Bjørn Christensen, 20/3-2017
 */
public class GyroBoy extends Thread {
	private UnregulatedMotor rightMotor = new UnregulatedMotor(MotorPort.A);
	private UnregulatedMotor leftMotor = new UnregulatedMotor(MotorPort.D);
	private EV3MediumRegulatedMotor arms = new EV3MediumRegulatedMotor(MotorPort.B);
	private EV3GyroSensor gyroSensor = new EV3GyroSensor(SensorPort.S2);

	private double speed = 0; 		// Forward motion speed of robot [-10,10]
	private double direction = 0; // Direction of robot [-50(left),50(right)]

	/**
	 * Set forward speed of the robot.
	 * @param s - speed of robot [-10,10]. A negative speed will make GyroBoy go backwards.
	 */
	public void setSpeed(double s) {
		if (s>10) s=10;			// Limit speed
		if (s<-10) s=-10;
		speed=s;
	}

	/**
	 * Increase or decrease speed.
	 * @param i - the amount to change current speed
	 */
	public void increaseSpeed(double i){
		setSpeed(speed+i);
	}
	
	/**
	 * Turn Gyroboy right or left while moving or standing still.
	 * @param d - Direction of robot [-50(left),50(right)]
	 * This is NOT how much the robot turns, but how fast is turns.
	 */
	public void turn(double d) {
		if (d>50) d=50;
		if (d<-50)d=-50;
		direction=d;
	}

	/**
	 * Move arms quickly up and down.
	 */
	public void moveArms(){
		arms.rotate(45);
		arms.rotate(-45);
	}

	/**
	 * This method keeps GyroBoy upright.
	 * It continuously (every 10 ms) measures gyro angle speed and motor rotational angle.
	 * From this it calculates current gyro angle and motor rotational speed.
	 * Finally the new motor power is computed. The formula used is taken from the LEGO-LabView 
	 * GyroBoy program supplied by LEGO in the EV3 LEGO mindstorms educations Base set.
	 */
	public void run() {
		double gAng = 0; // gyro angle in degrees
		double gSpd = 0; // gyro angle speed in degrees/sec
		double mPos = 0; // Rotation angle of motor in degrees
		double mSpd = 0; // Rotation speed of motor in degrees/sec
		double mSum = 0;
		double mD = 0;
		double mDP1 = 0;
		double mDP2 = 0;
		double mDP3 = 0;
		double pwr = 0; // motor power in [-100,100]
		int loopCount=0;			// postpone activation of the motors until dt in the loop is stable
		boolean ready=false;

		rightMotor.resetTachoCount();
		leftMotor.resetTachoCount();
		gyroSensor.reset();
		SampleProvider gyroReader = gyroSensor.getRateMode();
		float[] sample = new float[gyroReader.sampleSize()];
		long lastTimeStep = System.nanoTime();

		gAng = -0.25; 					// Start angle when sitting on support
		Sound.beepSequenceUp();
		Thread.currentThread().setPriority(MAX_PRIORITY);

		// feed back loop
		while (!Button.ESCAPE.isDown()) {
			// Get time in seconds since last step
			long now = System.nanoTime();
			double dt = (now - lastTimeStep) / 1000000000.0;	// Time step in seconds
			lastTimeStep = now;

			// Get gyro angle and speed
			gyroSensor.fetchSample(sample, 0);
			gSpd = -sample[0]; // invert sign to undo negation in class EV3GyroSensor
			gAng = gAng + (gSpd * dt); // integrate angle speed to get angle

			// Get motor rotation angle and rotational angle speed
			double mSumOld = mSum;
			double rightTacho = rightMotor.getTachoCount();
			double leftTacho = leftMotor.getTachoCount();
			mSum = rightTacho + leftTacho;
			mD = mSum - mSumOld;
			mPos = mPos + mD;
			mSpd = ((mD + mDP1 + mDP2 + mDP3) / 4.0) / dt; // motor rotational speed
			mDP3 = mDP2;
			mDP2 = mDP1;
			mDP1 = mD;

			// Compute new motor power
			mPos -= speed;	// make GyroBoy go forward or backward
			pwr = 0.08 * mSpd + 0.12 * mPos + 0.8 * gSpd + 15 * gAng;
			if (pwr > 100) pwr = 100;
			if (pwr < -100) pwr = -100;
			if (ready){
				rightMotor.setPower((int) (pwr - direction));
				leftMotor.setPower((int) (pwr + direction));
			}

			Delay.msDelay(10);
			loopCount++;
			if (loopCount==10) ready=true;	// skip first 10 iterations
		}

		rightMotor.close();
		leftMotor.close();
		gyroSensor.close();
	}
	
	//	public static void main(String[] args) {
//		GyroBoy gyroboy = new GyroBoy();
//		gyroboy.start();
//		gyroboy.setSpeed(5);
//		Delay.msDelay(5);
//		gyroboy.turn(5);
//	}

}
