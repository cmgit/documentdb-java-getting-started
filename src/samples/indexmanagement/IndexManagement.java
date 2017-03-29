package samples.indexmanagement;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DataType;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.ExcludedPath;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.IncludedPath;
import com.microsoft.azure.documentdb.Index;
import com.microsoft.azure.documentdb.IndexingDirective;
import com.microsoft.azure.documentdb.IndexingMode;
import com.microsoft.azure.documentdb.IndexingPolicy;
import com.microsoft.azure.documentdb.RangeIndex;
import com.microsoft.azure.documentdb.RequestOptions;

import samples.utils.ConnectUtil;

/**
 *
 * Sample which demonstrates indexing. Based on
 * https://github.com/Azure/azure-documentdb-dotnet/tree/master/samples/code-samples/IndexManagement
 * 
 * DocumentDB will, by default, create a HASH index for every numeric and string
 * field This default index policy is best for; - Equality queries against
 * strings - Range & equality queries on numbers - Geospatial indexing on all
 * GeoJson types.
 *
 * Demonstrates how to customize and alter the index policy on a
 * DocumentCollection by: 1. Exclude a document completely from the Index 2. Use
 * manual (instead of automatic) indexing 3. Use lazy (instead of consistent)
 * indexing 4. Exclude specified paths from document index 5. Force a range scan
 * operation on a hash indexed path 6. Perform index transform
 * ----------------------------------------------------------------------------------------------------------
 * Note -
 * 
 * Running this sample will create (and delete) multiple DocumentCollection
 * resources on your account. Each time a DocumentCollection is created the
 * account will be billed for 1 hour of usage based on the performance tier of
 * that account.
 * 
 * @author cmgit
 */
public class IndexManagement {

	private static final String DATABASE_NAME = "indexsamples";
	private static final String DB_URL = "dbs/" + DATABASE_NAME;
	private static final String COL_PREFIX = DB_URL + "/colls/";
	private static DocumentClient client;

	public static void main(String[] args) {
		try {
			ConnectionPolicy policy = new ConnectionPolicy();
			policy.setUserAgentSuffix(" samples-net/3");
			ConsistencyLevel level = ConsistencyLevel.Session;
			client = ConnectUtil.getClient(policy, level);
			initializeDatabase();
			explicitlyExcludeFromIndex();
			useManualIndexing();
			useLazyIndexing();
			excludePathsFromIndex();
			rangeScanOnHashIndex();
			useRangeIndexesOnString();
			performIndexTransformations();
			client.deleteDatabase(DB_URL, null);
			System.out.println("Index management demo done");
		} catch (DocumentClientException dce) {
			System.err.println(dce.getStatusCode() + ":" + dce.getMessage());
		} finally {
			if (client != null) {
				client.close();
			}
		}
	}

