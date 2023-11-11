package docfinder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

class DocFinder implements Runnable {
    private File file;
    private int delta;
    private File result = null;
    private int redOfset, greenOfset, blueOfset;

    DocFinder(File file, int delta, int redOfset, int greenOfset, int blueOfset) {
        this.file = file;
        this.delta = delta;
        this.redOfset = redOfset;
        this.greenOfset = greenOfset;
        this.blueOfset = blueOfset;
    }

    @Override
    public void run() {
		try {
		    BufferedImage img = ImageIO.read(file);
		    if (img != null && getMedian(img)) result = file;
		} catch (IOException e) {
		    e.printStackTrace();
		}
    }

    public File getResult() {
    	return result;
    }
    
    boolean getMedian (BufferedImage img){
        int width = img.getWidth();
        int height = img.getHeight();
        int[] redFlags = new int[256];
        int[] greenFlags = new int[256];
        int[] blueFlags = new int[256];
        int c;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                c = img.getRGB(x,y) & 0xFFFFFF;
                redFlags[c >> 16]++;
                greenFlags[(c >> 8) & 0x00FF]++;
                blueFlags[c & 0x0000FF]++;
            }
        }
        
        int halfPixels = (width * height) / 2;
        int countColor = 0;
        int redMedian = 0;
        while (countColor < halfPixels) {
        	countColor += redFlags[redMedian++];
        }
        countColor = 0;
        int greenMedian = 0;
        while (countColor < halfPixels) {
        	countColor += greenFlags[greenMedian++];
        }
        countColor = 0;
        int blueMedian = 0;
        while (countColor < halfPixels) {
        	countColor += blueFlags[blueMedian++];
        }

        redMedian -= redOfset;
        greenMedian -= greenOfset;
        blueMedian -= blueOfset;
//        System.out.printf("r: %d g: %d b: %d\n", redMedian, greenMedian, blueMedian);
        if ((       redMedian < greenMedian+delta)
        		&& (redMedian > greenMedian-delta)
        		&& (redMedian < blueMedian+delta)
        		&& (redMedian > blueMedian-delta)){
                return true;
        }
        return false;
    }
}

