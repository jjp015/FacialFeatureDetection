import java.io.ByteArrayInputStream;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * @author: Jeremy
 * This class handles the action of starting and stopping the video capture 
 * and detecting the face and facial features of the individual
 *
 */
public class FaceDetectionController
{
	@FXML
	private Button vidPress;
	@FXML
	private ImageView box;     //the box of the face detected
	
	private ScheduledExecutorService timer; //timer for video capturing
	private VideoCapture record; //activation for video capturing
	private boolean vidOn;       //camera capturing check
	private CascadeClassifier faceCascade; //feature of the face
	private CascadeClassifier noseCascade; //feature of the nose
	private CascadeClassifier mouthCascade;//feature of the mouth
	private int frameSize;
	
	/**
	 * Start of the program after pressing the button in the program
	 */
	protected void init()
	{
		this.record = new VideoCapture();
		this.faceCascade = new CascadeClassifier();
		this.noseCascade = new CascadeClassifier();
		this.mouthCascade = new CascadeClassifier();
		this.frameSize = 0;
	}
	
	@FXML
	protected void startCamera()
	{
		this.haarFace("resources/haarcascades/haarcascade_frontalface_alt.xml");
		this.haarNose("resources/haarcascades/haarcascade_mcs_nose.xml");
		this.haarMouth("resources/haarcascades/haarcascade_mcs_mouth.xml");
		
		box.setFitWidth(750);
		box.setPreserveRatio(true);
		
		if (!this.vidOn)
		{	
			this.record.open(0); //start video capture
			
			if (this.record.isOpened()) //check if video capturing exists
			{
				this.vidOn = true;
				
				//frame capture speed
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run()
					{
						Image imageToShow = grabFrame();
						box.setImage(imageToShow);
					}
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				
				//grab frame for 33 frames per second
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 34, 
            TimeUnit.MILLISECONDS);
				
				this.vidPress.setText("Stop");
			}
			else
			{
				System.err.println("Camera Connection Error");
			}
		}
		else
		{
			this.vidOn = false; //camera is off
			this.vidPress.setText("Activate Face Detection");
			
			//stop timer
			try
			{
				this.timer.shutdown();
				this.timer.awaitTermination(34, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				System.err.println("Exception Halting Camera, Attenpting to " +
            "Release Camera " + e);
			}	
			this.record.release();
			this.box.setImage(null);
		}
	}

	/**
	 * Grab the box frame from video capturing
	 * @return the image of the capture
	 */
	private Image grabFrame()
	{
		Image imageToShow = null;
		Mat frame = new Mat();
		
		//check if capturing is on
		if (this.record.isOpened())
		{
			try
			{
				//read video captures
				this.record.read(frame);
				
				//keep running if the frame exists
				if (!frame.empty())
				{
					this.detectShow(frame); // show the detections
					imageToShow = mat2Image(frame); //convert Mat to Image
				}
			}
			catch (Exception e)
			{
				System.err.println("ERROR: " + e);
			}
		}
		return imageToShow;
	}

	/**
	 * To detect and track the face and facial features and display it 
	 * on the screen
	 * @param frame the face and the facial features
	 */
	private void detectShow(Mat frame)
	{
		MatOfRect faces = new MatOfRect();
		MatOfRect nose = new MatOfRect();
		MatOfRect mouth = new MatOfRect();
		Mat grayFrame = new Mat();
	
    //conver fram to gray scale  
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
    //improve frame result
		Imgproc.equalizeHist(grayFrame, grayFrame);
		
		//face size must equal to at least 22% of the frame size
		if (this.frameSize == 0)
		{
			int height = grayFrame.rows();
			if (Math.round(height * 0.22f) > 0)
			{
				this.frameSize = Math.round(height * 0.22f);
			}
		}
		
		//detect face, nose, and mouth
		this.faceCascade.detectMultiScale(grayFrame, faces, 1.2, 3, 0 | 
        Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.frameSize, this.frameSize), new Size());
		this.noseCascade.detectMultiScale(grayFrame, nose, 1.2, 3, 0 | 
        Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.frameSize, this.frameSize), new Size());
		this.mouthCascade.detectMultiScale(grayFrame, mouth, 1.2, 3, 0 | 
        Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.frameSize, this.frameSize), new Size());
		
				
		Rect[] facesArray = faces.toArray();
		Rect[] noseArray = nose.toArray();
		Rect[] mouthArray = mouth.toArray();
		//continuous tracking of all face, nose, and mouth
		for (int i = 0; i < facesArray.length; i++) {
			Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), 
          new Scalar(0, 255, 0), 2);	
		}
		for (int i = 0; i < noseArray.length; i++) {
			Imgproc.rectangle(frame, noseArray[i].tl(), noseArray[i].br(), 
          new Scalar(255, 0, 0), 2);	
		}
		for (int i = 0; i < mouthArray.length; i++) {
			Imgproc.rectangle(frame, mouthArray[i].tl(), mouthArray[i].br(), 
          new Scalar(0, 0, 255), 2);	
		}
	}

	//load classifier of the face
	private void haarFace(String classifierPath)
	{
		this.faceCascade.load(classifierPath);
		this.vidPress.setDisable(false);
	}
	
	//load classifier of the nose
	private void haarNose(String classifierPath)
	{
		this.noseCascade.load(classifierPath);
		this.vidPress.setDisable(false);
	}
	
	//load classifier of the mouth
	private void haarMouth(String classifierPath)
	{
		this.mouthCascade.load(classifierPath);
		this.vidPress.setDisable(false);
	}
	
	
	private Image mat2Image(Mat frame)
	{
		MatOfByte buffer = new MatOfByte(); //temporary buffer
		Imgcodecs.imencode(".png", frame, buffer); //store frame in buffer in 
                                               //PNG format
    //show the images from buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
}