	/**
	 * The default index policy on a DocumentCollection will AUTOMATICALLY index
	 * ALL documents added. There may be scenarios where you want to exclude a
	 * specific doc from the index even though all other documents are being
	 * indexed automatically. This method demonstrates how to use an index
	 * directive to control this
	 * 
	 * @throws DocumentClientException
	 */
	private static void explicitlyExcludeFromIndex() throws DocumentClientException {
		String collectionId = "explicitexclude";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions requestOptions = new RequestOptions();
		collection.setId(collectionId);
		DocumentCollection createdCollection = createIfNotexists(collection, requestOptions);
		System.out.println("Created collection " + createdCollection.getId() + " with indexing policy "
				+ createdCollection.getIndexingPolicy());
		// Create a document then query. It will work because by default
		// everything is indexed
		JSONObject jobj = new JSONObject();
		jobj.put("id", "doc1");
		jobj.put("orderId", "order1");
		Document doc = new Document(jobj.toString());
		Document createdDoc = client.createDocument(COL_PREFIX + collectionId, doc, null, false).getResource();
		System.out.println("Created document with default indexing:" + createdDoc);
		Document queriedDoc = client.queryDocuments(COL_PREFIX + collectionId,
				"SELECT * FROM root r WHERE r.orderId='order1'", new FeedOptions()).getQueryIterable().toList().get(0);
		System.out.println("Queried document with default indexing:" + queriedDoc);
		// now create a second doc but exclude it from indexing
		JSONObject jobjx = new JSONObject();
		jobjx.put("id", "doc2");
		jobjx.put("orderId", "order2");
		Document docx = new Document(jobjx.toString());
		RequestOptions requestOptionsx = new RequestOptions();
		requestOptionsx.setIndexingDirective(IndexingDirective.Exclude);
		Document createdDocx = client.createDocument(COL_PREFIX + collectionId, docx, requestOptionsx, false)
				.getResource();
		System.out.println("Created document with exluded indexing:" + createdDocx);
		// now query with the expectation the document is NOT found
		int cnt = client.queryDocuments(COL_PREFIX + collectionId, "SELECT * FROM root r WHERE r.orderId='order2'",
				new FeedOptions()).getQueryIterable().toList().size();
		System.out.println("Queried document count with exluded indexing [should be 0]:" + cnt);
		// for non indexed can use selfLink
		Document bylink = client.readDocument(createdDocx.getSelfLink(), null).getResource();
		System.out.println("Excluded doc retrieved by selfLink:" + bylink);
	}

	/**
	 * The default index policy on a DocumentCollection will AUTOMATICALLY index
	 * ALL documents added. There may be cases where you can want to turn-off
	 * automatic indexing and only selectively add only specific documents to
	 * the index. This method demonstrates how to control this by setting the
	 * value of IndexingPolicy
	 * 
	 * @throws DocumentClientException
	 */
	private static void useManualIndexing() throws DocumentClientException {
		String collectionId = "autodisabled";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions requestOptions = new RequestOptions();
		collection.setId(collectionId);
		IndexingPolicy policy = new IndexingPolicy();
		// disable automatic
		policy.setAutomatic(false);
		collection.setIndexingPolicy(policy);
		DocumentCollection createdCollection = createIfNotexists(collection, requestOptions);
		System.out.println("Created collection " + createdCollection.getId() + " with indexing policy "
				+ createdCollection.getIndexingPolicy());
		JSONObject jobj = new JSONObject();
		jobj.put("id", "doc1");
		jobj.put("orderId", "order1");
		Document doc = new Document(jobj.toString());
		client.createDocument(COL_PREFIX + collectionId, doc, null, false);
		// Document should not be able to be found by query since auto indexing
		// is off.
		int cnt = client.queryDocuments(COL_PREFIX + collectionId, "SELECT * FROM root r WHERE r.orderId='order1'",
				new FeedOptions()).getQueryIterable().toList().size();
		System.out.println("Queried document count with automatic indexing turned off [should be 0]:" + cnt);
		// Now create document and explicitly include it in indexing
		JSONObject jobjIndexed = new JSONObject();
		jobjIndexed.put("id", "doc2");
		jobjIndexed.put("orderId", "order2");
		Document docIndexed = new Document(jobjIndexed.toString());
		RequestOptions requestOptionsInclude = new RequestOptions();
		requestOptionsInclude.setIndexingDirective(IndexingDirective.Include);
		client.createDocument(COL_PREFIX + collectionId, docIndexed, requestOptionsInclude, false);
		Document queriedDoc = client.queryDocuments(COL_PREFIX + collectionId,
				"SELECT * FROM root r WHERE r.orderId='order2'", new FeedOptions()).getQueryIterable().toList().get(0);
		System.out.println("Queried document with explicit indexing:" + queriedDoc);
	}

