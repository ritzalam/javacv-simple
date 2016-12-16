package org.bigbluebutton;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.*;
import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avutil.*;

public class ClientFFmpeg {
    public static void main(String[] args) throws Exception {
      System.out.println("Java temp dir : " + System.getProperty("java.io.tmpdir"));
      System.out.println("Java name : " + System.getProperty("java.vm.name"));
      System.out.println("OS name : " + System.getProperty("os.name"));
      System.out.println("OS arch : " + System.getProperty("os.arch"));
      System.out.println("JNA Path : " + System.getProperty("jna.library.path"));
      System.out.println("Platform : " + Loader.getPlatform());
      
      System.setProperty("org.bytedeco.javacpp.logger.debug", "true");
      
      System.out.println("Logger : " + System.getProperty("org.bytedeco.javacpp.logger.debug"));
      
      Double frameRate = 12.0;
      int width = 800;
      int height = 600;
      int x = 0;
      int y = 0;
      
      FFmpegFrameGrabber grabber = null;
      
      String platform = Loader.getPlatform();
      String osName = System.getProperty("os.name").toLowerCase();
      if (platform.startsWith("windows")) {
        grabber = setupWindowsGrabber(width, height, x, y);
      } else if (platform.startsWith("linux")) {
        grabber = setupLinuxGrabber(width, height, x, y);
      } else if (platform.startsWith("macosx-x86_64")) {
        grabber = setupMacOsXGrabber(width, height, x, y);
      }
      
      grabber.setFrameRate(frameRate);
      try {
        grabber.start();
      } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      
      String h264file = "out3.mp4";
      String svcfile = "out4.flv";
      String filename = h264file;
      
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(filename, grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setFormat("flv");
        recorder.setFrameRate(frameRate);
        recorder.setGopSize(24);
        // H264
        userH264(recorder);
        //useSVC2(recorder);
        
        try {
          recorder.start();
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        long sleepFramerate = (long) (1000 / frameRate);
        int framesToEncode = 620;
        Frame frame;
        int i = 0;
        while (i < framesToEncode) {
          long now = System.currentTimeMillis();
          try {
            frame = grabber.grabImage();
            if (frame != null) {
              try {
                recorder.record(frame);
              } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          i++;
          
          long execDuration = (System.currentTimeMillis() - now);
          long sleepDuration = Math.max(sleepFramerate - execDuration, 0);
          try {
             // sleep for framerate milliseconds
             Thread.sleep(sleepDuration);
          } catch (InterruptedException e) {
             e.printStackTrace();
          }
          
          System.out.println("Captured frame " + i);

        }
        try {
          recorder.stop();
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        try {
          grabber.stop();
        } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }
    
    private static void userH264(FFmpegFrameRecorder recorder) {
      recorder.setFormat("flv");
        
      // H264
      recorder.setVideoCodec(AV_CODEC_ID_H264);
      recorder.setPixelFormat(AV_PIX_FMT_YUV420P);

      //  recorder.setVideoQuality(26);
      //recorder.setVideoOption("f", "gdigrab");
      //recorder.setVideoOption("i", "desktop");
      recorder.setVideoOption("crf", "38");
      recorder.setVideoOption("preset", "veryfast");
      recorder.setVideoOption("tune", "zerolatency");
      recorder.setVideoOption("intra-refresh", "1");
      //  recorder.setVideoOption("rtmp_buffer", "0");
      //  recorder.setVideoOption("rtmp_live", "live");
      //  recorder.setVideoOption("fflags", "nobuffer");    
    }
 /**   
    private static void useH264(FFmpegFrameRecorder recorder, Map<String, String> codecOptions) {
        Double frameRate = parseFrameRate(codecOptions.get(FRAMERATE_KEY));
        recorder.setFrameRate(frameRate);
        
        int keyFrameInterval =  parseKeyFrameInterval(codecOptions.get(KEYFRAMEINTERVAL_KEY));
        int gopSize = frameRate.intValue() * keyFrameInterval;
        recorder.setGopSize(gopSize);
        
        System.out.println("==== CODEC OPTIONS =====");
        for (Map.Entry<String, String> entry : codecOptions.entrySet()) {
          System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
          if (entry.getKey().equals(FRAMERATE_KEY) || entry.getKey().equals(KEYFRAMEINTERVAL_KEY)) {
            // ignore as we have handled this above
          } else {
            recorder.setVideoOption(entry.getKey(), entry.getValue());        
          }

        }
        System.out.println("==== END CODEC OPTIONS =====");
        
        recorder.setFormat("flv");
          
        // H264
        recorder.setVideoCodec(AV_CODEC_ID_H264);
        recorder.setPixelFormat(AV_PIX_FMT_YUV420P);
        recorder.setVideoOption("crf", "38");
        recorder.setVideoOption("preset", "veryfast");
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setVideoOption("intra-refresh", "1"); 
      }
     */
    
      private static void useSVC2(FFmpegFrameRecorder recorder) {
        recorder.setFormat("flv");
        
        ///
        // Flash SVC2
        recorder.setVideoCodec(AV_CODEC_ID_FLASHSV2);
        recorder.setPixelFormat(AV_PIX_FMT_BGR24);
        
      }
      
      // Need to construct our grabber depending on which
      // platform the user is using.
      // https://trac.ffmpeg.org/wiki/Capture/Desktop
      //
      private static FFmpegFrameGrabber setupWindowsGrabber(int width, int height, int x, int y) {
        System.out.println("Setting up grabber for windows.");
        FFmpegFrameGrabber winGrabber = new FFmpegFrameGrabber("desktop");
        winGrabber.setImageWidth(width);
        winGrabber.setImageHeight(height);
        
        winGrabber.setFormat("gdigrab");   
        
        return winGrabber;
      }
      
      private static FFmpegFrameGrabber setupLinuxGrabber(int width, int height, int x, int y) {
        // ffmpeg -video_size 1024x768 -framerate 25 -f x11grab -i :0.0+100,200 output.mp4
        // This will grab the image from desktop, starting with the upper-left corner at (x=100, y=200) 
        // with the width and height of 1024x768.

        
        String videoSize = new Integer(width).toString().concat("x").concat(new Integer(height).toString());
        
        System.out.println("Setting up grabber for linux.");

        
        FFmpegFrameGrabber linuxGrabber = new FFmpegFrameGrabber(":0.0");
        linuxGrabber.setImageWidth(width);
        linuxGrabber.setImageHeight(height);
//        linuxGrabber.setOption("video_size", "cif"); 
//        linuxGrabber.setVideoCodec(AV_CODEC_ID_RAWVIDEO);
//        linuxGrabber.setPixelFormat(AV_PIX_FMT_0BGR);
//        linuxGrabber.setPixelFormat(AV_PIX_FMT_YUV420P);
        
        linuxGrabber.setFormat("x11grab");    
        return linuxGrabber;
      }
      
      private static FFmpegFrameGrabber setupMacOsXGrabber(int width, int height, int x, int y) {
        
        //ffmpeg -f avfoundation -i "Capture screen 0" test.mkv
        String inputDevice = "Capture screen 0";     
        String videoSize = new Integer(width).toString().concat("x").concat(new Integer(height).toString());

        System.out.println("Setting up grabber for macosx.");
        System.out.println("input:" + inputDevice + " videoSize:" + videoSize);
        
        FFmpegFrameGrabber macGrabber = new FFmpegFrameGrabber(inputDevice);
        macGrabber.setOption("video_size", videoSize); 
        macGrabber.setFormat("avfoundation");
        return macGrabber;
      }
}
