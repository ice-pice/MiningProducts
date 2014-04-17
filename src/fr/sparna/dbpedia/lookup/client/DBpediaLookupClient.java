package fr.sparna.dbpedia.lookup.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.sparna.dbpedia.lookup.client.schema.ArrayOfResult;
import fr.sparna.dbpedia.lookup.client.schema.Class;
import fr.sparna.dbpedia.lookup.client.schema.Classes;
import fr.sparna.dbpedia.lookup.client.schema.Result;


/**
 * Copyright Mark Watson 2008-2010. All Rights Reserved.
 * License: LGPL version 3 (http://www.gnu.org/licenses/lgpl-3.0.txt)
 */

// Use Georgi Kobilarov's DBpedia lookup web service
//    ref: http://lookup.dbpedia.org/api/search.asmx?op=KeywordSearch
//    example: http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=Flagstaff&QueryClass=XML&MaxHits=10
public class DBpediaLookupClient {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static final String DBPEDIA_LOOKUP_SERVICE = "http://lookup.dbpedia.org/api/search";
	
	private static final String API_KEYWORDSEARCH = "KeywordSearch";
	
	private static final String API_PREFIXSEARCH = "PrefixSearch";
	
	private static final String PARAM_QUERYSTRING = "QueryString";
	
	private static final String PARAM_QUERYCLASS = "QueryClass";
	
	private static final String PARAM_MAXHITS = "MaxHits";
	
	private Integer defaultMaxHits;
	
	private DBpediaLookupResultParser resultParser;
	
	/**
	 * Default constructor
	 */
	public DBpediaLookupClient() {
		defaultMaxHits = 5;
		// resultParser = new SAXSimpleResultParser();
		resultParser = new JAXBResultParser();
	}
	
	public ArrayOfResult keywordSearch(String queryString, String queryClass, Integer maxHits)
	throws DBpediaLookupException {
		return performSearch(
			queryString,
			queryClass,
			maxHits,
			API_KEYWORDSEARCH
		);
	}
	
	public ArrayOfResult keywordSearch(String queryString, String queryClass)
			throws DBpediaLookupException {
		return this.keywordSearch(queryString, queryClass, null);
	}
	
	public ArrayOfResult keywordSearch(String queryString)
	throws DBpediaLookupException {
		return this.keywordSearch(queryString, null, null);
	}
	
	public ArrayOfResult prefixSearch(String queryString, String queryClass, Integer maxHits)
	throws DBpediaLookupException {
		return performSearch(
			queryString,
			queryClass,
			maxHits,
			API_PREFIXSEARCH
		);
	}
	
	public ArrayOfResult prefixSearch(String queryString, String queryClass)
			throws DBpediaLookupException {
		return this.prefixSearch(queryString, queryClass, null);
	}
	
	public ArrayOfResult prefixSearch(String queryString)
	throws DBpediaLookupException {
		return this.prefixSearch(queryString, null, null);
	}
	
	private ArrayOfResult performSearch(
			String queryString,
			String queryClass,
			Integer maxHits, 
			final String searchType
	) throws DBpediaLookupException {		
		
		if(queryString == null || "".equals(queryString)) {
			throw new DBpediaLookupException("null or empty queryString parameter");
		}
		
		StringBuffer url;
		try {
			url = new StringBuffer(DBPEDIA_LOOKUP_SERVICE+"/"+searchType+"?"+PARAM_QUERYSTRING+"="+URLEncoder.encode(queryString, "ISO-8859-1"));
			if(queryClass != null && !queryClass.equals("")) {
				url.append("&"+PARAM_QUERYCLASS+"="+URLEncoder.encode(queryClass, "ISO-8859-1"));
			}
		} catch (UnsupportedEncodingException e1) {
			throw new DBpediaLookupException(e1);
		}
		
		if(maxHits != null) {
			url.append("&"+PARAM_MAXHITS+"="+maxHits);
		} else if(this.defaultMaxHits != null){
			url.append("&"+PARAM_MAXHITS+"="+this.defaultMaxHits);
		}
		
		log.info("Calling lookup service with URL "+url.toString());
		
		try {
			InputStream responseStream = Request.Get(url.toString()).execute().returnContent().asStream();
			return this.resultParser.parse(responseStream);
		} catch (ClientProtocolException e) {
			throw new DBpediaLookupException(e);
		} catch (IOException e) {
			throw new DBpediaLookupException(e);
		}
	}
	
	public Integer getDefaultMaxHits() {
		return defaultMaxHits;
	}

	public void setDefaultMaxHits(Integer defaultMaxHits) {
		this.defaultMaxHits = defaultMaxHits;
	}

	public static void main(String... args) throws Exception {
		// String searchKey = "Achieve Inc (USA)";
		String searchKey = "Hyundai";
		if(args.length > 0) {
			searchKey = args[0];
		}
		
		DBpediaLookupClient client = new DBpediaLookupClient();
		DBpediaLookupClient.getResultSets("Hyundai");
	}

	public static Map<String, Set<String>> getResultSets(String searchKey) throws DBpediaLookupException {
		
		Map<String, Set<String>> resultSet = new LinkedHashMap<>();
		DBpediaLookupClient client = new DBpediaLookupClient();
		ArrayOfResult result = client.keywordSearch(searchKey);
		List<Result> op = result.getResult();
		for (Result results : op) {
//			System.out.println("result : "+results.getLabel());
			Classes classes = results.getClasses();
			List<Class> clazz = classes.getClazz();
			Set<String> classSet = new TreeSet<>();
			for (Class clas : clazz) {
				classSet.add(clas.getLabel());
			}
			resultSet.put(results.getLabel(), classSet);
		}
		return resultSet;
	}
	
}