	/**
	 * DocumentDB offers synchronous (consistent) and asynchronous (lazy) index
	 * updates. By default, the index is updated synchronously on each insert,
	 * replace or delete of a document to the collection. There are times when
	 * you might want to configure certain collections to update their index
	 * asynchronously. Lazy indexing boosts write performance and is ideal for
	 * bulk ingestion scenarios for primarily read-heavy collections It is
	 * important to note that you might get inconsistent reads whilst the writes
	 * are in progress, However once the write volume tapers off and the index
	 * catches up, then reads continue as normal
	 * 
	 * This method demonstrates how to switch IndexMode to Lazy
	 * 
	 * @throws DocumentClientException
	 */
	private static void useLazyIndexing() throws DocumentClientException {
		String collectionId = "lazyindexed";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions requestOptions = new RequestOptions();
		collection.setId(collectionId);
		IndexingPolicy policy = new IndexingPolicy();
		policy.setIndexingMode(IndexingMode.Lazy);
		collection.setIndexingPolicy(policy);
		DocumentCollection createdCollection = createIfNotexists(collection, requestOptions);
		System.out.println("Created lazily indexed collection " + createdCollection);
	}

	/**
	 * The default behavior is for DocumentDB to index every attribute in every
	 * document automatically. There are times when a document contains large
	 * amounts of information, in deeply nested structures that you know you
	 * will never search on. In extreme cases like this, you can exclude paths
	 * from the index to save on storage cost, improve write performance and
	 * also improve read performance because the index is smaller
	 *
	 * This method demonstrates how to set IndexingPolicy.ExcludedPaths
	 * 
	 * @throws DocumentClientException
	 */
	private static void excludePathsFromIndex() throws DocumentClientException {

		// create a json document with several levels of nesting
		JSONObject jobj = new JSONObject();
		jobj.put("id", "doc1");
		jobj.put("foo", "bar");
		jobj.put("metaData", "meta");
		JSONObject subDoc = new JSONObject();
		subDoc.put("searchable", "searchable");
		subDoc.put("nonSearchable", "value");
		jobj.put("subDoc", subDoc);
		JSONObject excludedNode = new JSONObject();
		excludedNode.put("subExcluded", "something");
		JSONObject excludedNodeSubDoc = new JSONObject();
		excludedNodeSubDoc.put("someproperty", "value");
		excludedNode.put("subExcludeNode", excludedNodeSubDoc);
		jobj.put("excludeNode", excludedNode);
		System.out.println(jobj.toString(1));
		Document nested = new Document(jobj.toString());

		// setup paths and add them to a policy
		List<IncludedPath> includedPaths = new ArrayList<>();
		IncludedPath includePath = new IncludedPath();
		includePath.setPath("/*");
		includedPaths.add(includePath);// Special mandatory path of "/*"
										// required to denote include entire
										// tree

		List<ExcludedPath> excludedPaths = new ArrayList<>();
		ExcludedPath excludePath1 = new ExcludedPath();
		ExcludedPath excludePath2 = new ExcludedPath();
		ExcludedPath excludePath3 = new ExcludedPath();
		excludePath1.setPath("/metaData/*"); // exclude metaData node, and
												// anything under it
		excludePath2.setPath("/subDoc/nonSearchable/*"); // exclude ONLY a part
															// of subDoc
		excludePath3.setPath("/\"excludedNode\"/*"); // exclude excludedNode
														// node, and anything
														// under it
		excludedPaths.add(excludePath1);
		excludedPaths.add(excludePath2);
		excludedPaths.add(excludePath3);

		IndexingPolicy policy = new IndexingPolicy();
		policy.setIncludedPaths(includedPaths);
		policy.setExcludedPaths(excludedPaths);

		// create collection with the policy
		String collectionId = "excluded";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions requestOptions = new RequestOptions();
		collection.setId(collectionId);
		collection.setIndexingPolicy(policy);
		createIfNotexists(collection, requestOptions);
		client.createDocument(COL_PREFIX + collectionId, nested, null, false);

		// Querying for a document on either metaData or
		// /subDoc/subSubDoc/someProperty should fail because they were excluded
		String badQuery1 = "SELECT * FROM root r WHERE r.metaData='meta'";
		List<Document> results = wrapQuery(collectionId, badQuery1);
		System.out.println(badQuery1 + " should NOT work. Results are :" + results);
		String badQuery2 = "SELECT * FROM root r WHERE r.subDoc.nonSearchable='value'";
		results = wrapQuery(collectionId, badQuery2);
		System.out.println(badQuery2 + " should NOT work. Results are :" + results);
		String badQuery3 = "SELECT * FROM root r WHERE r.subDoc.nonSearchable='value'";
		results = wrapQuery(collectionId, badQuery3);
		System.out.println(badQuery3 + " should NOT work. Results are :" + results);
		// Querying for a document using foo , or even subDoc/searchable should
		// succeed because they were not excluded
		String goodQuery1 = "SELECT * FROM root r WHERE r.foo='bar'";
		results = client.queryDocuments(COL_PREFIX + collectionId, goodQuery1, new FeedOptions()).getQueryIterable()
				.toList();
		System.out.println(goodQuery1 + " should work. Results are :" + results);
		String goodQuery2 = "SELECT * FROM root r WHERE r.subDoc.searchable='searchable'";
		results = client.queryDocuments(COL_PREFIX + collectionId, goodQuery2, new FeedOptions()).getQueryIterable()
				.toList();
		System.out.println(goodQuery2 + " should work. Results are :" + results);

	}

