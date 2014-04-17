
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import nu.xom.Serializer;
import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.XMLOutputter;
import edu.stanford.nlp.util.CoreMap;

public class MiningProducts {
	
	private StanfordCoreNLP coreNLP;
	
	public MiningProducts () {
/*        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        coreNLP = new StanfordCoreNLP(props);*/
	}

	public static void main(String[] args) {

		File txtFile = new File("/home/vijay/Documents/IIIT/IRE/MajorPjt/JDPASentimentCorpus/car/batch001/txt/car-001-001.txt");
		File annotatedFile = new File("/home/vijay/Documents/IIIT/IRE/MajorPjt/JDPASentimentCorpus/car/batch001/annotation/car-001-001.txt.knowtator.xml");
		String txtContent = "";
		String annotatedContent = "";
		
		try {
			FeatureExtraction fe = new FeatureExtraction();
			MiningProducts ma = new MiningProducts();
			ma.coreNLP = fe.getCoreNLP();
/*			File folder = new File("/home/vijay/Documents/IIIT/IRE/MajorPjt/JDPASentimentCorpus/car/batch001/txt/");
			for (File testFile : folder.listFiles()) {
				System.out.println("File name : "+testFile.getName());
				MiningProducts ma = new MiningProducts();
				BufferedReader reader=new BufferedReader(new FileReader(testFile));
				StringBuilder text=new StringBuilder();
				String str="";
				while((str=reader.readLine())!=null) {
					text.append(str);
					text.append("\n");
				}
				reader.close();
				ma.parseText(text.toString());
			}*/

			BufferedReader reader=new BufferedReader(new FileReader(txtFile));
			StringBuilder text=new StringBuilder();
			String str="";
			while((str=reader.readLine())!=null) {
				text.append(str);
				text.append("\n");
			}
			txtContent = text.toString();				
			
			text=new StringBuilder();
			str="";
			reader.close();
			reader=new BufferedReader(new FileReader(annotatedFile));
			while((str=reader.readLine())!=null) {
				text.append(str);
				text.append("\n");
			}
			annotatedContent = text.toString();
			reader.close();
			ma.parseText(text.toString());
			fe.readTestFile(txtContent, annotatedContent);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void parseText (String text) throws IOException {
//		String text = "1 package hot dogs, such as Hebrew National Jumbo Beef Franks.";
		
    	List <String> properNouns = new ArrayList<String>();
		Annotation document = new Annotation(text);
        coreNLP.annotate(document);

        nu.xom.Document xmldoc = XMLOutputter.annotationToDoc(document, coreNLP);
	     // below is a tweaked version of XMLOutputter.writeXml()
	     ByteArrayOutputStream sw = new ByteArrayOutputStream();
	     Serializer ser = new Serializer(sw);
	         ser.setIndent(0);
	         ser.setLineSeparator("\n"); // gonna kill this in a moment
	         ser.write(xmldoc);
	         ser.flush();
	     String xmlstr = sw.toString();
	     xmlstr = xmlstr.replace("\n", "");
	     System.out.println(xmlstr);
        
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        
        for(CoreMap sentence: sentences) {
        	StringBuffer sb = new StringBuffer();
 //       	int count = 0;
        	StringBuffer nounAppend = new StringBuffer();
        	for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
	        	String word = token.get(TextAnnotation.class);  
	            sb.append(word);
	            sb.append(":");
	            String pos = token.get(PartOfSpeechAnnotation.class);
	            sb.append(pos);
	            sb.append(" ");
	            
	            if (pos.equals("NNP") && !"".equals(word)) {
	            	nounAppend.append(word+" ");
	            } else {
	            	String appender = nounAppend.toString().trim();
	            	if (!"".equals(appender))
	            		properNouns.add(appender);
	            	nounAppend = new StringBuffer();
	            }
        	}
//        	System.out.println(sb.toString());
//        	 SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
 //       	 dependencies.prettyPrint();
        }
        
//        System.out.println("\n"+properNouns.toString());
        Set <String> propNounSet = new HashSet<>(properNouns);
        Map <Integer, String> frequencyMap = new TreeMap<>();
        for (String word : propNounSet) {
//        	System.out.println(word + ": " + Collections.frequency(properNouns, word));
        	frequencyMap.put(new Integer(Collections.frequency(properNouns, word)),word);
        } 
        System.out.println("word frequencies : "+frequencyMap);

		Map<Integer, CorefChain> corefChainMap = document.get(CorefChainAnnotation.class);
//		System.out.println("corefChainMap : "+corefChainMap);
        
		List <List <String>> corefList = new ArrayList<>();
		for (Map.Entry<Integer, CorefChain> entry : corefChainMap.entrySet()) {
			CorefChain coreChain = entry.getValue();
			
			// Ignore representatives having self mentions alone
			if (coreChain.getMentionsInTextualOrder().size() <= 1)
				continue;

			// Get all the mentions mapped to the representative
			List <String> corefMentions = new ArrayList<>();
			for (CorefMention lMentions : coreChain.getMentionsInTextualOrder()) {

				StringBuffer refsB = new StringBuffer();
				List<CoreLabel> tkns = document
						.get(SentencesAnnotation.class)
						.get(lMentions.sentNum - 1).get(TokensAnnotation.class);
				String posType = null;
				int dif = 0;
				int end = 0;
				for (int j = lMentions.startIndex - 1; j < lMentions.endIndex - 1; j++) {
					if (j == lMentions.startIndex - 1) {
						end = tkns.get(j).endPosition();
					} else {
						dif = tkns.get(j).beginPosition() - end;
						end = tkns.get(j).endPosition();
					}
					for (int k = 0; k < dif; k++) {
						refsB.append(new String(" "));
					}
					refsB.append(tkns.get(j).get(TextAnnotation.class));
					posType = tkns.get(j).get(PartOfSpeechAnnotation.class);
				}
				String refs = refsB.toString().trim();
				corefMentions.add(refs);
			}
			corefList.add(corefMentions);
		}
		
		System.out.println("Coreference list : "+corefList);
		
		this.resolveCorefs (frequencyMap, corefList);
	}

	private void resolveCorefs(Map<Integer, String> frequencyMap,
			List<List<String>> corefList) {
		
		Map <String, Set<Integer>> additions = new LinkedHashMap<>();
		for (int frequency : frequencyMap.keySet()) {
			String wordPhrase = frequencyMap.get(frequency).toLowerCase();
			for (String word : wordPhrase.split(",")) {
				
				int i = 0;
				for (List<String> corefMentions : corefList) {
					i++;
					for (String mention : corefMentions) {
						mention = mention.toLowerCase();
						if (mention.equalsIgnoreCase(word) || mention.contains(word)) {
							
							if (additions.containsKey(word+":"+frequency)) {
								Set<Integer> listIds = additions.get(word+":"+frequency);
								listIds.add(i);
								additions.put(word+":"+frequency, listIds);
							} else {
								Set<Integer> listIds = new TreeSet<>();
								listIds.add(i);
								additions.put(word+":"+frequency, listIds);
							}
						}
					}
				}
			}			
		}
//		System.out.println(additions);
		Map <Integer, String> pointerWordMap = new TreeMap<>();
		Map <String, Integer> wordCountMap = new TreeMap<>();
		for (String wordPhrase : additions.keySet()) {
			String word = wordPhrase.split(":")[0];
			int wordCount = Integer.parseInt(wordPhrase.split(":")[1]);
			Set<Integer> corefMentionSet = additions.get(wordPhrase);
			boolean addable = true;
			for (Integer pointer : corefMentionSet) {
				pointer = pointer-1;
				if (pointerWordMap.containsKey(pointer)) {
					String word1 = pointerWordMap.get(pointer);
					int count = wordCountMap.get(word1);
					wordCount = wordCount+count;
					wordCountMap.put(word1, wordCount);
					addable = false;
				} else {
					wordCount = wordCount + corefList.get(pointer).size();
					pointerWordMap.put (pointer, word);
				}
			}
			if (addable)
				wordCountMap.put(word, wordCount);
		}
		System.out.println("Subject and counts of Blog : "+wordCountMap+"\n");
	}

}

