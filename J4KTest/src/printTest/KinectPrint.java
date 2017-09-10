package printTest;

import edu.ufl.digitalworlds.j4k.J4KSDK;
import edu.ufl.digitalworlds.j4k.Skeleton;

public class KinectPrint {

	public static void main(String[] args) {
		myKinect kinect = new myKinect();
		kinect.start(J4KSDK.SKELETON);
		kinect.setSeatedSkeletonTracking(true);
		System.out.println("Initializing...");
		
		try {
			Thread.sleep(5000);
		} catch(InterruptedException e) {}
		
		float handX;
		for(int i = 0; i < 10; i++) {
			handX = kinect.skeleton.get3DJointX(Skeleton.HAND_RIGHT);
			System.out.println(handX);
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {}
		}
		
		kinect.stop();
		System.out.println("Completed!");
	}

}