	/**
	 * When a range index is not available (i.e. Only hash or no index found on
	 * the path), comparisons queries can still can still be performed as scans
	 * using enableScanInQuery request option.
	 *
	 * This method demonstrates how to force a scan when only hash indexes exist
	 * on the path
	 */
	private static void rangeScanOnHashIndex() throws DocumentClientException {
		String collectionId = "rangeindexscan";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions requestOptions = new RequestOptions();
		collection.setId(collectionId);
		IncludedPath ipath = new IncludedPath();
		ipath.setPath("/");
		ExcludedPath epath = new ExcludedPath();
		epath.setPath("/length/*");
		List<IncludedPath> included = new ArrayList<>();
		included.add(ipath);
		List<ExcludedPath> excluded = new ArrayList<>();
		excluded.add(epath);
		IndexingPolicy policy = new IndexingPolicy();
		policy.setIncludedPaths(included);
		policy.setExcludedPaths(excluded);
		collection.setIndexingPolicy(policy);
		createIfNotexists(collection, requestOptions);
		JSONObject doc1 = new JSONObject();
		JSONObject doc2 = new JSONObject();
		JSONObject doc3 = new JSONObject();
		doc1.put("id", "dyn1");
		doc1.put("length", 10);
		doc1.put("width", 5);
		doc1.put("height", 15);
		doc2.put("id", "dyn2");
		doc2.put("length", 7);
		doc2.put("width", 15);
		doc3.put("id", "dyn3");
		doc3.put("length", 2);

		client.createDocument(COL_PREFIX + collectionId, new Document(doc1.toString()), null, false);
		client.createDocument(COL_PREFIX + collectionId, new Document(doc2.toString()), null, false);
		client.createDocument(COL_PREFIX + collectionId, new Document(doc3.toString()), null, false);
		// Query for length > 5 should fail because this is a range based query
		// on a Hash index only document
		String rangeQuery = "SELECT * FROM root r WHERE r.length > 5";
		List<Document> results = wrapQuery(collectionId, rangeQuery);
		System.out.println(rangeQuery + " should NOT work. Results are :" + results);

		// Now the same query should work because we enable scan
		FeedOptions feedOptions = new FeedOptions();
		feedOptions.setEnableScanInQuery(true);
		results = client.queryDocuments(COL_PREFIX + collectionId, rangeQuery, feedOptions).getQueryIterable().toList();
		System.out.println(rangeQuery + " should work because scan enabled. Results are :" + results);
	}

