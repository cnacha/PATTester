package nz.aucklanduni.util;

import java.util.Random;

public class RandomUtil {
		
	public int randomNumber(int min, int max) {
		Random rand = new Random(); 
		return rand.nextInt((max - min) + 1) + min;
	}
}
