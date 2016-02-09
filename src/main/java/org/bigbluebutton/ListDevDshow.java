package org.bigbluebutton;

import org.bigbluebutton.MyFrameRecorder.Exception;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.*;

import java.io.IOException;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;

public class ListDevDshow {
    public static void main(String[] args) throws Exception {
      System.out.println("Java temp dir : " + System.getProperty("java.io.tmpdir"));
      System.out.println("Java name : " + System.getProperty("java.vm.name"));
      System.out.println("OS name : " + System.getProperty("os.name"));
      System.out.println("OS arch : " + System.getProperty("os.arch"));
      System.out.println("JNA Path : " + System.getProperty("jna.library.path"));
      System.out.println("Platform : " + Loader.getPlatform());
      
      Double frameRate = 12.0;
      
      final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("dummy");
      grabber.setImageWidth(800);
      grabber.setImageHeight(600);
      grabber.setFormat("dshow");
      grabber.setFrameRate(frameRate);
      grabber.setOption("list_devices", "true");
      try {
        grabber.start();
      } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

 
    }
    
}