	private static void useRangeIndexesOnString() throws DocumentClientException {
		String collectionId = "rangeindexonstring";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions requestOptions = new RequestOptions();
		collection.setId(collectionId);
		// For demo purposes, we are going to use the default (range on numbers,
		// hash on strings) for the whole document (/* )
		// and just include a range index on strings for the "region".. You can
		// use the same range index at top level path to make all properties
		// searchable by range
		IncludedPath ipath1 = new IncludedPath();
		ipath1.setPath("/*");
		IncludedPath ipath2 = new IncludedPath();
		ipath2.setPath("/region/?");
		List<Index> indexes = new ArrayList<>();
		RangeIndex rdx = new RangeIndex(DataType.String, -1);
		indexes.add(rdx);
		ipath2.setIndexes(indexes);
		List<IncludedPath> included = new ArrayList<>();
		included.add(ipath1);
		included.add(ipath2);
		IndexingPolicy policy = new IndexingPolicy();
		policy.setIncludedPaths(included);
		collection.setIndexingPolicy(policy);
		createIfNotexists(collection, requestOptions);
		JSONObject doc1 = new JSONObject();
		JSONObject doc2 = new JSONObject();
		JSONObject doc3 = new JSONObject();
		JSONObject doc4 = new JSONObject();
		doc1.put("id", "doc1");
		doc1.put("region", "US");
		doc2.put("id", "doc2");
		doc2.put("region", "UK");
		doc3.put("id", "doc3");
		doc3.put("region", "Armenia");
		doc4.put("id", "doc4");
		doc4.put("region", "Egypt");
		client.createDocument(COL_PREFIX + collectionId, new Document(doc1.toString()), null, false);
		client.createDocument(COL_PREFIX + collectionId, new Document(doc2.toString()), null, false);
		client.createDocument(COL_PREFIX + collectionId, new Document(doc3.toString()), null, false);
		client.createDocument(COL_PREFIX + collectionId, new Document(doc4.toString()), null, false);

		String reqionOrderByQuery = "SELECT * FROM orders o ORDER BY o.region";
		List<Document> results = client.queryDocuments(COL_PREFIX + collectionId, reqionOrderByQuery, null)
				.getQueryIterable().toList();
		System.out.println("Documents ordered by region");
		for (Document doc : results) {
			System.out.println("\t" + doc);
		}

		// You can also perform filters against string comparisons like >= 'UK'.
		// Note that you can perform a prefix query,
		// the equivalent of LIKE 'U%' (is >= 'U' AND < 'U')
		String reqionGreaterThanUQuery = "SELECT * FROM orders o WHERE o.region >= 'U'";
		results = client.queryDocuments(COL_PREFIX + collectionId, reqionGreaterThanUQuery, null).getQueryIterable()
				.toList();
		System.out.println("Documents with region beginning with U");
		for (Document doc : results) {
			System.out.println("\t" + doc);
		}
	}

