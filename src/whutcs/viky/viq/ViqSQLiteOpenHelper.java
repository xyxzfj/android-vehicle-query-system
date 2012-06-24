package whutcs.viky.viq;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Provides access to the underlying database elements -- tables, views and
 * columns.
 * 
 * @author xyxzfj@gmail.com
 * 
 */
public class ViqSQLiteOpenHelper extends SQLiteOpenHelper {
	// private static final String TAG = "ViqSQLiteOpenHelper";

	// Database schema.
	public static final String DB_VIQ = "viq.db";
	public static final String TABLE_QUERY = "Query";
	public static final String TABLE_INFO = "Info";
	public static final String VIEW_QUERY_INFO = "QueryInfo";
	public static final int DB_VERSION = 20;

	// TABLE QUERY:
	public static final String[] TABLE_QUERY_COLUMNS_SELECTED = new String[] {
			"_id", "time", "place", "note", "photo" };
	public static final String TABLE_QUERY_SELECTION = getSelection(TABLE_QUERY_COLUMNS_SELECTED);
	// column "_licence_" is never SELECTed in selection clause.
	public static final String[] TABLE_QUERY_COLUMNS = new String[] { "_id",
			"time", "place", "note", "photo", "_licence_" };
	public static final int TABLE_QUERY_COLUMN_TIME = 1;
	public static final int TABLE_QUERY_COLUMN_PLACE = 2;
	public static final int TABLE_QUERY_COLUMN_NOTE = 3;
	public static final int TABLE_QUERY_COLUMN_PHOTO = 4;
	public static final int TABLE_QUERY_COLUMN_LICENCE = 5;

	// TABLE INFO:
	public static final String[] TABLE_INFO_COLUMNS = new String[] { "_id",
			"licence", "type", "vin", "name", "phone", "gender", "birth",
			"driving_licence", "note", "photo" };
	public static final String TABLE_INFO_SELECTION = getSelection(TABLE_INFO_COLUMNS);
	public static final int TABLE_INFO_COLUMN_LICENCE = 1;
	public static final int TABLE_INFO_COLUMN_TYPE = 2;
	public static final int TABLE_INFO_COLUMN_VIN = 3;
	public static final int TABLE_INFO_COLUMN_NAME = 4;
	public static final int TABLE_INFO_COLUMN_PHONE = 5;
	public static final int TABLE_INFO_COLUMN_GENDER = 6;
	public static final int TABLE_INFO_COLUMN_BIRTH = 7;
	public static final int TABLE_INFO_COLUMN_DRIVING_LICENCE = 8;
	public static final int TABLE_INFO_COLUMN_NOTE = 9;
	public static final int TABLE_INFO_COLUMN_PHOTO = 10;

	// VIEW QUERY_INFO:
	public static final String[] VIEW_QUERY_INFO_COLUMNS = new String[] {
			"_id", "licence", "name", "phone", "time", "place", "note", "photo" };
	public static final String VIEW_QUERY_INFO_SELECTION = getSelection(VIEW_QUERY_INFO_COLUMNS);
	public static final int VIEW_QUERY_INFO_COLUMN_LICENCE = 1;
	public static final int VIEW_QUERY_INFO_COLUMN_NAME = 2;
	public static final int VIEW_QUERY_INFO_COLUMN_PHONE = 3;
	public static final int VIEW_QUERY_INFO_COLUMN_TIME = 4;
	public static final int VIEW_QUERY_INFO_COLUMN_PLACE = 5;
	public static final int VIEW_QUERY_INFO_COLUMN_NOTE = 6;
	public static final int VIEW_QUERY_INFO_COLUMN_PHOTO = 7;

	// Creation sql of tables and views.
	/**
	 * The _licence_ field in table Query serves as a reference to table Info.
	 */
	public static final String SQL_CREATE_TABLE_QUERY = genCreateTableSql(
			TABLE_QUERY, TABLE_QUERY_COLUMNS);
	public static final String SQL_CREATE_TABLE_INFO = genCreateTableSql(
			TABLE_INFO, TABLE_INFO_COLUMNS);
	public static final String SQL_CREATE_VIEW_QUERY_INFO = "CREATE VIEW QueryInfo "
			+ "AS SELECT Query._id AS _id, _licence_ AS licence,name,phone,time,place,Query.note AS note,Query.photo AS photo "
			+ "FROM Query LEFT OUTER JOIN Info ON _licence_=licence";

