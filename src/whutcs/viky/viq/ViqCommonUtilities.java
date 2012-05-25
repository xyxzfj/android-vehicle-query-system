package whutcs.viky.viq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.Log;

/**
 * Provides access to common constants and methods of this app. Extends
 * ViqSQLiteOpenHelper only to use its constants directly.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class ViqCommonUtilities extends ViqSQLiteOpenHelper {
	private static final String TAG = "ViqCommonUtility";

	public ViqCommonUtilities(Context context) {
		super(context);
	}

	public static final String APP_NAME = "AndroidVIQ";
	/**
	 * The dcimDirectory in /sdcard/DCIM to save taken vehicle images.
	 */
	public static final File DIRECTORY_VIQ = getDcimDirectory(APP_NAME);

	public static final String EXTRA_ID = "_id";
	public static final String EXTRA_LICENCE = "licenee";
	public static final String EXTRA_VEHICLE = "vehicle";

	public static final int CODE_TAKE_PHOTO = 0;
	public static final int CODE_SELECT_PHOTO = 1;

	public static void streamCopy(InputStream is, OutputStream os)
			throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = is.read(buffer)) > 0) {
			os.write(buffer, 0, read);
		}
	}

	public static boolean fileCopy(File srcFile, File objFile) {
		if (srcFile == null || objFile == null) {
			return false;
		}

		boolean result;
		InputStream iStream;
		try {
			iStream = new FileInputStream(srcFile);
			OutputStream oStream = new FileOutputStream(objFile);
			streamCopy(iStream, oStream);
			iStream.close();
			oStream.close();
			result = true;
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	/**
	 * Get the date time string as a file name.
	 * 
	 * @return a unique file name not including the extended name.
	 */
	public static String getDateTimeAsImageName() {
		SimpleDateFormat fileNameFormat = new SimpleDateFormat(
				"yyyy-MM-dd_HH-mm-ss");
		String fileName = fileNameFormat.format(new Date(System
				.currentTimeMillis()));
		return fileName;
	}

	public static String getDataTimeString() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String datetime = format.format(new Date(System.currentTimeMillis()));
		return datetime;
	}

	public static String getRelativeTime(Context context, String time) {
		if (time == null) {
			return null;
		}

		String relativeTime = null;

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			date = format.parse(time);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (date != null) {
			Long timeMillis = date.getTime();
			relativeTime = DateUtils.getRelativeTimeSpanString(
					timeMillis,
					System.currentTimeMillis(),
					DateUtils.MINUTE_IN_MILLIS,
					DateUtils.FORMAT_NUMERIC_DATE
							| DateUtils.FORMAT_ABBREV_RELATIVE).toString();
			Log.v(TAG, relativeTime);
		}

		if (relativeTime == null) {
			relativeTime = time;
		}

		return relativeTime;
	}

	public static String getGpsString(Context context) {
		if (context == null) {
			return null;
		}

		String gps = null;
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);

		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		String provider = locationManager.getBestProvider(criteria, true);

		if (provider != null) {
			Location location = locationManager.getLastKnownLocation(provider);

			if (location != null) {
				double longtitude = location.getLongitude();
				double latitude = location.getLatitude();
				gps = "Lon:" + longtitude + "; Lat:" + latitude;
			}
		}

		return gps;
	}

	/**
	 * Get the corresponding file of the dcimDirectory /sdcard/DCIM/name. Will
	 * create the dcimDirectory if is doesn't exist.
	 * 
	 * @param dirName
	 *            the dcimDirectory dirName.
	 * @return the dcimDirectory's corresponding file.
	 */
	public static File getDcimDirectory(String dirName) {
		if (dirName == null) {
			return null;
		}

		File dcimDirectory = null;
		String dcimPath = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DCIM).getPath();
		dcimDirectory = new File(dcimPath, dirName);
		if (!dcimDirectory.exists()) {
			dcimDirectory.mkdirs();
		}
		return dcimDirectory;
	}

	public static Bitmap getBitmap(String imagePath, int inSampleSize) {
		if (imagePath == null) {
			return null;
		}
		if (inSampleSize == 0) {
			inSampleSize = 1;
		}

		Bitmap bmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		opts.inSampleSize = inSampleSize;
		bmp = BitmapFactory.decodeFile(imagePath, opts);

		return bmp;
	}

	public static final Bitmap getBitmap(String imagePath) {
		if (imagePath == null) {
			return null;
		}

		Bitmap bitmap = getBitmap(imagePath, 8);

		return bitmap;
	}

	public static final Bitmap getBitmapByName(String imageName) {
		if (imageName == null) {
			return null;
		}

		Bitmap bitmap = null;
		String imagePath = null;
		File imageFile = getExistingImageFile(imageName);
		if (imageFile != null) {
			imagePath = imageFile.getPath();
		}
		if (imagePath != null) {
			bitmap = getBitmap(imagePath);
		}
		return bitmap;
	}

	/**
	 * Get a new image file with the imagePath under "DCIM/VIQ" and the name of
	 * the current date time.
	 * 
	 * @return
	 */
	public static File getNewImageFile() {
		String imageName = getDateTimeAsImageName() + ".jpg";
		File imageFile = new File(DIRECTORY_VIQ, imageName);
		return imageFile;
	}

	public static File getExistingImageFile(String imageName) {
		if (imageName == null) {
			return null;
		}

		File imageFile = new File(DIRECTORY_VIQ, imageName);
		if (!imageFile.exists()) {
			imageFile = null;
		}
		return imageFile;
	}

	/**
	 * 
	 * @param context
	 * @param uri
	 *            uri of SCHEME_FILE or SCHEME_CONTENT
	 * @return image path; uri will be changed to SCHEME_FILE
	 */
	public static String uriToImagePath(Context context, Uri uri) {
		if (context == null || uri == null) {
			return null;
		}

		String imagePath = null;
		String uriString = uri.toString();
		String uriSchema = uri.getScheme();
		if (uriSchema.equals(ContentResolver.SCHEME_FILE)) {
			imagePath = uriString.substring("file://".length());
		} else {// uriSchema.equals(ContentResolver.SCHEME_CONTENT)
			ContentResolver resolver = context.getContentResolver();
			Cursor cursor = resolver.query(uri, null, null, null, null);
			if (cursor.getCount() == 0) {
				Log.e(TAG, "Uri(" + uri.toString() + ") not found!");
				return null;
			}
			cursor.moveToFirst();
			// imagePath = cursor.getString(cursor
			// .getColumnIndex(MediaStore.Images.ImageColumns.DATA));
			imagePath = cursor.getString(1);
			// imagePath =
			// cursor.getString(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

			// Change the SCHEME_CONTENT uri to the SCHEME_FILE.
			uri = Uri.fromFile(new File(imagePath));
		}
		Log.v(TAG, "Final uri: " + uri.toString());
		return imagePath;
	}
}
