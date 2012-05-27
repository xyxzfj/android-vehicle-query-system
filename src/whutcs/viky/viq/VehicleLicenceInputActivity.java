package whutcs.viky.viq;

import static com.googlecode.javacv.cpp.opencv_core.CV_32FC1;
import static com.googlecode.javacv.cpp.opencv_core.CV_RGB;
import static com.googlecode.javacv.cpp.opencv_core.CV_WHOLE_SEQ;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvCreateMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvGet2D;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvScalarAll;
import static com.googlecode.javacv.cpp.opencv_core.cvSet;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvZero;
import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_GAUSSIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_INTER_LINEAR;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_POLY_APPROX_DP;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_EXTERNAL;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_WARP_FILL_OUTLIERS;
import static com.googlecode.javacv.cpp.opencv_imgproc.cv2DRotationMatrix;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvApproxPoly;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCanny;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourArea;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourPerimeter;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvEqualizeHist;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvMinAreaRect2;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvWarpAffine;
import static whutcs.viky.viq.ViqCommonUtilities.CODE_SELECT_PHOTO;
import static whutcs.viky.viq.ViqCommonUtilities.CODE_TAKE_PHOTO;
import static whutcs.viky.viq.ViqCommonUtilities.EXTRA_LICENCE;
import static whutcs.viky.viq.ViqCommonUtilities.fileCopy;
import static whutcs.viky.viq.ViqCommonUtilities.getBitmap;
import static whutcs.viky.viq.ViqCommonUtilities.getDataTimeString;
import static whutcs.viky.viq.ViqCommonUtilities.getDcimDirectory;
import static whutcs.viky.viq.ViqCommonUtilities.getGpsString;
import static whutcs.viky.viq.ViqCommonUtilities.getNewImageFile;
import static whutcs.viky.viq.ViqCommonUtilities.streamCopy;
import static whutcs.viky.viq.ViqCommonUtilities.uriToImagePath;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.SPECIAL_COLUMN_LICENCE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMNS;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_NOTE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_PHOTO;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_PLACE;
import static whutcs.viky.viq.ViqSQLiteOpenHelper.TABLE_QUERY_COLUMN_TIME;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.cpp.opencv_core.CvBox2D;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.CvPoint2D32f;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

