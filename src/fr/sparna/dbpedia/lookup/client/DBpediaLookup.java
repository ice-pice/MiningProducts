package fr.sparna.dbpedia.lookup.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBpediaLookup {

	/**
	 * @param args
	 */
	public static List<String> lookUp(String args) {
		List<String> x = null;
		try {
			Map<String, Set<String>> dbpediaResults = DBpediaLookupClient
					.getResultSets(args);
			Set<String> keySet = dbpediaResults.keySet();
			for (String keys : keySet) {
				Set<String> dbpediaSet = dbpediaResults.get(keys);
				x = new ArrayList<String>(dbpediaSet);
				Collections.sort(x);

			}
			// System.out.println(isCompany);
		} catch (DBpediaLookupException e) {
			e.printStackTrace();
		}

		return x;
	}

	public static void main(String[] args) {
		System.out.println(DBpediaLookup.lookUp("Kia Rio"));
	}

}