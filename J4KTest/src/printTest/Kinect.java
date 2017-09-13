package printTest;

import javax.swing.JLabel;

import org.usfirst.frc253.Code2017.Robot;

import edu.ufl.digitalworlds.j4k.DepthMap;
import edu.ufl.digitalworlds.j4k.J4KSDK;
import edu.ufl.digitalworlds.j4k.Skeleton;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.networktables.NetworkTable;


/*
 * Copyright 2011-2014, Digital Worlds Institute, University of 
 * Florida, Angelos Barmpoutis.
 * All rights reserved.
 *
 * When this program is used for academic or research purposes, 
 * please cite the following article that introduced this Java library: 
 * 
 * A. Barmpoutis. "Tensor Body: Real-time Reconstruction of the Human Body 
 * and Avatar Synthesis from RGB-D', IEEE Transactions on Cybernetics, 
 * October 2013, Vol. 43(5), Pages: 1347-1356. 
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *     * Redistributions of source code must retain this copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce this
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
@SuppressWarnings("unchecked")
public class Kinect extends J4KSDK{

	ViewerPanel3D viewer=null;
	JLabel label=null;
	boolean mask_players=false;
	
	NetworkTable table = KinectViewerApp.table;
	
	public void maskPlayers(boolean flag){mask_players=flag;}
	
	public Kinect()
	{
		super();
	}
	
	public Kinect(byte type)
	{
		super(type);
	}
	
	public void setViewer(ViewerPanel3D viewer){this.viewer=viewer;}
	
	public void setLabel(JLabel l){this.label=l;}
	
	private boolean use_infrared=false;
	
	public void updateTextureUsingInfrared(boolean flag)
	{
		use_infrared=flag;
	}
	
	@Override
	public void onDepthFrameEvent(short[] depth_frame, byte[] player_index, float[] XYZ, float[] UV) {
		
		if(viewer==null || label==null)return;
		float a[]=getAccelerometerReading();
		label.setText(((int)(a[0]*100)/100f)+","+((int)(a[1]*100)/100f)+","+((int)(a[2]*100)/100f));
		DepthMap map=new DepthMap(getDepthWidth(),getDepthHeight(),XYZ);
		
		map.setMaximumAllowedDeltaZ(0.5);
		
		if(UV!=null && !use_infrared) map.setUV(UV);
		else if(use_infrared) map.setUVuniform();
		if(mask_players)
		{
			map.setPlayerIndex(depth_frame, player_index);
			map.maskPlayers();
		}
		viewer.map=map;
	}

	@Override
	public void onSkeletonFrameEvent(boolean[] flags, float[] positions, float[] orientations, byte[] state) {
		if(viewer==null || viewer.skeletons==null)return;
		
		for(int i=0;i<getSkeletonCountLimit();i++)
		{
			viewer.skeletons[i]=Skeleton.getSkeleton(i, flags,positions, orientations,state,this);
		}
		
		for(int i=0;i<getSkeletonCountLimit() && viewer.skeletons[i].isTracked()==true;i++) {

				double leftWristY = viewer.skeletons[i].get3DJointY(Skeleton.WRIST_LEFT);
				double rightWristY = viewer.skeletons[i].get3DJointY(Skeleton.WRIST_RIGHT);
				double spineBaseY = viewer.skeletons[i].get3DJointY(Skeleton.SPINE_BASE);
				
				double leftSpeed = leftWristY - spineBaseY;
				double rightSpeed = rightWristY - spineBaseY;
				
				while(viewer.skeletons[i].isTracked()){
					table.putNumber("leftSpeed", leftSpeed);
					table.putNumber("rightSpeed", rightSpeed);
				}
//				
//				if(Math.abs(leftSpeed) > .125)
//		    		Robot.drivetraintank.setLeft(leftSpeed);
//		    	else
//		    		Robot.drivetraintank.setLeft(0);
//		    		
//		    	
//		    	if(Math.abs(rightSpeed) > -.125)
//		    		Robot.drivetraintank.setRight(rightSpeed);
//		    	else
//		    		Robot.drivetraintank.setRight(0);
//		    	if(Math.abs(leftSpeed) > .125)
//		    		Robot.drivetraintank.setLeft_Back(leftSpeed);
//		    	else
//		    		Robot.drivetraintank.setLeft_Back(0);
//		    	
//		    	if(Math.abs(rightSpeed) > .125)
//		    		Robot.drivetraintank.setRight_Back(rightSpeed);
//		    	else
//		    		Robot.drivetraintank.setRight_Back(0);
				
				
//				System.out.println(i);
//				System.out.println("X:" + viewer.skeletons[i].get3DJointX(Skeleton.HAND_RIGHT));
//				System.out.println("Y:" + viewer.skeletons[i].get3DJointY(Skeleton.HAND_RIGHT));
//				System.out.println("Z:" + viewer.skeletons[i].get3DJointZ(Skeleton.HAND_RIGHT));
//				System.out.println();
		}
		
		/*
		 * Note: A total of 6 skeletons can be recorded at a time. Skeletons are given a
		 * random player ID, therefore the if statement to check whether a skeleton is tracked
		 * is necessary to remove all nonexistent skeletons from the console output.
		 */
		
		
		}

	@Override
	public void onColorFrameEvent(byte[] data) {
		if(viewer==null || viewer.videoTexture==null || use_infrared) return;
		viewer.videoTexture.update(getColorWidth(), getColorHeight(), data);
	}

	@Override
	public void onInfraredFrameEvent(short[] data) {
		if(viewer==null || viewer.videoTexture==null || !use_infrared) return;
		int sz=getInfraredWidth()*getInfraredHeight();
		byte bgra[]=new byte[sz*4];
		int idx=0;
		int iv=0;
		short sv=0;
		byte bv=0;
		for(int i=0;i<sz;i++)
		{
			sv=data[i];
			iv=sv >= 0 ? sv : 0x10000 + sv; 
			bv=(byte)( (iv & 0xfff8)>>6);
			bgra[idx]=bv;idx++;
			bgra[idx]=bv;idx++;
			bgra[idx]=bv;idx++;
			bgra[idx]=0;idx++;
		}
		
		viewer.videoTexture.update(getInfraredWidth(), getInfraredHeight(), bgra);
	}

}
