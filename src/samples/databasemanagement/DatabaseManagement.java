package samples.databasemanagement;

import java.util.List;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.FeedOptions;

import samples.utils.ConnectUtil;
/**
 * 
 * @author cmgit
 * 
 * Demonstrates basics of creating and deleting a database
 * 
 * Shows two methods of creation because Java SDK does not include create if not exists method like C# counterpart.
 * 
 * 
 * 
 * Method 1 iterates available databases incurring cost of the lookup.
 * 
 * Method 2 uses caught 404 but exception handling is now part of control flow.   
 *
 */
public class DatabaseManagement {

	private static final String DATABASE_NAME = "samples2";
	private static final String DB_URL = "dbs/" + DATABASE_NAME;
	private static DocumentClient client;

	public static void main(String[] args) {
		ConnectionPolicy policy = new ConnectionPolicy();
		policy.setUserAgentSuffix(" samples-net/3");
		ConsistencyLevel level = ConsistencyLevel.Session;
		client = ConnectUtil.getClient(policy, level);
		try {
			createDatabaseIfNotExistUsingReadDatabase();
			client.deleteDatabase(DB_URL, null);
			createDatabaseIfNotExistCatchingStatusCode();
		} catch (DocumentClientException e) {
			System.err.println(e.getStatusCode() + ":" + e.getMessage());
		}

	}

	private static void createDatabaseIfNotExistCatchingStatusCode() throws DocumentClientException {
		System.out.println("Creating database " + DATABASE_NAME + " if it doesn't existby catching 404");
		boolean create = false;
		try {
			client.readDatabase(DB_URL, null);
		} catch (DocumentClientException e) {
			if (e.getStatusCode() != 404) {
				throw e;
			} else {
				create = true;
			}
		}
		if (create) {
			System.out.println(DATABASE_NAME + " not found creating...");
			Database db = new Database();
			db.setId(DATABASE_NAME);
			client.createDatabase(db, null);
		}
	}

	private static void createDatabaseIfNotExistUsingReadDatabase() throws DocumentClientException {
		System.out.println("Creating database " + DATABASE_NAME + " if it doesn't exist by readDatabase and iterating");
		FeedOptions options = new FeedOptions();
		options.setPageSize(100);
		List<Database> dbs = client.readDatabases(options).getQueryIterable().toList();
		Database db = new Database();
		db.setId(DATABASE_NAME);
		if (dbs.isEmpty()) {
			System.out.println(DATABASE_NAME + " not found creating...");
			client.createDatabase(db, null);

		} else {
			boolean create = true;
			for(Database candidate:dbs){
				if(candidate.getId().equals(DATABASE_NAME)){
					create = false;
					break;
				}
			}
			if(create){
				System.out.println(DATABASE_NAME + " not found creating...");
				client.createDatabase(db, null);
			}
				
		}
	}

}
