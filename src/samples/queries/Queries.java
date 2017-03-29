package samples.queries;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.microsoft.azure.documentdb.DataType;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.Index;
import com.microsoft.azure.documentdb.IndexingPolicy;
import com.microsoft.azure.documentdb.PartitionKeyDefinition;
import com.microsoft.azure.documentdb.RangeIndex;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.documentdb.SqlParameter;
import com.microsoft.azure.documentdb.SqlParameterCollection;
import com.microsoft.azure.documentdb.SqlQuerySpec;

import samples.pojos.Address;
import samples.pojos.Child;
import samples.pojos.Family;
import samples.pojos.Parent;
import samples.pojos.Pet;
import samples.utils.ConnectUtil;

/**
 * This sample shows common query patterns. Loosely based on .NET samples https://github.com/Azure/azure-documentdb-dotnet/tree/master/samples/code-samples/Queries
 * For additional examples using the SQL query grammar refer to the SQL Query Tutorial available 
 * at https://azure.microsoft.com/documentation/articles/documentdb-sql-query/.
 * There is also an interactive Query Demo web application where you can try out different 
 * SQL queries available at https://www.documentdb.com/sql/demo.  
 * @author cmgit
 *
 */

public class Queries {

	private static final String DATABASE_NAME = "querysamples";
	private static final String DB_URL = "dbs/" + DATABASE_NAME;
	private static DocumentClient client;
	private static final String COLLECTION_NAME = "query-samples";
	private static final String COL_URL = DB_URL + "/colls/" + COLLECTION_NAME;
	private static FeedOptions DEFAULT_OPTIONS = new FeedOptions();

	public static void main(String[] args) {

		try {
			DEFAULT_OPTIONS.setEnableCrossPartitionQuery(true);
			client = ConnectUtil.getClient();
			initialize();
			createDemoDocuments();
			queryAllDocuments();
			runEqualityQueries();
			runInequalityQueries();
			runRangeQueries();
			runOrderByQueries();
			runAggregateQueries();
			queryWithSubdocuments();
			runJoinQueries();
			runQueriesWithMathAndStringOperators();
			queryWithSqlQuerySpec();
			System.out.println("Demo done");
		} catch (DocumentClientException e) {
			System.err.println(e.getStatusCode() + ":" + e.getMessage());
		} finally {
			if (client != null) {
				client.close();
			}
		}

	}

	private static void runEqualityQueries() {
		System.out.println("Running inequality queries");
		queryWithEqualsOnId();
		queryWithEqualsAndFilter();
		queryWithEqualsAndFilterAndProjection();
	}

	private static void runInequalityQueries() {
		System.out.println("Running equality queries");
		queryWithInequality();
		queryWithInequalityAlternateOpertor();
		queryWithEqualityAndInequality();
	}

	private static void runRangeQueries() {
		System.out.println("Running range queries");
		queryWithRangeOperatorOnNumber();
		queryWithRangeOperatorOnString();
		queryWithRangeOperatorOnDate();
	}

	private static void runOrderByQueries() {
		System.out.println("Running ORDER BY queries");
		queryWithOrderByAsc();
		queryWithOrderByDesc();
	}

	private static void runAggregateQueries() {
		System.out.println("Running aggregate queries");
		queryWithCountTopLevel();
		queryWithCountArrayWithin();
		queryWithMax();
	}

	private static void runJoinQueries() {
		System.out.println("Running join queries");
		queryWithSingleJoin();
		queryWithTwoJoins();
		queryWithTwoJoinsAndFilter();
	}

	private static void runQueriesWithMathAndStringOperators() {
		System.out.println("Running math and string operator queries");
		queryWithStartsWith();
		queryWithFloor();
		queryWithArrayLength();
	}

	private static void queryAllDocuments() {
		System.out.println("\tQuerying all documents and asserting expected outcome...");
		String query = "SELECT * FROM Families";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 2;
	}

	private static void queryWithEqualsOnId() {
		System.out.println("\tQuerying with equals on id asserting expected outcome...");
		String query = "SELECT * FROM Families f WHERE f.id='AndersenFamily'";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
	}

	private static void queryWithEqualsAndFilter() {
		System.out.println("\tQuerying with AND filter asserting expected outcome...");
		String query = "SELECT * FROM Families f WHERE f.id='AndersenFamily' AND f.address.city='Seattle'";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
	}

	private static void queryWithEqualsAndFilterAndProjection() {
		System.out.println("\tQuerying with filter and projection and printing results...");
		String query = "SELECT f.lastName AS Name, f.address.city AS City FROM Families f WHERE f.id='AndersenFamily' OR f.address.city='NY'";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		for (Document dc : documents) {
			System.out.println("\t\t" + dc);
		}
	}