/**
 * Inputs a licence (number) -- either by recognizing from a vehicle image, or
 * by hand typed in, or both (in which the later is a corcharRecting process). A
 * vehicle image can be either from the camera or from the gallary.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class VehicleLicenceInputActivity extends Activity {
	private static final String TAG = "VehicleLicenceInputActivity";

	// TODO: set false when publishing
	private static final boolean SAVE_IMG = false;

	public static final String SAVE_IMAGE_PATH = getDcimDirectory("viq_save")
			.getPath() + File.separator;

	/**
	 * The file of the vehicle image taken or selected.
	 */
	private File mVehicleImageFile;

	/**
	 * The bitmap of the licence plate image.
	 */
	private Bitmap mPlateBitmap;

	/**
	 * Number of chars in a licence.
	 */
	private static final int PLATECHARS = 7;
	/**
	 * Number of candidate (guessed) mCandidateChars for each char in the
	 * licence.
	 */
	private static final int CANDIDATES = 7;

	/**
	 * The space char, a button's initial text.
	 */
	private static final char SPACE = ' ';

	/**
	 * The selected col of each row; 0 by default.
	 */
	private final int[] mSelections = new int[PLATECHARS];

	/**
	 * The char field recognized.
	 */
	private final char[][] mCandidateChars = new char[CANDIDATES][PLATECHARS];

	/**
	 * The button field in correspondence with the char field.
	 */
	private final CandidateButton[][] mCandidateButtons = new CandidateButton[CANDIDATES][PLATECHARS];

	/**
	 * The edit text row showing the result and accepting dicharRect edit.
	 */
	private final ResultEditText[] mResultTexts = new ResultEditText[PLATECHARS];

	private EditText mLicenceText;
	private TableLayout mTableLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vehicle_licence_input);

		mLicenceText = (EditText) findViewById(R.id.licence);
		mTableLayout = (TableLayout) findViewById(R.id.table);
		Button okButton = (Button) findViewById(R.id.query_ok);
		Button cancelButton = (Button) findViewById(R.id.query_cancel);

		// add result edit texts row
		TableRow resultRow = new TableRow(this);
		for (int col = 0; col < PLATECHARS; col++) {
			ResultEditText text = new ResultEditText(col);
			mResultTexts[col] = text;
			resultRow.addView(text);
		}
		mTableLayout.addView(resultRow);

		// add candidate buttons rows
		for (int row = 0; row < CANDIDATES; row++) {
			TableRow tableRow = new TableRow(this);
			for (int col = 0; col < PLATECHARS; col++) {
				CandidateButton button = new CandidateButton(this, row, col);
				mCandidateButtons[row][col] = button;
				tableRow.addView(button);
			}
			mTableLayout.addView(tableRow);
		}

		okButton.setOnClickListener(new OnOkClickListener());
		cancelButton.setOnClickListener(new OnCancelClickListener());

		// Do a image recognition at once.
		String queryMethod = ViqPreferenceActivity.getQueryMethod(this);
		if (queryMethod.equals(getString(R.string.take_licence_image))) {
			takeVehicleImage();
		} else if (queryMethod.equals(getString(R.string.select_licence_image))) {
			selectVehicleImage();
		} else {
		}
	}

	private class OnOkClickListener implements OnClickListener {

		public void onClick(View v) {
			String datetime = getDataTimeString();
			String gps = getGpsString(VehicleLicenceInputActivity.this);
			String note = null;

			String licence = null;
			if (mLicenceText.getText() != null) {
				licence = mLicenceText.getText().toString();
			}
			String vehicle = null;
			if (mVehicleImageFile != null) {
				vehicle = mVehicleImageFile.getName();
			}

			ContentValues values = new ContentValues();
			values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_TIME], datetime);
			values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_PLACE], gps);
			values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_NOTE], note);
			values.put(TABLE_QUERY_COLUMNS[TABLE_QUERY_COLUMN_PHOTO], vehicle);
			values.put(SPECIAL_COLUMN_LICENCE, licence);

			ViqSQLiteOpenHelper helper = new ViqSQLiteOpenHelper(
					VehicleLicenceInputActivity.this);
			SQLiteDatabase database = helper.getWritableDatabase();
			long rowid = database.insert(TABLE_QUERY, null, values);
			Log.v(TAG, "rowid: " + rowid);
			database.close();
			helper.close();

			Toast.makeText(VehicleLicenceInputActivity.this,
					getString(R.string.check_ok), Toast.LENGTH_SHORT).show();

			// startActivity(new Intent(VehicleLicenceInputActivity.this,
			// VehicleQueryEditActivity.class).putExtra(EXTRA_LICENCE,
			// licence).putExtra(EXTRA_VEHICLE, vehicle));
			startActivity(new Intent(VehicleLicenceInputActivity.this,
					VehicleItemViewActivity.class).putExtra(EXTRA_LICENCE,
					licence));

			finish();
		}
	}

	private class OnCancelClickListener implements OnClickListener {

		public void onClick(View v) {
			finish();
		}

	}

	/**
	 * Get a vehicle image by calling the system's camera to take a image.
	 */
	private void takeVehicleImage() {
		mVehicleImageFile = getNewImageFile();
		Intent takeVehicleImageIntent = new Intent(
				MediaStore.ACTION_IMAGE_CAPTURE);
		Uri vehicleImageUri = Uri.fromFile(mVehicleImageFile);
		Log.v(TAG, "Taken image uri: " + vehicleImageUri.toString());
		takeVehicleImageIntent.putExtra(MediaStore.EXTRA_OUTPUT,
				vehicleImageUri);
		startActivityForResult(takeVehicleImageIntent, CODE_TAKE_PHOTO);
	}

	/**
	 * Get a vehicle image by selecting a image file in local mStorage.
	 */
	private void selectVehicleImage() {
		Intent selectVehicleImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
		selectVehicleImageIntent.setType("image/**");
		startActivityForResult(selectVehicleImageIntent, CODE_SELECT_PHOTO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case CODE_TAKE_PHOTO:
				break;
			case CODE_SELECT_PHOTO:
				Uri vehicleImageUri = data.getData();
				Log.v(TAG, "Selected image uri: " + vehicleImageUri.toString());
				String selectImageName = uriToImagePath(this, vehicleImageUri);
				File srcFile = new File(selectImageName);
				mVehicleImageFile = getNewImageFile();
				// Copy the selected image file to the DIRECTORY_VIQ.
				fileCopy(srcFile, mVehicleImageFile);
				break;
			default:
				break;
			}

			recogniseImage();
			showResultForCorcharRecting();
		}
	}

	public static Bitmap iplImageToBitmap(IplImage src) {
		int width = src.width();
		int height = src.height();
		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				int gray = (int) Math.floor(cvGet2D(src, r, c).getVal(0));
				bitmap.setPixel(c, r, Color.argb(255, gray, gray, gray));
			}
		}
		return bitmap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.vehicle_licence_input_options_menu, menu);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean result = super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case R.id.menu_setup:
			startActivity(new Intent(this, ViqPreferenceActivity.class));
			break;
		case R.id.menu_take_image:
			takeVehicleImage();
			break;
		case R.id.menu_select_image:
			selectVehicleImage();
			break;
		default:
			break;
		}

		return result;
	}

	/**
	 * Show the recognition result and let the user corcharRect what is wrong.
	 */
	private void showResultForCorcharRecting() {
		// Show the extracted licence image.
		ImageView extractedLicenceImageView = (ImageView) findViewById(R.id.extracted_licence);
		extractedLicenceImageView
				.setImageBitmap(mPlateBitmap == null ? getBitmap(mVehicleImageFile
						.getPath()) : mPlateBitmap);

		// Show the recognized licence number.
		for (int col = 0; col < PLATECHARS; col++) {
			mResultTexts[col].setText("" + mCandidateChars[0][col]);
		}
		// Show the recognized candidate chars of the licence number
		// with a table of candidate buttons.
		for (int row = 0; row < CANDIDATES; row++) {
			for (int col = 0; col < PLATECHARS; col++) {
				mCandidateButtons[row][col].setText(""
						+ mCandidateChars[row][col]);
			}
		}
	}

	/**
	 * Recognize the vehicle licence image, generating
	 * mCandidateChars[CANDIDATES][PLATECHARS].
	 */
	private void recogniseImage() {
		String plateNumber;
		String vehicleImagePath = mVehicleImageFile.getPath();
		IplImage plateImage = findPlateImage(vehicleImagePath);
		if (plateImage != null) {
			mPlateBitmap = iplImageToBitmap(plateImage);
		} else {
			mPlateBitmap = getBitmap(vehicleImagePath);
		}
		plateNumber = recognizePlate(mPlateBitmap);

		int index = plateNumber.length() - 1;
		for (int col = PLATECHARS - 1; col >= 0; col--) {
			char c = SPACE;
			// try to get the right most none space char at plateNumber
			while (index >= 0) {
				c = plateNumber.charAt(index--);
				if (c != SPACE && c != '\n') {
					break;
				}
			}
			mCandidateChars[0][col] = c;

			ArrayList<Character> alikeChars = getAlikeChars(c);
			for (int row = 1; row < CANDIDATES && row - 1 < alikeChars.size(); row++) {
				mCandidateChars[row][col] = alikeChars.get(row - 1);
			}
		}
	}

	private static ArrayList<Character> getAlikeChars(char c) {
		ArrayList<Character> alikeChars = new ArrayList<Character>();

		final int CHARS = 5;
		final char likeChars[][] = new char[CHARS][];

		likeChars[0] = new char[] { 'B', '8' };
		likeChars[1] = new char[] { 'C', 'D', 'G', 'O', 'Q', '0' };
		likeChars[2] = new char[] { 'E', 'F', 'P', 'T', '7' };
		likeChars[3] = new char[] { 'I', 'L', '1' };
		likeChars[4] = new char[] { '¶õ', 'Ô¥', '¸Ó' };

		int row = 0;
		int col = 0;
		boolean found = false;
		for (row = 0; row < CHARS && !found; row++) {
			for (col = 0; col < likeChars[row].length && !found; col++) {
				if (likeChars[row][col] == c) {
					found = true;
				}
			}
		}
		if (found) {
			row--;
			col--;
			for (int j = 0; j < likeChars[row].length; j++) {
				if (j != col) {
					alikeChars.add(likeChars[row][j]);
				}
			}
		}

		return alikeChars;
	}

	private String recognizePlate(Bitmap plateBitmap) {
		String plateNumber = null;

		String tessPath = "/mnt/sdcard/tesseract/";
		String langCnl = "cnl";
		String langSuffix = ".traineddata";
		String langName = langCnl + langSuffix;
		File tessDataDir = new File(tessPath, "tessdata");
		if (!tessDataDir.exists()) {
			tessDataDir.mkdirs();
			// copy assets/cnl.traineddata to /mnt/sdcard/tesseract/tessdata/
			try {
				InputStream is = getAssets().open(langName);
				File tessDataFile = new File(tessDataDir, langName);
				try {
					OutputStream os = new FileOutputStream(tessDataFile);
					streamCopy(is, os);
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// create an instance of TessBaseAPI
		TessBaseAPI tessBaseAPI = new TessBaseAPI();

		// use the trained Chinese Licence language pack to recognize the image
		// plate
		tessBaseAPI.init(tessPath, langCnl);

		// use single-line page segmentation mood
		tessBaseAPI.setPageSegMode(TessBaseAPI.PSM_SINGLE_LINE);

		// set the image to recognize.
		Pix pix = ReadFile.readBitmap(plateBitmap);
		tessBaseAPI.setImage(pix);

		// do the recognition
		plateNumber = tessBaseAPI.getUTF8Text();
		Log.v(TAG, "Licence recognized: " + plateNumber);
		Toast.makeText(this, plateNumber, Toast.LENGTH_LONG).show();
		int confidence = tessBaseAPI.meanConfidence();
		Log.v(TAG, "Mean confidence: " + confidence);
		Toast.makeText(this, "confidence: " + confidence, Toast.LENGTH_LONG)
				.show();

		return plateNumber;
	}

	public static IplImage findPlateImage(String vehicleImagePath) {
		IplImage plateImage = null;

		// progressing order indicator
		int poi = 1;

		// the original vehicle image
		IplImage vehicleOriginalImage = cvLoadImage(vehicleImagePath);

		if (vehicleOriginalImage != null) {
			// !!!! image process:

			IplImage vehicleImage = IplImage.create(
					vehicleOriginalImage.width(),
					vehicleOriginalImage.height(), IPL_DEPTH_8U, 1);

			// convert vehicle image into grayscale
			cvCvtColor(vehicleOriginalImage, vehicleImage, CV_BGR2GRAY);
			if (SAVE_IMG) {
				cvSaveImage(SAVE_IMAGE_PATH + (poi++) + ".cvCvtColor.jpg",
						vehicleImage);
			}

			// create a copy of the grayscale vehicle image
			IplImage vehicleImageCopy = IplImage.create(vehicleImage.width(),
					vehicleImage.height(), IPL_DEPTH_8U, 1);
			cvCopy(vehicleImage, vehicleImageCopy);

			// smooth the grayscale image
			cvSmooth(vehicleImage, vehicleImage, CV_GAUSSIAN, 3);
			if (SAVE_IMG) {
				cvSaveImage(SAVE_IMAGE_PATH + (poi++) + ".cvSmooth().jpg",
						vehicleImage);
			}

			// enhance the brightness and contrast of the smoothed grayscale
			// image
			cvEqualizeHist(vehicleImage, vehicleImage);
			if (SAVE_IMG) {
				cvSaveImage(SAVE_IMAGE_PATH + (poi++)
						+ ".cvEqualizeHist();.jpg", vehicleImage);
			}

			// convert the enhanced and smoothed grayscale image into binary
			// image
			cvThreshold(vehicleImage, vehicleImage, 128, 255, CV_THRESH_BINARY);
			if (SAVE_IMG) {
				cvSaveImage(SAVE_IMAGE_PATH + (poi++) + ".cvThreshold().jpg",
						vehicleImage);
			}

			// find plate-like contour from the binary image
			CvSeq plateLikeContour = new CvSeq(null);
			int plateLikeContours = cvFindContours(vehicleImage,
					cvCreateMemStorage(0), plateLikeContour,
					Loader.sizeof(CvContour.class), CV_RETR_LIST,
					CV_CHAIN_APPROX_SIMPLE);
			if (SAVE_IMG) {
				cvSaveImage(
						SAVE_IMAGE_PATH + (poi++) + ".cvFindContours().jpg",
						vehicleImage);
			}
			Log.v(TAG, "plateLikeContours: " + plateLikeContours);

			// !!!! licence plate detection:

			// looping through all the plate-like contours to find the plate
			// image
			// TODO:
			while (plateLikeContour != null && !plateLikeContour.isNull()) {
				if (plateLikeContour.elem_size() > 0) {
					// build a polygon surrounding the plate-like contour
					CvSeq polyPlateLikeContour = cvApproxPoly(plateLikeContour,
							Loader.sizeof(CvContour.class),
							cvCreateMemStorage(0), CV_POLY_APPROX_DP,
							cvContourPerimeter(plateLikeContour) * 0.05, 0);

					// if the length of the polygon is 4 then check its
					// width and height ratio
					if (polyPlateLikeContour.total() == 4
							&& cvContourArea(polyPlateLikeContour,
									CV_WHOLE_SEQ, 0) > 600) {
						// find the bounding charRectangle of the polygon
						CvRect plateRect = cvBoundingRect(polyPlateLikeContour,
								0);
						double ratioWH = ((double) plateRect.width())
								/ ((double) plateRect.height());

						// if the width and height ratio (standard
						// 440/140==3.14) is
						// between 2.8 and 3.4 we suppose, this area has licence
						// plate and we extract that area from grayscale image
						if (ratioWH > 2.8 && ratioWH < 3.4) {
							// set vehicleImageCopy's ROI the plateRect
							cvSetImageROI(vehicleImageCopy, plateRect);

							// licence plate image rotation:

							IplImage plateLikeImage;
							plateLikeImage = IplImage.create(plateRect.width(),
									plateRect.height(), IPL_DEPTH_8U, 1);
							// set plateLikeImage pure black
							cvZero(plateLikeImage);

							CvPoint2D32f plateImageCen = new CvPoint2D32f();
							plateImageCen.x(plateLikeImage.width() / 2.0f);
							plateImageCen.y(plateLikeImage.height() / 2.0f);

							CvBox2D box = cvMinAreaRect2(polyPlateLikeContour,
									cvCreateMemStorage(0));

							// rotate the plateLikeImage
							CvMat transformMat = CvMat.create(2, 3, CV_32FC1);
							cv2DRotationMatrix(plateImageCen, box.angle(), 1.0,
									transformMat);

							cvWarpAffine(vehicleImageCopy, plateLikeImage,
									transformMat, CV_INTER_LINEAR
											+ CV_WARP_FILL_OUTLIERS,
									cvScalarAll(0));
							if (SAVE_IMG) {
								cvSaveImage(SAVE_IMAGE_PATH + (poi++)
										+ ".cvWarpAffine().jpg", plateLikeImage);
							}

							// create a clone of plate image
							IplImage plateImageClone = plateLikeImage.clone();

							// character segmentation:

							IplImage plateImageCanny = IplImage.create(
									plateLikeImage.width(),
									plateLikeImage.height(), IPL_DEPTH_8U, 1);
							cvCanny(plateLikeImage, plateImageCanny, 128, 255,
									3);
							if (SAVE_IMG) {
								cvSaveImage(SAVE_IMAGE_PATH + (poi++)
										+ ".cvCanny().jpg", plateImageCanny);
							}

							// create a all white clone of plate image
							IplImage plateLikeImageClone = plateLikeImage
									.clone();
							cvSet(plateLikeImageClone, CV_RGB(255.0, 255, 255));
							if (SAVE_IMG) {
								cvSaveImage(SAVE_IMAGE_PATH + (poi++)
										+ ".cvSet().jpg", plateLikeImageClone);
							}

							CvSeq charLikeContour = new CvSeq(null);
							int charLikeContours = cvFindContours(
									plateImageCanny, cvCreateMemStorage(0),
									charLikeContour,
									Loader.sizeof(CvContour.class),
									CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE,
									cvPoint(0, 0));
							Log.v(TAG, "charLikeContours: " + charLikeContours);

							// legal chars
							int chars = 0;

							// TODO:
							while (charLikeContour != null
									&& !charLikeContour.isNull()) {
								if (charLikeContour.elem_size() > 0) {
									CvSeq polyCharLikeContour = cvApproxPoly(
											charLikeContour,
											Loader.sizeof(CvContour.class),
											cvCreateMemStorage(0),
											CV_POLY_APPROX_DP, 1, 0);

									CvRect charRect = cvBoundingRect(
											polyCharLikeContour, 0);
									// if the bounding charRectangle height is
									// greater
									// than the half times height of the licence
									// plate, then it is supposed that
									// charRectangle
									// area belongs to licence number and it is
									// copied to new image as we can see
									if (2 * charRect.height() > plateLikeImage
											.height()
											&& 2 * charRect.width() < plateLikeImage
													.width()) {
										cvSetImageROI(plateImageClone, charRect);
										if (SAVE_IMG) {
											cvSaveImage(SAVE_IMAGE_PATH
													+ (poi++)
													+ ".cvSetImageROI().jpg",
													plateImageClone);
										}
										cvSetImageROI(plateLikeImageClone,
												charRect);
										cvCopy(plateImageClone,
												plateLikeImageClone);
										cvResetImageROI(plateImageClone);
										cvResetImageROI(plateLikeImageClone);
										chars++;
									}
								}
								charLikeContour = charLikeContour.h_next();
							}// while (charLikeContour != null &&
								// !charLikeContour.isNull())

							// if all chars are found, stop searching!
							if (chars == 7) {
								plateImage = plateLikeImageClone;
								break;
							}

						}// if (ratioWH > 2.8 && ratioWH < 3.4)

					}// if (polyPlateLikeContour.total() == 4 &&
						// cvContourArea(polyPlateLikeContour, CV_WHOLE_SEQ, 0)
						// > 600)
				}// if (plateLikeContour.elem_size() > 0)
				plateLikeContour = plateLikeContour.h_next();
			}// while (plateLikeContour != null && !plateLikeContour.isNull())

			if (plateImage != null) {
				cvThreshold(plateImage, plateImage, 100, 255, CV_THRESH_BINARY);
				if (SAVE_IMG) {
					cvSaveImage(SAVE_IMAGE_PATH + (poi++)
							+ ".cvThreshold().jpg", plateImage);
				}

				cvResetImageROI(vehicleImageCopy);

			}

		} // if (vehicleImage != null)

		return plateImage;
	}

	private class ResultEditText extends EditText {
		private final EditText mEditText = this;
		private static final int MAXLEN = 1;

		/**
		 * 
		 * @param context
		 * @param i
		 *            this edit text's index at resultTexts.
		 * @param resultTexts
		 */
		public ResultEditText(final int i) {
			super(VehicleLicenceInputActivity.this);

			// Set max length of MAXLEN.
			setFilters(new InputFilter[] { new InputFilter.LengthFilter(MAXLEN) });

			setGravity(Gravity.CENTER);

			setMaxLines(1);

			setOnFocusChangeListener(new OnFocusChangeListener() {

				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						EditText editText = (EditText) v;
						editText.selectAll();
					}
				}
			});

			addTextChangedListener(new TextWatcher() {

				public void onTextChanged(CharSequence s, int start,
						int before, int count) {
				}

				public void beforeTextChanged(CharSequence s, int start,
						int count, int after) {
				}

				public void afterTextChanged(Editable s) {
					String text = mEditText.getText().toString();
					if (text.length() > 0) {
						if (Character.isLowerCase(text.charAt(0))) {
							mEditText.setText(text.toUpperCase());
						}
						// Move to the next result text.
						mResultTexts[(i + 1) % mResultTexts.length]
								.requestFocus();
					}

					StringBuilder builder = new StringBuilder();
					for (int col = 0; col < PLATECHARS; col++) {
						builder.append(mResultTexts[col].getText().toString());
					}
					mLicenceText.setText(builder.toString());
				}
			});
		}
	}

	private class CandidateButton extends Button {

		public CandidateButton(Context context, final int row, final int col) {
			super(context);

			setText("" + SPACE);

			setTextColor(Color.BLACK);

			setBackgroundColor(row == 0 ? Color.GRAY : Color.WHITE);

			setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					// Save the selection.
					mSelections[col] = row;

					// Update the color of the buttons in the candidate col.
					for (int i = 0; i < CANDIDATES; i++) {
						if (i == row) {
							mCandidateButtons[i][col]
									.setBackgroundColor(Color.GRAY);
						} else {
							mCandidateButtons[i][col]
									.setBackgroundColor(Color.WHITE);
						}
					}

					mResultTexts[col].setText("" + mCandidateChars[row][col]);
				}
			});
		}

	}

}
