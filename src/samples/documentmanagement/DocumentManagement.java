package samples.documentmanagement;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.microsoft.azure.documentdb.ConnectionMode;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DataType;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.Index;
import com.microsoft.azure.documentdb.IndexingPolicy;
import com.microsoft.azure.documentdb.PartitionKey;
import com.microsoft.azure.documentdb.PartitionKeyDefinition;
import com.microsoft.azure.documentdb.RangeIndex;
import com.microsoft.azure.documentdb.RequestOptions;

import samples.pojos.SalesOrder;
import samples.pojos.SalesOrder2;
import samples.pojos.SalesOrderDetail;
import samples.pojos.SalesOrderDetail2;
import samples.utils.ConnectUtil;

/**
 * @author cmgit
 * Sample meant to show basic document management. 
 * 
 * Loosely based on https://raw.githubusercontent.com/Azure/azure-documentdb-dotnet/master/samples/code-samples/DocumentManagement/Program.cs.
 * 
 * Demonstrates
 * 1) Creating a database if none exists
 * 2) Creating a partitioned collection if none exists and setting throughput
 * 3) Inserting and reading documents using POJO classes
 * 4) Retrieving documents using the readDocuments method
 * 5) Querying for a document using SQL
 * 6) Deleting a document from a partitioned collection
 *
 */

public class DocumentManagement {

	private static final String DATABASE_NAME = "samples";
	private static final String COLLECTION_NAME = "auth-samples";
	private static DocumentClient client;
	private static final String DB_URL = "dbs/" + DATABASE_NAME;
	private static final String COL_URL = DB_URL + "/colls/" + COLLECTION_NAME;