	private static void queryWithInequality() {
		System.out.println("\tQuerying with != and asserting results...");
		String query = "SELECT * FROM Families f WHERE f.id != 'AndersenFamily'";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
	}

	private static void queryWithInequalityAlternateOpertor() {
		System.out.println("\tQuerying with <> and asserting results...");
		String query = "SELECT * FROM Families f WHERE f.id <> 'AndersenFamily'";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
	}

	private static void queryWithEqualityAndInequality() {
		System.out.println("\tQuerying with = and  != and asserting results...");
		String query = "SELECT * FROM Families f WHERE f.id = 'AndersenFamily' AND f.address.city != 'NY'";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
	}

	private static void queryWithRangeOperatorOnNumber() {
		System.out.println("\tQuerying with range on number and asserting results...");
		String query = "SELECT * FROM Families f WHERE f.children[0].grade > 5";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
	}

	private static void queryWithRangeOperatorOnString() {
		System.out.println("\tQuerying with range on string and asserting results...");
		String query = "SELECT * FROM Families f WHERE f.address.state > 'NY'";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
	}

	private static void queryWithOrderByAsc() {
		System.out.println("\tQuerying with ORDER BY on number ASC (default) and printing results...");
		String query = "SELECT * FROM Families f ORDER BY f.children[0].grade";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		for (Document dc : documents) {
			System.out.println("\t\t" + dc);
		}
	}

	private static void queryWithOrderByDesc() {
		System.out.println("\tQuerying with ORDER BY on string DESC and printing results...");
		String query = "SELECT * FROM Families f ORDER BY f.lastName DESC";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		for (Document dc : documents) {
			System.out.println("\t\t" + dc);
		}
	}

	private static void queryWithRangeOperatorOnDate() {
		System.out.println("\tQuerying with range on date and asserting results...");
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.add(Calendar.DATE, -3);
		String dateArg = toIso861(cal.getTime());
		String query = "SELECT * FROM c WHERE c.registrationDate >= '" + dateArg + "'";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
	}

	private static void queryWithCountTopLevel() {
		System.out.println("\tQuerying with COUNT on top level property and asserting results...");
		String query = "SELECT VALUE COUNT(f) FROM Families f WHERE f.lastName = 'Andersen'";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
		assert documents.get(0).getInt("_aggregate") == 1;
	}

	private static void queryWithCountArrayWithin() {
		System.out.println("\tQuerying with COUNT on Document array elements and asserting results...");
		String query = "SELECT VALUE COUNT(child) FROM child IN f.children";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
		assert documents.get(0).getInt("_aggregate") == 3;
	}

	private static void queryWithMax() {
		System.out.println("\tQuerying with MAX and asserting results...");
		String query = "SELECT VALUE MAX(child.grade) FROM child IN f.children";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
		assert documents.get(0).getInt("_aggregate") == 8;
	}

	private static void queryWithSubdocuments() {
		System.out.println("Querying with subdocument and displaying results...");
		String query = "SELECT VALUE c FROM c IN f.children";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		for (Document dc : documents) {
			System.out.println("\t" + dc);
		}
	}

	private static void queryWithSingleJoin() {
		System.out.println("\tQuerying with single join and displaying results...");
		String query = "SELECT f.id FROM Families f JOIN c IN f.children";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		for (Document dc : documents) {
			System.out.println("\t\t" + dc);
		}
	}

	private static void queryWithTwoJoins() {
		System.out.println("\tQuerying with two joins and displaying results...");
		String query = "SELECT f.id as family, c.firstName AS child, p.givenName AS pet FROM Families f JOIN c IN f.children JOIN p IN c.pets";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		for (Document dc : documents) {
			System.out.println("\t\t" + dc);
		}
	}

	private static void queryWithTwoJoinsAndFilter() {
		System.out.println("\tQuerying with two joins and a filter and displaying results...");
		String query = "SELECT f.id as family, c.firstName AS child, p.givenName AS pet FROM Families f JOIN c IN f.children JOIN p IN c.pets WHERE p.givenName = 'Fluffy'";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		for (Document dc : documents) {
			System.out.println("\t\t" + dc);
		}
	}

	private static void queryWithStartsWith() {
		System.out.println("\tQuerying with STARTSWITH() and asserting results...");
		String query = "SELECT * FROM family WHERE STARTSWITH(family.lastName, 'An')";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1;
	}

	private static void queryWithFloor() {
		System.out.println("\tQuerying with FLOOR() and asserting results...");
		String query = "SELECT VALUE FLOOR(family.children[0].grade) FROM family";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 2 && documents.get(0).getInt("_aggregate") == 5
				&& documents.get(1).getInt("_aggregate") == 8;
	}

