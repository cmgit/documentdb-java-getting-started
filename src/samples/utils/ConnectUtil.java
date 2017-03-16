package samples.utils;
import java.util.ResourceBundle;

import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.ConsistencyLevel;
import com.microsoft.azure.documentdb.DocumentClient;

public final class ConnectUtil {
	
	private static String CONNECTION_KEY="connection.key";
	private static String CONNECTION_ENDPOINT="connection.endpoint";
	private static String BUNDLE = "resources.configuration";
	private static String key = null;
	private static String endpoint = null;		
	private ConnectUtil()
	{
	
	}
	
	public static DocumentClient getClient()
	{
		initConfigInfo();
		return getClient(ConnectionPolicy.GetDefault(),ConsistencyLevel.Session);
	}

	private static void initConfigInfo() {
		key = ResourceBundle.getBundle(BUNDLE).getString(CONNECTION_KEY);
		endpoint = ResourceBundle.getBundle(BUNDLE).getString(CONNECTION_ENDPOINT);
	}
	
	public static DocumentClient getClient(ConnectionPolicy policy,ConsistencyLevel level)
	{
		initConfigInfo();
		return new DocumentClient(endpoint,key,policy,level);
	}
	
}