	private static void performIndexTransformations() throws DocumentClientException {
		String collectionId = "indextransforms";
		DocumentCollection collection = new DocumentCollection();
		RequestOptions requestOptions = new RequestOptions();
		collection.setId(collectionId);
		// Create a collection with default indexing policy
		DocumentCollection collectionBack = createIfNotexists(collection, requestOptions);
		// insert some documents
		JSONObject doc1 = new JSONObject();
		JSONObject doc2 = new JSONObject();
		JSONObject doc3 = new JSONObject();
		doc1.put("id", "dyn1");
		doc1.put("length", 10);
		doc1.put("width", 5);
		doc1.put("height", 15);
		doc2.put("id", "dyn2");
		doc2.put("length", 7);
		doc2.put("width", 15);
		doc3.put("id", "dyn3");
		doc3.put("length", 2);
		client.createDocument(COL_PREFIX + collectionId, new Document(doc1.toString()), null, false);
		client.createDocument(COL_PREFIX + collectionId, new Document(doc2.toString()), null, false);
		client.createDocument(COL_PREFIX + collectionId, new Document(doc3.toString()), null, false);
		// now switch to lazy indexing
		System.out.println("Switching collection " + collectionId + " to lazy indexing");
		IndexingPolicy policy1 = new IndexingPolicy();
		policy1.setIndexingMode(IndexingMode.Lazy);
		collectionBack.setIndexingPolicy(policy1);
		//These changes may take some time so we use a wrapper to call replaceCollection
		collectionBack = executeWithRetry(requestOptions, collectionBack);
		System.out.println("\tCollection after change:" + collectionBack);
		// Switch to use string & number range indexing with maximum precision.
		List<Index> indexes = new ArrayList<>();
		RangeIndex rdx = new RangeIndex(DataType.String, -1);
		indexes.add(rdx);
		IndexingPolicy policy2 = new IndexingPolicy();
		policy2.setIndexingMode(IndexingMode.Consistent);
		System.out.println("Switching collection " + collectionId
				+ " to consistent indexing with range index with maximum precision from root");
		IncludedPath path = new IncludedPath();
		path.setPath("/*");
		path.setIndexes(indexes);
		List<IncludedPath> includePaths = new ArrayList<>();
		includePaths.add(path);
		policy2.setIncludedPaths(includePaths);
		collectionBack.setIndexingPolicy(policy2);
		collectionBack = executeWithRetry(requestOptions, collectionBack);
		System.out.println("\tCollection after change:" + collectionBack);
		// Now exclude a path from indexing to save on storage space.
		List<ExcludedPath> excludedPaths = new ArrayList<>();
		ExcludedPath expath = new ExcludedPath();
		expath.setPath("/length/*");
		excludedPaths.add(expath);
		collectionBack.getIndexingPolicy().setExcludedPaths(excludedPaths);
		System.out.println("Switching collection " + collectionId + " to exclude length to save space");
		collectionBack = executeWithRetry(requestOptions, collectionBack);
		System.out.println("\tCollection after change:" + collectionBack);
	}

	private static DocumentCollection executeWithRetry(RequestOptions requestOptions, DocumentCollection collectionBack)
			throws DocumentClientException {
		while (true) {
			try {
				return client.replaceCollection(collectionBack, requestOptions).getResource();
			} catch (DocumentClientException e) {
				try {
					long retry = 15 * 1000;
					if (e.getStatusCode() == 409) {
						long retrySuggestion = e.getRetryAfterInMilliseconds();
						if (retrySuggestion > 0) {
							retry = retrySuggestion;
						}
						Thread.sleep(retry);
					} else {
						throw e;
					}
				} catch (InterruptedException ignore) {
					ignore.printStackTrace();
					break;
				}
			}
		}
		return null;
	}

	private static List<Document> wrapQuery(String collectionId, String badQuery1) {
		List<Document> results = new ArrayList<>();
		try {
			return client.queryDocuments(COL_PREFIX + collectionId, badQuery1, new FeedOptions()).getQueryIterable()
					.toList();
		} catch (Exception e) {
			if (e.getCause() instanceof DocumentClientException) {
				DocumentClientException dce = (DocumentClientException) e.getCause();
				if (dce.getStatusCode() != 400) { // bad request is what happens
													// when you try to query
													// excluded index
					throw e;
				} else {
					return results;
				}
			}
			throw e;
		}
	}

	private static DocumentCollection createIfNotexists(DocumentCollection collection, RequestOptions requestOptions)
			throws DocumentClientException {
		DocumentCollection retVal = null;
		FeedOptions options = new FeedOptions();
		options.setPageSize(100);
		List<DocumentCollection> collections = client.readCollections(DB_URL, options).getQueryIterable().toList();
		if (collections.isEmpty()) {
			System.out.println(collection.getId() + " collection not found creating...");
			retVal = client.createCollection(DB_URL, collection, requestOptions).getResource();
		} else {
			boolean create = true;
			for (DocumentCollection dc : collections) {
				if (dc.getId().equals(collection.getId())) {
					create = false;
					retVal = dc;
					break;
				}
			}
			if (create) {
				System.out.println(collection.getId() + " collection not found creating...");
				retVal = client.createCollection(DB_URL, collection, requestOptions).getResource();
			}
		}
		return retVal;
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