	private static String genCreateTableSql(String tableName,
			String[] columnsWithIDFirst) {
		if (tableName == null || tableName.length() == 0
				|| columnsWithIDFirst.length == 0
				|| !columnsWithIDFirst[0].equals("_id")) {
			return null;
		}

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("CREATE TABLE ").append(tableName).append("(");
		sqlBuilder.append("_id INTEGER PRIMARY KEY AUTOINCREMENT");
		for (int i = 1; i < columnsWithIDFirst.length; i++) {
			sqlBuilder.append(",").append(columnsWithIDFirst[i]);
		}
		sqlBuilder.append(")");

		return sqlBuilder.toString();
	}

	/**
	 * Generate a selection clause of OR with all columns " LIKE %?% ".
	 * 
	 * @param columns
	 * @return
	 */
	private static String getSelection(String[] columns) {
		if (columns.length == 0) {
			return null;
		}

		StringBuilder builder = new StringBuilder("(");
		for (int i = 0; i < columns.length - 1; i++) {
			builder.append(columns[i]);
			builder.append(" LIKE ? OR ");
		}
		builder.append(columns[columns.length - 1]);
		builder.append(" LIKE ?)");

		return builder.toString();
	}

	/**
	 * Get the corresponding selectionArgs with getSelection().
	 * 
	 * @param filter
	 * @return selectionArgs
	 */
	public static String[] getSelectiionArgs(String filter, int columns) {
		if (columns == 0) {
			return null;
		}

		String[] selectionArgs = new String[columns];
		for (int i = 0; i < selectionArgs.length; i++) {
			selectionArgs[i] = "%" + filter + "%";
		}

		return selectionArgs;
	}

	public ViqSQLiteOpenHelper(Context context) {
		super(context, DB_VIQ, null, DB_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.delete(TABLE_QUERY, null, null);
		db.delete(TABLE_INFO, null, null);

		sampleInsert(db);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_TABLE_INFO);
		db.execSQL(SQL_CREATE_TABLE_QUERY);
		db.execSQL(SQL_CREATE_VIEW_QUERY_INFO);

		sampleInsert(db);

	}

