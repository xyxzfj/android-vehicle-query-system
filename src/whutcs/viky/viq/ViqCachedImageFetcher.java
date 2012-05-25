package whutcs.viky.viq;

import static whutcs.viky.viq.ViqCommonUtilities.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

/**
 * Asynchronously feeds imageView with a cached version of imageName
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class ViqCachedImageFetcher extends Thread {
	private final String mImageName;
	private final ImageView mImageView;
	private static final String CACHE_PATH = getDcimDirectory(".viq_cache")
			.getPath() + File.separator;

	public ViqCachedImageFetcher(String imageName, ImageView imageView) {
		mImageName = imageName;
		mImageView = imageView;
	}

	@Override
	public void run() {
		super.run();

		BitmapDrawable drawable = null;
		String cachedImagePath = CACHE_PATH + mImageName;
		File cachedImageFile = new File(cachedImagePath);
		if (cachedImageFile.exists()) {
			// read the cache
			drawable = new BitmapDrawable(cachedImagePath);
		} else {
			// create the cache
			File originalImageFile = getExistingImageFile(mImageName);
			if (originalImageFile != null && originalImageFile.exists()) {
				Bitmap bitmap = getBitmap(originalImageFile.getPath());
				try {
					bitmap.compress(CompressFormat.JPEG, 50,
							new FileOutputStream(cachedImageFile));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				drawable = new BitmapDrawable(bitmap);
			}
		}

		if (drawable != null) {
			mImageView.setImageDrawable(drawable);
		}
	}
}
