package org.provider;

import org.database.DatabaseHelper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class DmdContentProvider extends ContentProvider {

	private DatabaseHelper mDB;
	private SQLiteDatabase bdd;

	private static final String AUTHORITY = "org.provider.DmdContentProvider";
	public static final int REQUEST_AREA = 100;
	public static final int REQUEST_ROOM = 110;
	public static final int REQUEST_ICON = 120;
	public static final int REQUEST_FEATURE_ALL = 130;
	public static final int REQUEST_FEATURE_MAP = 140;
	public static final int REQUEST_FEATURE_ID = 150;
	public static final int REQUEST_FEATURE_STATE = 160;

	public static final int INSERT_AREA = 200;
	public static final int INSERT_ROOM = 210;
	public static final int INSERT_ICON = 220;
	public static final int INSERT_FEATURE = 230;
	public static final int INSERT_FEATURE_ASSOCIATION = 240;
	public static final int INSERT_FEATURE_MAP = 250;
	public static final int INSERT_FEATURE_STATE = 260;
	
	public static final int UPDATE_FEATURE_STATE = 300;
	
	public static final int UPGRADE_FEATURE_STATE = 400;

	private static final String DOMODROID_BASE_PATH = "domodroid";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH);
	public static final Uri CONTENT_URI_REQUEST_AREA = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/REQUEST_AREA");
	public static final Uri CONTENT_URI_REQUEST_ROOM = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/REQUEST_ROOM");
	public static final Uri CONTENT_URI_REQUEST_ICON = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/REQUEST_ICON");
	public static final Uri CONTENT_URI_REQUEST_FEATURE_ALL = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ALL");
	public static final Uri CONTENT_URI_REQUEST_FEATURE_MAP = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_MAP");
	public static final Uri CONTENT_URI_REQUEST_FEATURE_ID = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ID");
	public static final Uri CONTENT_URI_REQUEST_FEATURE_STATE = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/REQUEST_FEATURE_STATE");

	public static final Uri CONTENT_URI_INSERT_AREA = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/INSERT_AREA");
	public static final Uri CONTENT_URI_INSERT_ROOM = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/INSERT_ROOM");
	public static final Uri CONTENT_URI_INSERT_ICON = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/INSERT_ICON");
	public static final Uri CONTENT_URI_INSERT_FEATURE = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/INSERT_FEATURE");
	public static final Uri CONTENT_URI_INSERT_FEATURE_ASSOCIATION = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/INSERT_FEATURE_ASSOCIATION");
	public static final Uri CONTENT_URI_INSERT_FEATURE_MAP = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/INSERT_FEATURE_MAP");
	public static final Uri CONTENT_URI_INSERT_FEATURE_STATE = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/INSERT_FEATURE_STATE");

	public static final Uri CONTENT_URI_UPDATE_FEATURE_STATE = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/UPDATE_FEATURE_STATE");

	public static final Uri CONTENT_URI_UPGRADE_FEATURE_STATE = Uri.parse("content://" + AUTHORITY+ "/" + DOMODROID_BASE_PATH + "/UPGRADE_FEATURE_STATE");


	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/domodroid";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/domodroid";

	private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_AREA", REQUEST_AREA);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_ROOM", REQUEST_ROOM);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_ICON", REQUEST_ICON);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ALL", REQUEST_FEATURE_ALL);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_MAP", REQUEST_FEATURE_MAP);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_ID", REQUEST_FEATURE_ID);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/REQUEST_FEATURE_STATE", REQUEST_FEATURE_STATE);

		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_AREA", INSERT_AREA);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_ROOM", INSERT_ROOM);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_ICON", INSERT_ICON);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_FEATURE", INSERT_FEATURE);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_FEATURE_ASSOCIATION", INSERT_FEATURE_ASSOCIATION);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_FEATURE_MAP", INSERT_FEATURE_MAP);
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/INSERT_FEATURE_STATE", INSERT_FEATURE_STATE);
		
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPDATE_FEATURE_STATE", UPDATE_FEATURE_STATE);
		
		sURIMatcher.addURI(AUTHORITY, DOMODROID_BASE_PATH + "/UPGRADE_FEATURE_STATE", UPGRADE_FEATURE_STATE);
	}

	@Override
	public boolean onCreate() {
		mDB = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		if(uriType == UPGRADE_FEATURE_STATE){
			bdd = mDB.getWritableDatabase();
			bdd.delete("table_area", null, null);
			bdd.delete("table_room", null, null);
			bdd.delete("table_icon", null, null);
			bdd.delete("table_feature", null, null);
			bdd.delete("table_feature_association", null, null);
			bdd.delete("table_feature_state", null, null);
			bdd.delete("table_feature_map", null, null);
			mDB = null;
			mDB = new DatabaseHelper(getContext());
		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = sURIMatcher.match(uri);
		long id = 0;
		switch (uriType) {
		case INSERT_AREA:
			mDB.getWritableDatabase().insert("table_area", null, values);
			break;
		case INSERT_ROOM:
			mDB.getWritableDatabase().insert("table_room", null, values);
			break;
		case INSERT_ICON:
			mDB.getWritableDatabase().insert("table_icon", null, values);
			break;
		case INSERT_FEATURE:
			mDB.getWritableDatabase().insert("table_feature", null, values);
			break;
		case INSERT_FEATURE_ASSOCIATION:
			mDB.getWritableDatabase().insert("table_feature_association", null, values);
			break;
		case INSERT_FEATURE_MAP:
			mDB.getWritableDatabase().insert("table_feature_map", null, values);
			break;
		case INSERT_FEATURE_STATE:
			mDB.getWritableDatabase().insert("table_feature_state", null, values);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI= "+uri);
		}
		return Uri.parse(DOMODROID_BASE_PATH + "/" + id);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		Cursor cursor=null;
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case REQUEST_AREA:
			queryBuilder.setTables("table_area");
			cursor = queryBuilder.query(mDB.getReadableDatabase(),projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case REQUEST_ROOM:
			queryBuilder.setTables("table_room");
			cursor = queryBuilder.query(mDB.getReadableDatabase(),projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case REQUEST_ICON:
			queryBuilder.setTables("table_icon");
			cursor = queryBuilder.query(mDB.getReadableDatabase(),projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case REQUEST_FEATURE_ALL:
			cursor=mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature INNER JOIN table_feature_association ON table_feature.id = table_feature_association.device_feature_id GROUP BY device_id,state_key",null);
			break;
		case REQUEST_FEATURE_MAP:
			cursor=mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature INNER JOIN table_feature_map ON table_feature.id = table_feature_map.id",null);
			break;
		case REQUEST_FEATURE_ID:
			cursor=mDB.getReadableDatabase().rawQuery("SELECT * FROM table_feature INNER JOIN table_feature_association ON table_feature.id = table_feature_association.device_feature_id WHERE table_feature_association.place_id = "+selectionArgs[0]+" AND table_feature_association.place_type="+"\""+selectionArgs[1]+"\"",null);
			break;
		case REQUEST_FEATURE_STATE:
			queryBuilder.setTables("table_feature_state");
			cursor = queryBuilder.query(mDB.getReadableDatabase(),projection, selection, selectionArgs, null, null, sortOrder);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI");
		}
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int uriType = sURIMatcher.match(uri);
		switch (uriType) {
		case UPDATE_FEATURE_STATE:
			mDB.getWritableDatabase().update("table_feature_state", values, selection,selectionArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return 0;
	}
}