	void sampleInsert(SQLiteDatabase db) {
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12245','A1','LSGPC52U7AF127561','小杨','13667147300','男','1989-11','371322198701202314','有醉酒驾车前科','2012-05-22_18-41-32.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12246','A2','LSGPC52U7AF127562','王小杨','13667147311','女','1989-11','371322198701202311','2012年5月新学会开车','IMG_20120304_143047.jpg')");

		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12301','A1','LSGPC52U7AF127561','车主1','13667147301','男','1989-1','371322198701202301','备注1','IMG_20120220_073130.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12302','A2','LSGPC52U7AF127562','车主2','13667147302','男','1989-2','371322198701202302','备注2','IMG_20120304_142701.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12303','A3','LSGPC52U7AF127563','车主3','13667147303','男','1989-3','371322198701202303','备注3','IMG_20120304_143026.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12304','A4','LSGPC52U7AF127564','车主4','13667147304','男','1989-4','371322198701202304','备注4','2012-05-22_18-49-38.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12305','A5','LSGPC52U7AF127565','车主5','13667147305','男','1989-5','371322198701202305','备注5','IMG_20120220_073139.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12306','A6','LSGPC52U7AF127566','车主6','13667147306','男','1989-6','371322198701202306','备注6','IMG_20120220_073148.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12307','A7','LSGPC52U7AF127567','车主7','13667147307','男','1989-7','371322198701202307','备注7','IMG_20120220_073157.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12308','A8','LSGPC52U7AF127568','车主8','13667147308','男','1989-8','371322198701202308','备注8','IMG_20120220_073203.bmp')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12309','A9','LSGPC52U7AF127569','车主9','13667147309','男','1989-9','371322198701202309','备注9','IMG_20120220_073203.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12310','A0','LSGPC52U7AF127570','车主10','13667147310','男','1989-10','371322198701202310','备注10','IMG_20120220_073212.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12311','A1','LSGPC52U7AF127571','车主11','13667147311','男','1989-11','371322198701202311','备注11','IMG_20120220_073225.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12312','A2','LSGPC52U7AF127572','车主12','13667147312','男','1989-12','371322198701202312','备注12','IMG_20120304_142701.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12313','A3','LSGPC52U7AF127573','车主13','13667147313','男','1990-1','371322198701202313','备注13','IMG_20120304_142706.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12314','A4','LSGPC52U7AF127574','车主14','13667147314','男','1990-2','371322198701202314','备注14','IMG_20120304_142715.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12315','A5','LSGPC52U7AF127575','车主15','13667147315','男','1990-3','371322198701202315','备注15','IMG_20120304_142847.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12316','A6','LSGPC52U7AF127576','车主16','13667147316','男','1990-4','371322198701202316','备注16','IMG_20120304_142854.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12317','A7','LSGPC52U7AF127577','车主17','13667147317','男','1990-5','371322198701202317','备注17','IMG_20120304_142901.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12318','A8','LSGPC52U7AF127578','车主18','13667147318','男','1990-6','371322198701202318','备注18','IMG_20120304_142909.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12319','A9','LSGPC52U7AF127579','车主19','13667147319','男','1990-7','371322198701202319','备注19','IMG_20120304_143011.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12320','A0','LSGPC52U7AF127580','车主20','13667147320','男','1990-8','371322198701202320','备注20','IMG_20120304_143015.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12321','A1','LSGPC52U7AF127581','车主21','13667147321','男','1990-9','371322198701202321','备注21','IMG_20120304_143026.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12322','A2','LSGPC52U7AF127582','车主22','13667147322','男','1990-10','371322198701202322','备注22','IMG_20120304_143034.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12323','A3','LSGPC52U7AF127583','车主23','13667147323','男','1990-11','371322198701202323','备注23','IMG_20120304_143042.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12324','A4','LSGPC52U7AF127584','车主24','13667147324','女','1990-12','371322198701202324','备注24','IMG_20120304_143047.jpg')");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12325','A5','LSGPC52U7AF127585','车主25','13667147325','女','1991-1','371322198701202325','备注25',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12326','A6','LSGPC52U7AF127586','车主26','13667147326','女','1991-2','371322198701202326','备注26',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12327','A7','LSGPC52U7AF127587','车主27','13667147327','女','1991-3','371322198701202327','备注27',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12328','A8','LSGPC52U7AF127588','车主28','13667147328','女','1991-4','371322198701202328','备注28',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12329','A9','LSGPC52U7AF127589','车主29','13667147329','女','1991-5','371322198701202329','备注29',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12330','A0','LSGPC52U7AF127590','车主30','13667147330','女','1991-6','371322198701202330','备注30',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12331','A1','LSGPC52U7AF127591','车主31','13667147331','女','1991-7','371322198701202331','备注31',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12332','A2','LSGPC52U7AF127592','车主32','13667147332','女','1991-8','371322198701202332','备注32',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12333','A3','LSGPC52U7AF127593','车主33','13667147333','女','1991-9','371322198701202333','备注33',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12334','A4','LSGPC52U7AF127594','车主34','13667147334','女','1991-10','371322198701202334','备注34',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12335','A5','LSGPC52U7AF127595','车主35','13667147335','女','1991-11','371322198701202335','备注35',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12336','A6','LSGPC52U7AF127596','车主36','13667147336','女','1991-12','371322198701202336','备注36',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12337','A7','LSGPC52U7AF127597','车主37','13667147337','女','1992-1','371322198701202337','备注37',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12338','A8','LSGPC52U7AF127598','车主38','13667147338','女','1992-2','371322198701202338','备注38',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12339','A9','LSGPC52U7AF127599','车主39','13667147339','女','1992-3','371322198701202339','备注39',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12340','A0','LSGPC52U7AF127600','车主40','13667147340','女','1992-4','371322198701202340','备注40',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12341','A1','LSGPC52U7AF127601','车主41','13667147341','女','1992-5','371322198701202341','备注41',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12342','A2','LSGPC52U7AF127602','车主42','13667147342','女','1992-6','371322198701202342','备注42',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12343','A3','LSGPC52U7AF127603','车主43','13667147343','女','1992-7','371322198701202343','备注43',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12344','A4','LSGPC52U7AF127604','车主44','13667147344','女','1992-8','371322198701202344','备注44',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12345','A5','LSGPC52U7AF127605','车主45','13667147345','女','1992-9','371322198701202345','备注45',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12346','A6','LSGPC52U7AF127606','车主46','13667147346','女','1992-10','371322198701202346','备注46',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12347','A7','LSGPC52U7AF127607','车主47','13667147347','女','1992-11','371322198701202347','备注47',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12348','A8','LSGPC52U7AF127608','车主48','13667147348','女','1992-12','371322198701202348','备注48',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12349','A9','LSGPC52U7AF127609','车主49','13667147349','女','1993-1','371322198701202349','备注49',null)");
		db.execSQL("INSERT INTO Info VALUES(null,'鄂A12350','A0','LSGPC52U7AF127610','车主50','13667147350','女','1993-2','371322198701202350','备注50',null)");

		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:18','湖北省武汉市青山区武汉理工大学地点1','备注1',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:19','湖北省武汉市青山区武汉理工大学地点2','备注2',null,'鄂A12303')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:20','湖北省武汉市青山区武汉理工大学地点3','备注3',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:21','湖北省武汉市青山区武汉理工大学地点4','备注4',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:22','湖北省武汉市青山区武汉理工大学地点5','备注5',null,'鄂A12303')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:23','湖北省武汉市青山区武汉理工大学地点6','备注6',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:24','湖北省武汉市青山区武汉理工大学地点7','备注7',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:25','湖北省武汉市青山区武汉理工大学地点8','备注8',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:26','湖北省武汉市青山区武汉理工大学地点9','备注9',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:27','湖北省武汉市青山区武汉理工大学地点10','备注10',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:28','湖北省武汉市青山区武汉理工大学地点11','备注11',null,'鄂A12303')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:29','湖北省武汉市青山区武汉理工大学地点12','备注12',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:30','湖北省武汉市青山区武汉理工大学地点13','备注13',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:31','湖北省武汉市青山区武汉理工大学地点14','备注14',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:32','湖北省武汉市青山区武汉理工大学地点15','备注15',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:33','湖北省武汉市青山区武汉理工大学地点16','备注16',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:34','湖北省武汉市青山区武汉理工大学地点17','备注17',null,'鄂A12303')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:35','湖北省武汉市青山区武汉理工大学地点18','备注18',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:36','湖北省武汉市青山区武汉理工大学地点19','备注19',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:37','湖北省武汉市青山区武汉理工大学地点20','备注20',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:38','湖北省武汉市青山区武汉理工大学地点21','备注21',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:39','湖北省武汉市青山区武汉理工大学地点22','备注22',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:40','湖北省武汉市青山区武汉理工大学地点23','备注23',null,'鄂A12303')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:41','湖北省武汉市青山区武汉理工大学地点24','备注24',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:42','湖北省武汉市青山区武汉理工大学地点25','备注25',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:43','湖北省武汉市青山区武汉理工大学地点26','备注26',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:44','湖北省武汉市青山区武汉理工大学地点27','备注27','IMG_20120304_142901.jpg','鄂A12304')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:45','湖北省武汉市青山区武汉理工大学地点28','备注28',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:46','湖北省武汉市青山区武汉理工大学地点29','备注29',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:47','湖北省武汉市青山区武汉理工大学地点30','备注30',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:48','湖北省武汉市青山区武汉理工大学地点31','备注31',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:49','湖北省武汉市青山区武汉理工大学地点32','备注32',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:50','湖北省武汉市青山区武汉理工大学地点33','备注33',null,'鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:51','湖北省武汉市青山区武汉理工大学地点34','备注34',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:52','湖北省武汉市青山区武汉理工大学地点35','备注35',null,'鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:53','湖北省武汉市青山区武汉理工大学地点36','备注36',null,'鄂A12303')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:54','湖北省武汉市青山区武汉理工大学地点37','备注37',null,'鄂A12303')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:55','湖北省武汉市青山区武汉理工大学地点38','备注38','IMG_20120304_142847.jpg','鄂A12304')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:56','湖北省武汉市青山区武汉理工大学地点39','备注39',null,'鄂A12303')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:57','湖北省武汉市青山区武汉理工大学地点40','备注40','IMG_20120304_143015.jpg','鄂A123013')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:58','湖北省武汉市青山区武汉理工大学地点41','备注41','IMG_20120304_143011.jpg','鄂A12303')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:59','湖北省武汉市青山区武汉理工大学地点42','备注42','IMG_20120304_142715.jpg','鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:60','湖北省武汉市青山区武汉理工大学地点43','备注43','IMG_20120304_142706.jpg','鄂A12302')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:61','湖北省武汉市青山区武汉理工大学地点44','备注44','IMG_20120220_073225.jpg','鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:62','湖北省武汉市青山区武汉理工大学地点45','备注45','IMG_20120220_073212.jpg','鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:63','湖北省武汉市青山区武汉理工大学地点46','备注46','IMG_20120220_073203.jpg','鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:64','湖北省武汉市青山区武汉理工大学地点47','备注47','IMG_20120220_073203.bmp','鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:65','湖北省武汉市青山区武汉理工大学地点48','备注48','IMG_20120220_073157.jpg','鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:66','湖北省武汉市青山区武汉理工大学地点49','备注49','IMG_20120220_073148.jpg','鄂A12301')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:67','湖北省武汉市青山区武汉理工大学地点50','备注50','IMG_20120220_073139.jpg','鄂A12301')");

		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–22 16:52:20','湖北省武汉市青山区武汉理工大学海虹五栋','','IMG_20120304_143034.jpg','鄂A12246')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–25 16:52:20','湖北省武汉市青山区武汉理工大学海虹四栋','违规停放','IMG_20120304_143042.jpg','鄂A12246')");
		db.execSQL("INSERT INTO Query VALUES(null,'2012–04–20 16:52:18','湖北省武汉市青山区武汉理工大学','未缴纳停车费','2012-05-22_18-49-13.jpg','鄂A12245')");
	}
}