	private static void queryWithSqlQuerySpec(){
		System.out.println("Querying with SqlQuerySpec object and asserting results...");
		SqlQuerySpec spec = new SqlQuerySpec("SELECT * FROM Families f WHERE (f.id = @id AND f.address.city = @city)");
		SqlParameterCollection spc = new SqlParameterCollection();
		SqlParameter parameter1 = new SqlParameter("@id","AndersenFamily");
		SqlParameter parameter2 = new SqlParameter("@city","Seattle");
		spc.add(parameter1);
		spc.add(parameter2);
		spec.setParameters(spc);
		List<Document> documents = client.queryDocuments(COL_URL, spec, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 1; 
	}
	
	private static void queryWithArrayLength() {
		System.out.println("\tQuerying with ARRAY_LENGTH() and asserting results...");
		String query = "SELECT VALUE ARRAY_LENGTH(family.children) FROM family";
		List<Document> documents = client.queryDocuments(COL_URL, query, DEFAULT_OPTIONS).getQueryIterable().toList();
		assert documents.size() == 2 && documents.get(0).getInt("_aggregate") == 1
				&& documents.get(1).getInt("_aggregate") == 2;
	}

	private static String toIso861(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date);
	}

	private static void initialize() throws DocumentClientException {
		System.out.println("Creating database " + DATABASE_NAME + " if it doesn't exist");
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
			for (Database candidate : dbs) {
				if (candidate.getId().equals(DATABASE_NAME)) {
					create = false;
					break;
				}
			}
			if (create) {
				System.out.println(DATABASE_NAME + " not found creating...");
				client.createDatabase(db, null);
			}

		}
		DocumentCollection collection = new DocumentCollection();
		collection.setId(COLLECTION_NAME);
		RangeIndex ridx = new RangeIndex(DataType.String);
		ridx.setPrecision(-1);
		IndexingPolicy policy = new IndexingPolicy(new Index[] { ridx });
		collection.setIndexingPolicy(policy);
		List<String> paths = new ArrayList<>();
		paths.add("/lastName");
		PartitionKeyDefinition pkdef = new PartitionKeyDefinition();
		pkdef.setPaths(paths);
		collection.setPartitionKey(pkdef);
		RequestOptions requestOptions = new RequestOptions();
		requestOptions.setOfferThroughput(400);
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

	private static void createDemoDocuments() throws DocumentClientException {
		Family andersenFamily = new Family();
		andersenFamily.setId("AndersenFamily");
		andersenFamily.setLastName("Andersen");
		Parent[] parents = new Parent[2];
		parents[0] = new Parent();
		parents[1] = new Parent();
		parents[0].setFirstName("Thomas");
		parents[1].setFirstName("Mary Kay");
		andersenFamily.setParents(parents);
		Child child = new Child();
		child.setFirstName("Henriette Thaulow");
		child.setGender("female");
		child.setGrade(5);
		Pet pet = new Pet();
		pet.setGivenName("Fluffy");
		child.setPets(new Pet[] { pet });
		andersenFamily.setChildren(new Child[] { child });
		Address address = new Address();
		address.setState("WA");
		address.setCounty("King");
		address.setCity("Seattle");
		andersenFamily.setAddress(address);
		andersenFamily.setRegistered(true);
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.add(Calendar.DATE, -1);
		andersenFamily.setRegistrationDate(toIso861(cal.getTime()));
		client.upsertDocument(COL_URL, andersenFamily, null, false);

		Family wakefieldFamily = new Family();
		wakefieldFamily.setId("WakefieldFamily");
		wakefieldFamily.setLastName("Wakefield");
		parents = new Parent[2];
		parents[0] = new Parent();
		parents[1] = new Parent();
		parents[0].setFirstName("Robin");
		parents[0].setFamilyName("Wakefield");
		parents[1].setFirstName("Ben");
		parents[1].setFamilyName("Miller");
		wakefieldFamily.setParents(parents);
		Child[] children = new Child[2];
		children[0] = new Child();
		children[0].setFirstName("Jesse");
		children[0].setFamilyName("Merriam");
		children[0].setGender("female");
		children[0].setGrade(8);
		Pet[] pets = new Pet[2];
		pets[0] = new Pet();
		pets[0].setGivenName("Goofy");
		pets[1] = new Pet();
		pets[1].setGivenName("Shadow");
		children[0].setPets(pets);
		children[1] = new Child();
		children[1].setFirstName("Lisa");
		children[1].setGender("female");
		children[1].setGrade(8);
		wakefieldFamily.setChildren(children);
		address = new Address();
		address.setState("NY");
		address.setCounty("Manhattan");
		address.setCity("NY");
		wakefieldFamily.setAddress(address);
		wakefieldFamily.setRegistered(false);
		cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.add(Calendar.DATE, -30);
		wakefieldFamily.setRegistrationDate(toIso861(cal.getTime()));
		client.upsertDocument(COL_URL, wakefieldFamily, null, false);

	}

}
