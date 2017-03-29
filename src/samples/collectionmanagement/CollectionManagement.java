package samples.collectionmanagement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.IndexingMode;
import com.microsoft.azure.documentdb.IndexingPolicy;
import com.microsoft.azure.documentdb.Offer;
import com.microsoft.azure.documentdb.OfferV2;
import com.microsoft.azure.documentdb.PartitionKeyDefinition;
import com.microsoft.azure.documentdb.RequestOptions;

import samples.utils.ConnectUtil;

/**
 * 
 * @author cmgit
 * 
 *         Demonstrates collection management CRUD operations.
 * 
 * 
 *
 *         NOTE: COLLECTION IS A UNIT OF BILLING IN DOCUMENTDB
 * 
 *         Running this sample will create (and delete) multiple
 *         DocumentCollections on your account. Each time a DocumentCollection
 *         is created the account will be billed for 1 hour of usage based on
 *         the performance tier of that account.
 * 
 * 
 */

public class CollectionManagement {

	private static final String DATABASE_NAME = "collectionsamples";
	private static final String DB_URL = "dbs/" + DATABASE_NAME;
	private static final String COL_PREFIX = DB_URL + "/colls/";
	private static DocumentClient client;
	private static final Set<String> CREATED = new HashSet<>(); // track
																// collections
																// to clean up

	public static void main(String[] args) {
		try {
			client = ConnectUtil.getClient();
			initializeDatabase();
			runSimpleCollection();
			runPartitionedCollection();
			runLazyIndexedCollection();
			runSetDefaultTimeToLiveCollection();
			runChangeRu();
			runListCollectionPropertiesAndOffers();
			deleteCollections();
			client.deleteDatabase(DB_URL, null);
			System.out.println("Collections management demo done");
		} catch (DocumentClientException e) {
			printDocumentClientException(e);
		} finally {
			if (client != null) {
				client.close();
			}
		}

	}

	private static void runSimpleCollection() throws DocumentClientException {
		String collectionId = "simpleCollection";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions options = new RequestOptions();
		collection.setId(collectionId);
		options.setOfferThroughput(400);
		createIfNotexists(collection, options);
		CREATED.add(collectionId);

	}

	private static void runPartitionedCollection() throws DocumentClientException {
		String collectionId = "partitionedCollection";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions options = new RequestOptions();
		collection.setId(collectionId);
		PartitionKeyDefinition pkdef = new PartitionKeyDefinition();
		List<String> paths = new ArrayList<>();
		paths.add("/deviceId");
		pkdef.setPaths(paths);
		collection.setPartitionKey(pkdef);
		options.setOfferThroughput(10100);
		createIfNotexists(collection, options);
		CREATED.add(collectionId);
	}

	private static void runLazyIndexedCollection() throws DocumentClientException {
		String collectionId = "lazyIndexedCollection";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions options = new RequestOptions();
		collection.setId(collectionId);
		IndexingPolicy policy = new IndexingPolicy();
		policy.setIndexingMode(IndexingMode.Lazy);
		collection.setIndexingPolicy(policy);
		options.setOfferThroughput(400);
		createIfNotexists(collection, options);
		CREATED.add(collectionId);
	}

	private static void runSetDefaultTimeToLiveCollection() throws DocumentClientException {
		String collectionId = "defaultTimeToLiveCollection";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions options = new RequestOptions();
		collection.setId(collectionId);
		collection.setDefaultTimeToLive(60 * 60 * 24); //expire after one day
		options.setOfferThroughput(400);
		createIfNotexists(collection, options);
		CREATED.add(collectionId);
	}	
	
	private static void runChangeRu() throws DocumentClientException{
		FeedOptions fo = new FeedOptions();
		fo.setPageSize(1);
		System.out.println("ChangingRU in simpleCollection from 400 to 500");
		DocumentCollection col =  client.readCollection(COL_PREFIX+"simpleCollection",null).getResource();
		Offer offer  = client.queryOffers("SELECT * FROM Offers where Offers[\"resource\"] = \""+col.getSelfLink()+"\"",fo).getQueryIterable().toList().get(0);
		OfferV2 ov2 = new OfferV2(offer);
		ov2.setOfferThroughput(500);
		client.replaceOffer(ov2);
	}
	
	private static void runListCollectionPropertiesAndOffers(){
		FeedOptions fo = new FeedOptions();
		fo.setPageSize(1);
		List<DocumentCollection> collections = client.readCollections(DB_URL,fo).getQueryIterable().toList();
		System.out.println("Listing colleciotn propeties and associated offer for collections");
		for(DocumentCollection dc: collections){
			System.out.println("collection properities for "+dc.getId()+"-->"+dc);
			Offer offer  = client.queryOffers("SELECT * FROM Offers where Offers[\"resource\"] = \""+dc.getSelfLink()+"\"",fo).getQueryIterable().toList().get(0);
			System.out.println("offer properities for "+dc.getId()+"-->"+offer);
		}
	}
	
	private static void deleteCollections() throws DocumentClientException {
		for (String collectionId : CREATED) {
			System.out.println("Deleting collection " + collectionId);
			client.deleteCollection(COL_PREFIX + collectionId, null);
		}
	}

	private static void printDocumentClientException(DocumentClientException dce) {
		System.err.println(dce.getStatusCode() + ":" + dce.getMessage());
	}

	private static void createIfNotexists(DocumentCollection collection, RequestOptions requestOptions)
			throws DocumentClientException {
		FeedOptions options = new FeedOptions();
		options.setPageSize(100);
		List<DocumentCollection> collections = client.readCollections(DB_URL, options).getQueryIterable().toList();
		if (collections.isEmpty()) {
			System.out.println(collection.getId() + " collection not found creating...");
			client.createCollection(DB_URL, collection, requestOptions);
		} else {
			boolean create = true;
			for (DocumentCollection dc : collections) {
				if (dc.getId().equals(collection.getId())) {
					create = false;
					break;
				}
			}
			if (create) {
				System.out.println(collection.getId() + " collection not found creating...");
				client.createCollection(DB_URL, collection, requestOptions);
			}
		}

	}

	private static void initializeDatabase() throws DocumentClientException {
		FeedOptions options = new FeedOptions();
		options.setPageSize(100);
		List<Database> dbs = client.readDatabases(options).getQueryIterable().toList();
		Database db = new Database();
		db.setId(DATABASE_NAME);
		if (dbs.isEmpty()) {
			System.out.println(DATABASE_NAME + " database not found creating...");
			client.createDatabase(db, null);

		} else {
			boolean create = true;
			for (Database candidate : dbs) {
				if (candidate.getId().equals(DATABASE_NAME)) {
					create = false;
					break;
				}
			}
			if (create) {
				System.out.println(DATABASE_NAME + " database not found creating...");
				client.createDatabase(db, null);
			}

		}
	}

}