	public static void main(String[] args) {
		ConnectionPolicy connectionPolicy = new ConnectionPolicy();
		List<String> locations = new ArrayList<>();
		locations.add("West US");
		locations.add("North Europe");
		locations.add("Southeast Asia");
		connectionPolicy.setPreferredLocations(locations);
		connectionPolicy.setConnectionMode(ConnectionMode.Gateway);
		client = ConnectUtil.getClient(connectionPolicy, ConsistencyLevel.Session);
		try {
			initialize();
			sendAndRetrieveUsingPojos();
			retrieveAllDocumentsInACollection();
			queryCollectionWithSQLAndConvertToPojo();
			deleteDocument();
			System.out.println("Demo complete");
		} catch (DocumentClientException e) {
			System.err.println(e.getStatusCode() + ":" + e.getMessage());
		}
		finally{
			if(client != null){
				client.close();
			}
		}
		

	}

	
	/**
	 * Creates the sample database and collection if they don't already exists and
	 * sets throughput to 1000 RU
	 * @throws DocumentClientException
	 */
	private static void initialize() throws DocumentClientException {
		System.out.println("Attempting to initialize database");
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
			Database db = new Database();
			db.setId(DATABASE_NAME);
			client.createDatabase(db, null);

		}
		try {
			create = false;
			client.readCollection(COL_URL, null);
		} catch (DocumentClientException e) {
			if (e.getStatusCode() != 404) {
				throw e;
			} else {
				create = true;
			}
		}
		if (create) {
			DocumentCollection collection = new DocumentCollection();
			collection.setId(COLLECTION_NAME);
			RangeIndex ridx = new  RangeIndex(DataType.String);
			ridx.setPrecision(-1);
			IndexingPolicy idxpol = new IndexingPolicy(new Index[]{ridx});
			List<String> paths = new ArrayList<>();
			paths.add("/accountNumber");
			PartitionKeyDefinition pkdef = new PartitionKeyDefinition();
			pkdef.setPaths(paths);
			collection.setIndexingPolicy(idxpol);
			collection.setPartitionKey(pkdef);
			RequestOptions requestOptions = new RequestOptions();
			requestOptions.setOfferThroughput(1000);
			client.createCollection(DB_URL, collection, requestOptions);
		}
	}

	/**
	 *  This method demonstrates adding two versions of a document to a collection.
	 *  It uses POJOS to insert and verifies that the retrieved document matches the original inserted after retrieval.  
	 */
	private static void sendAndRetrieveUsingPojos() {
		System.out.println("Inserting multiple versions of type into the same collection");
		String orderId1 = "SalesOrder1";
		String orderId2 = "SalesOrder2";
		SalesOrder salesOrderV1Inserted = getSalesOrderSample(orderId1);
		SalesOrder2 salesOrderV2Inserted = getSalesOrderV2Sample(orderId2);
		try {
			Document doc1Response = client.upsertDocument(COL_URL, salesOrderV1Inserted, null, false).getResource();
			Document doc2Response = client.upsertDocument(COL_URL, salesOrderV2Inserted, null, false).getResource();
			RequestOptions reqOptions1 = new RequestOptions();
			reqOptions1.setPartitionKey(new PartitionKey("Account1") );
			SalesOrder salesOrderV1Selected = documentToClass(
					client.readDocument(doc1Response.getSelfLink(), reqOptions1).getResource(), SalesOrder.class);
			RequestOptions reqOptions2 = new RequestOptions();
			reqOptions2.setPartitionKey(new PartitionKey("Account2") );
			SalesOrder2 saleseOrderV2Selected = documentToClass(
					client.readDocument(doc2Response.getSelfLink(), reqOptions2).getResource(), SalesOrder2.class);
			assert salesOrderV1Selected.equals(salesOrderV1Inserted);
			assert saleseOrderV2Selected.equals(salesOrderV2Inserted);
		} catch (DocumentClientException e) {
			System.err.println(e.getStatusCode() + ":" + e.getMessage());
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  
	 *  Method which reads documents in a collection using readDocuments.
	 *  Prints out the documents as Document JSON to show the documentdb fields.  
	 */
	private static void retrieveAllDocumentsInACollection()
	{
		System.out.println("Retrieving all documents in a collection via readDocuments");
		FeedOptions fopts = new FeedOptions();
		fopts.setPageSize(10);
		for(Document doc: client.readDocuments(COL_URL, fopts).getQueryIterable()){
			try {
				System.out.println(toJson(doc));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 *  
	 *  Method which retrieves a document in a collection by using SQL.  
	 */
	private static void queryCollectionWithSQLAndConvertToPojo() 
	{
		System.out.println("Using SQL to retrieve a document and printing as POJO to json");
		String sql = "SELECT * FROM c WHERE c.accountNumber = \"Account1\"";
		try {
			SalesOrder sa = documentToClass(client.queryDocuments(COL_URL, sql, null).getQueryIterable().toList().get(0),SalesOrder.class);
			System.out.println(toJson(sa));
			
		}  catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Demonstrates deleting a document from a partitioned collection 
	 */
	private static void deleteDocument(){
		System.out.println("Deleting a document in a partitioned collection with id SalesOrder1");
		String doclink = COL_URL+"/docs/SalesOrder1";
		RequestOptions reqOptions = new RequestOptions();
		reqOptions.setPartitionKey(new PartitionKey("Account1") );
		try {
			client.deleteDocument(doclink, reqOptions);
		} catch (DocumentClientException e) {
			System.err.println(e.getStatusCode() + ":" + e.getMessage());
		}
	}
	
	/**
	 * Helper method which converts from JSON to POJO which ignores the documentdb fields 
	 * @param doc
	 * @param cl
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static <T> T documentToClass(Document doc, Class<?> cl)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return (T) mapper.readValue(doc.toString(), cl);
	}

	/**
	 * Helper method to convert an object to json string which ignores nulls and indents
	 * @param o
	 * @return
	 * @throws JsonProcessingException
	 */
	private static String toJson(Object o) throws JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		return mapper.writeValueAsString(o);
	}
	
	/**
	 * Helper method which generates SalesOrder object
	 * @param documentId
	 * @return
	 */
	private static SalesOrder getSalesOrderSample(String documentId) {
		SalesOrder retVal = new SalesOrder();
		retVal.setId(documentId);
		retVal.setAccountNumber("Account1");
		retVal.setPurchaseOrderNumber("PO18009186470");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2005);
		cal.set(Calendar.MONTH, Calendar.JULY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		retVal.setOrderDate(cal.getTime());
		retVal.setSubTotal(new BigDecimal("419.4589"));
		retVal.setTaxAmount(new BigDecimal("12.5838"));
		retVal.setFreight(new BigDecimal("472.3108"));
		retVal.setTotalDue(new BigDecimal("985.018"));
		SalesOrderDetail detail = new SalesOrderDetail();
		detail.setOrderQty(1);
		detail.setProductId(760);
		detail.setUnitPrice(new BigDecimal("419.4589"));
		detail.setLineTotal(new BigDecimal("419.4589"));
		retVal.setItems(new SalesOrderDetail[] { detail });
		retVal.setTimeToLive(60 * 60 * 24 * 30);
		return retVal;
	}

	/**
	 * Helper method which generates version 2 of a SalesOrder object
	 * @param documentId
	 * @return
	 */
	private static SalesOrder2 getSalesOrderV2Sample(String documentId) {

		SalesOrder2 retVal = new SalesOrder2();
		retVal.setId(documentId);
		retVal.setAccountNumber("Account2");
		retVal.setPurchaseOrderNumber("PO15428132599");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2005);
		cal.set(Calendar.MONTH, Calendar.JULY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		retVal.setOrderDate(cal.getTime());
		cal.set(Calendar.YEAR, 2005);
		cal.set(Calendar.MONTH, Calendar.JULY);
		cal.set(Calendar.DAY_OF_MONTH, 13);
		retVal.setDueDate(cal.getTime());
		cal.set(Calendar.YEAR, 2005);
		cal.set(Calendar.MONTH, Calendar.JULY);
		cal.set(Calendar.DAY_OF_MONTH, 8);
		retVal.setShippedDate(cal.getTime());
		retVal.setSubTotal(new BigDecimal("6107.0820"));
		retVal.setTaxAmt(new BigDecimal("586.1203"));
		retVal.setFreight(new BigDecimal("183.1626"));
		retVal.setDiscountAmt(new BigDecimal("1982.872"));
		retVal.setTotalDue(new BigDecimal("4893.3929"));
		SalesOrderDetail2 detail = new SalesOrderDetail2();
		detail.setOrderQty(3);
		detail.setProductCode("A-123");
		detail.setProductName("Product 1");
		detail.setCurrencySymbol("$");
		detail.setCurrencyCode("USD");
		detail.setUnitPrice(new BigDecimal("17.1"));
		detail.setLineTotal(new BigDecimal("5.7"));
		retVal.setItems(new SalesOrderDetail2[] { detail });
		return retVal;
	}

}
