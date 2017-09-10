package printTest;

import edu.ufl.digitalworlds.j4k.J4KSDK;
import edu.ufl.digitalworlds.j4k.Skeleton;

public class myKinect extends J4KSDK{
	
	int maxSkeletons = getMaxNumberOfSkeletons();
	
	Skeleton skeleton;

	@Override
	public void onColorFrameEvent(byte[] arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDepthFrameEvent(short[] arg0, byte[] arg1, float[] arg2, float[] arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSkeletonFrameEvent(boolean[] flags, float[] positions, float[] orientations, byte[] state) {
		System.out.print(".");
		for (int i = 0; i < maxSkeletons; i++) {
			skeleton = Skeleton.getSkeleton(i, flags, positions, orientations, state, this);
			
			if (flags[i]){
				skeleton.get3DJoint(Skeleton.HAND_RIGHT);
			}

		}

		
	}

}
