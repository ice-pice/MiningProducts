import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ml.WekaTrain;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

import persist.Tokeninfo;
import vo.TokenVo;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import xmlUtils.jdpa.Annotations;
import xmlUtils.jdpa.Annotations.Annotation;
import xmlUtils.jdpa.Annotations.ClassMention;
import xmlUtils.jdpa.Annotations.ClassMention.MentionClass;
import xmlUtils.stanford.Root;
import xmlUtils.stanford.Root.Document.Sentences.Sentence;
import xmlUtils.stanford.Root.Document.Sentences.Sentence.Dependencies;
import xmlUtils.stanford.Root.Document.Sentences.Sentence.Dependencies.Dep;
import xmlUtils.stanford.Root.Document.Sentences.Sentence.Dependencies.Dep.Dependent;
import xmlUtils.stanford.Root.Document.Sentences.Sentence.Dependencies.Dep.Governor;
import xmlUtils.stanford.Root.Document.Sentences.Sentence.Tokens;
import xmlUtils.stanford.Root.Document.Sentences.Sentence.Tokens.Token;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;


public class FeatureExtraction {

	private StanfordCoreNLP coreNLP;
	private final String GOVERNERS = "governers";
	private final String DEPENDENTS = "dependents";
	private final String ORGANIZATION = "Mention.Organization";
	private final String CARS = "Mention.Vehicles.Cars";
	private final String CARPART = "Mention.CarPart";
	private final String OTHERS = "others";
	EntityManager entityManager = null;
	Session session = null;
	Transaction tx = null;
	Classifier classifier = null;
	Instances testingInstances;

	public FeatureExtraction () {
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        coreNLP = new StanfordCoreNLP(props);
	}

	
	public StanfordCoreNLP getCoreNLP() {
		return coreNLP;
	}


	public void setCoreNLP(StanfordCoreNLP coreNLP) {
		this.coreNLP = coreNLP;
	}


	public static void main(String[] args) {
		FeatureExtraction fa = new FeatureExtraction();
//		fa.readTrainingCorpus();
		fa.readTestFile();
	}
	
	public void readTestFile () {

		this.loadWekaModel();
		File txtFile = new File("/home/vijay/Documents/IIIT/IRE/MajorPjt/JDPASentimentCorpus/car/batch001/txt/car-001-001.txt");
		File annotatedFile = new File("/home/vijay/Documents/IIIT/IRE/MajorPjt/JDPASentimentCorpus/car/batch001/annotation/car-001-001.txt.knowtator.xml");
		String txtContent = "";
		String annotatedContent = "";
		try {
			BufferedReader reader=new BufferedReader(new FileReader(txtFile));
			StringBuilder text=new StringBuilder();
			String str="";
			while((str=reader.readLine())!=null) {
				text.append(str);
				text.append("\n");
			}
			txtContent = text.toString();
			txtContent =  this.parseText(txtContent);
			
			
			text=new StringBuilder();
			str="";
			reader.close();
			reader=new BufferedReader(new FileReader(annotatedFile));
			while((str=reader.readLine())!=null) {
				text.append(str);
				text.append("\n");
			}
			annotatedContent = text.toString();
			this.testXmls(txtContent, annotatedContent);
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void readTestFile (String txtContent, String annotatedContent) {

		this.loadWekaModel();
		try {
			txtContent =  this.parseText(txtContent);
			this.testXmls(txtContent, annotatedContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void readTrainingCorpus () {
		
		File fldr1 = new File("/home/vijay/Documents/IIIT/IRE/MajorPjt/JDPASentimentCorpus/car/batch001/txt/");
		File fldr2 = new File("/home/vijay/Documents/IIIT/IRE/MajorPjt/JDPASentimentCorpus/car/batch001/annotation/");
		File [] fldr1_files = fldr1.listFiles();
		File [] fldr2_files = fldr2.listFiles();
		for (File txtFile :fldr1_files) {
			String fileName = txtFile.getName();
			System.out.println(fileName);
			fileName = fileName+".knowtator.xml";
			for (File annotateFile :fldr2_files) {
				String fileName2 = annotateFile.getName();
				if (fileName.equalsIgnoreCase(fileName2)) {
					try {
						String txtContent = "";
						String annotateContent = "";
						
						BufferedReader reader=new BufferedReader(new FileReader(txtFile));
						StringBuilder text=new StringBuilder();
						String str="";
						while((str=reader.readLine())!=null) {
							text.append(str);
							text.append("\n");
						}
						txtContent = text.toString();
						reader.close();
						reader=new BufferedReader(new FileReader(annotateFile));
						text=new StringBuilder();
						str="";
						while((str=reader.readLine())!=null) {
							text.append(str);
							text.append("\n");
						}
						annotateContent = text.toString();
						reader.close();
						txtContent = this.parseText(txtContent);		
						this.analyseXmls(txtContent, annotateContent);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (FeatureException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	

	public String parseText (String text) throws IOException {
		
    	edu.stanford.nlp.pipeline.Annotation document = new edu.stanford.nlp.pipeline.Annotation(text);
		coreNLP.annotate(document);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			coreNLP.xmlPrint(document, stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream.toString();	
	}
	
	
	public void analyseXmls (String txtContent, String annotatedContent) throws FeatureException {
		try {

			JAXBContext jaxbContext = JAXBContext.newInstance(Annotations.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			StringReader reader = new StringReader(annotatedContent);
			Annotations annotations = (Annotations) unmarshaller.unmarshal(reader);
			
			JAXBContext jaxbContext2 = JAXBContext.newInstance(Root.class);
			Unmarshaller unmarshaller2 = jaxbContext2.createUnmarshaller();
			StringReader reader2 = new StringReader(txtContent);
			Root root = (Root) unmarshaller2.unmarshal(reader2);
			
//			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("files/persistanceOP.txt"), true));

			Configuration configuration = new Configuration().configure();
			StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().
			applySettings(configuration.getProperties());
			SessionFactory factory = configuration.buildSessionFactory(builder.build());
			session = factory.openSession();
		
			List <Object> subtags = annotations.getAnnotationOrClassMentionOrComplexSlotMention();
			List <Token> tokenListMaster = new ArrayList<>();
			List <Dependencies> dependListMaster = new ArrayList<>();
			Set <Token> persistedTokens = new HashSet<>();
			int refSentenceCount=0;
			for (Object ele : subtags) {

				if (ele instanceof xmlUtils.jdpa.Annotations.ClassMention) {
					ClassMention mention = (ClassMention) ele;
					String classid = mention.getId();
					List <Object> classMentionTags = mention.getMentionClassOrHasSlotMentionOrMention();
					for (Object mentionTag : classMentionTags) {
						if (mentionTag instanceof MentionClass) {
							MentionClass mentionClass = (MentionClass) mentionTag;
							String mentionClassId = mentionClass.getId();
//							if (mentionClassId.equals(ORGANIZATION) || mentionClassId.equals(CARS) || mentionClassId.equals(CARPART)) {
								Annotation annotation = this.getAnnotationByMentionId (subtags, classid);
								String start = annotation.getSpan().getStart();
								String end = annotation.getSpan().getEnd();

								List<Sentence>sentences = root.getDocument().getSentences().getSentence();
								int sentenceCount = 0;
								sentenceBreaker:
								for (Sentence sentence : sentences) {
									sentenceCount++;
									Tokens tokens = sentence.getTokens();
									List <Dependencies> dependencies = sentence.getDependencies();
									List <Token> tokenList = tokens.getToken();
									int lastTokenNum = tokenList.size()-1;
									Token lastToken = tokenList.get(lastTokenNum);
									if (Integer.parseInt(lastToken.getCharacterOffsetEnd()) < Integer.parseInt(start))
										continue;
									boolean appendable = false;
									for (Token token : tokenList) {
										TokenVo tokenVo = this.getTokenVo(token);
										if (tokenVo.getTokenBegin()==Integer.parseInt(start) ) {
											appendable = true;
										}									
										if (appendable) {
											if ((tokenVo.getPos().contains("NN")
													|| tokenVo.getPos().contains(
															"JJ") || tokenVo
													.getPos().contains("CD"))
													&& (mentionClassId
															.equals(ORGANIZATION)
															|| mentionClassId
																	.equals(CARS) || mentionClassId
																.equals(CARPART))) {
												if (refSentenceCount == 0) {
													refSentenceCount = sentenceCount;
													tokenListMaster = tokenList;
													dependListMaster = dependencies;
												}
												this.persistToken(mentionClassId,
														tokenVo, dependencies,
														tokenList);
											}
											if (!persistedTokens.contains(token)) {
												persistedTokens.add(token);
											}
										} 
										if (tokenVo.getTokenEnd() == Integer.parseInt(end))
											break sentenceBreaker;
									}
								}
								if (sentenceCount > refSentenceCount) {
									refSentenceCount = 0;
									for (Token masterToken : tokenListMaster) {
										if (!persistedTokens.contains(masterToken)) {
											TokenVo tokenVo = this.getTokenVo(masterToken);
											this.persistToken(OTHERS, tokenVo, dependListMaster, tokenListMaster);
										}
									}
									tokenListMaster = new ArrayList<>();
									dependListMaster = new ArrayList<>();
								}
//							}							
						}
					}

				}
			}
//			bw.close();
		} catch (JAXBException e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			session.close();
		}
	}
	

	private void testXmls(String txtContent, String annotatedContent) {
		try {

			JAXBContext jaxbContext = JAXBContext.newInstance(Annotations.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			StringReader reader = new StringReader(annotatedContent);
			Annotations annotations = (Annotations) unmarshaller.unmarshal(reader);
			
			JAXBContext jaxbContext2 = JAXBContext.newInstance(Root.class);
			Unmarshaller unmarshaller2 = jaxbContext2.createUnmarshaller();
			StringReader reader2 = new StringReader(txtContent);
			Root root = (Root) unmarshaller2.unmarshal(reader2);
		
			List <Object> subtags = annotations.getAnnotationOrClassMentionOrComplexSlotMention();			
			List<Sentence>sentences = root.getDocument().getSentences().getSentence();
			List <String> opList = new ArrayList<>();
			for (Sentence sentence : sentences) {
				Tokens tokens = sentence.getTokens();
				List <Dependencies> dependencies = sentence.getDependencies();
				List <Token> tokenList = tokens.getToken();
				for (Token token : tokenList) {
					TokenVo tokenVo = this.getTokenVo(token);	
					String mentionClassName = "";
					subtagBreak:
					for (Object ele : subtags) {
						if (ele instanceof xmlUtils.jdpa.Annotations.ClassMention) {
							ClassMention mention = (ClassMention) ele;
							String classid = mention.getId();
							List <Object> classMentionTags = mention.getMentionClassOrHasSlotMentionOrMention();
							for (Object mentionTag : classMentionTags) {
								if (mentionTag instanceof MentionClass) {
									MentionClass mentionClass = (MentionClass) mentionTag;
									mentionClassName = mentionClass.getId();
									Annotation annotation = this.getAnnotationByMentionId (subtags, classid);
									String start = annotation.getSpan().getStart();
									String end = annotation.getSpan().getEnd();
									if (tokenVo.getTokenBegin()>=Integer.parseInt(start) && tokenVo.getTokenEnd()<=Integer.parseInt(end))
									break subtagBreak;
								}
							}

						}
					}					
					String op = this.predictToken(mentionClassName, tokenVo, dependencies, tokenList);
					if (!"".equals(op))
						opList.add(op);
				}
			}
			System.out.println(opList.size());
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void persistToken(String mentionClass, TokenVo tokenVo, List <Dependencies> dependencies, List <Token> tokenList) throws NumberFormatException {
	
		tx = session.beginTransaction();
//		TokenVo tokenVo = this.getTokenVo(tokenVo);
		Map <String, List<String>> dependencyMap = this.getDependents(tokenVo.getTokenId(), dependencies);
		Tokeninfo persistVo = new Tokeninfo();
		persistVo.setMentionClass(mentionClass);
		persistVo.setWordPOS(tokenVo.getPos());
		persistVo.setWordNER(tokenVo.getNer());
		persistVo.setWord(tokenVo.getWord());
		if (dependencyMap.containsKey(GOVERNERS)) {
			for(String tokenString : dependencyMap.get(GOVERNERS)) {
				int tokenIdx = Integer.parseInt(tokenString.split(":")[0]);
				String depend = tokenString.split(":")[1];
				TokenVo governVo  = this.getTokenVo(tokenIdx, tokenList);
				persistVo.setGovernPOS(governVo.getPos());
				persistVo.setGovernNER(governVo.getNer());
				persistVo.setGovernParse(depend);
				if (dependencyMap.containsKey(DEPENDENTS)) { 
					for(String tokenString2 : dependencyMap.get(DEPENDENTS)) {
						int tokenIdx2 = Integer.parseInt(tokenString2.split(":")[0]);
						String depend2 = tokenString2.split(":")[1];
						TokenVo dependVo  = this.getTokenVo(tokenIdx2, tokenList);
						persistVo.setDependPOS(dependVo.getPos());
						persistVo.setDependNER(dependVo.getNer());
						persistVo.setDependParse(depend2);
						session.save(persistVo);
					}
				} else {
					session.save(persistVo);
				}
			}
		} else if (dependencyMap.containsKey(DEPENDENTS)) { 
			for(String tokenString2 : dependencyMap.get(DEPENDENTS)) {
				int tokenIdx2 = Integer.parseInt(tokenString2.split(":")[0]);
				String depend2 = tokenString2.split(":")[1];
				TokenVo dependVo  = this.getTokenVo(tokenIdx2, tokenList);
				persistVo.setDependPOS(dependVo.getPos());
				persistVo.setDependNER(dependVo.getNer());
				persistVo.setDependParse(depend2);
				session.save(persistVo);
			}
		} else {
			session.save(persistVo);
		}	
		tx.commit();
	}
	
	
	public String predictToken(String mentionClass, TokenVo tokenVo, List <Dependencies> dependencies, List <Token> tokenList) throws NumberFormatException {

		String op = "";
		Map <String, List<String>> dependencyMap = this.getDependents(tokenVo.getTokenId(), dependencies);
		Tokeninfo persistVo = new Tokeninfo();
		persistVo.setMentionClass(mentionClass);
		persistVo.setWordPOS(tokenVo.getPos());
		persistVo.setWordNER(tokenVo.getNer());
		persistVo.setWord(tokenVo.getWord());
		if (dependencyMap.containsKey(GOVERNERS)) {
			for(String tokenString : dependencyMap.get(GOVERNERS)) {
				int tokenIdx = Integer.parseInt(tokenString.split(":")[0]);
				String depend = tokenString.split(":")[1];
				TokenVo governVo  = this.getTokenVo(tokenIdx, tokenList);
				persistVo.setGovernPOS(governVo.getPos());
				persistVo.setGovernNER(governVo.getNer());
				persistVo.setGovernParse(depend);
				if (dependencyMap.containsKey(DEPENDENTS)) { 
					for(String tokenString2 : dependencyMap.get(DEPENDENTS)) {
						int tokenIdx2 = Integer.parseInt(tokenString2.split(":")[0]);
						String depend2 = tokenString2.split(":")[1];
						TokenVo dependVo  = this.getTokenVo(tokenIdx2, tokenList);
						persistVo.setDependPOS(dependVo.getPos());
						persistVo.setDependNER(dependVo.getNer());
						persistVo.setDependParse(depend2);
//						session.save(persistVo);
					}
				} else {
//					session.save(persistVo);
				}
			}
		} else if (dependencyMap.containsKey(DEPENDENTS)) { 
			for(String tokenString2 : dependencyMap.get(DEPENDENTS)) {
				int tokenIdx2 = Integer.parseInt(tokenString2.split(":")[0]);
				String depend2 = tokenString2.split(":")[1];
				TokenVo dependVo  = this.getTokenVo(tokenIdx2, tokenList);
				persistVo.setDependPOS(dependVo.getPos());
				persistVo.setDependNER(dependVo.getNer());
				persistVo.setDependParse(depend2);
//				session.save(persistVo);
			}
		} else {
//			session.save(persistVo);
		}			
		this.predict(persistVo);

		if (mentionClass.equals(ORGANIZATION) || mentionClass.equals(CARS) || mentionClass.equals(CARPART)) {
			String opString = "Word : "+persistVo.getWord()+", GivenClass : "+mentionClass+", Prediction : "+persistVo.getWordNER();
			if (mentionClass.equalsIgnoreCase(persistVo.getWordNER())) {
				op= opString;
			}
		}
		return op;
	}


	private TokenVo getTokenVo(int id, List<Token> tokenList) {
		TokenVo tokenVo = new TokenVo();
		for (Token token : tokenList) {
			if (Integer.parseInt(token.getId()) == id) {
				tokenVo.setTokenId(Integer.parseInt(token.getId()));
				tokenVo.setNer(token.getNER());
				tokenVo.setPos(token.getPOS());
				tokenVo.setTokenBegin(Integer.parseInt(token.getCharacterOffsetBegin()));
				tokenVo.setTokenEnd(Integer.parseInt(token.getCharacterOffsetEnd()));
				tokenVo.setWord(token.getWord());
				tokenVo.setLemma(token.getLemma());
			}			
		}
		return tokenVo;
	}

	private Map<String, List<String>> getDependents(int tokenId,
			List<Dependencies> dependencies) {
		Map<String, List<String>> tempMap = new TreeMap<>();
		List <String> governers = new ArrayList<>();
		List <String> dependents = new ArrayList<>();
		for (Dependencies depends : dependencies) {
			if ("collapsed-ccprocessed-dependencies".equalsIgnoreCase(depends.getType())) {
				for (Dep dep :depends.getDep()) {
					String type = dep.getType();
					Governor gover = dep.getGovernor();
					Dependent dept = dep.getDependent();
					if (tokenId == Integer.parseInt(gover.getIdx()))
						dependents.add(dept.getIdx()+":"+type);
					if (tokenId == Integer.parseInt(dept.getIdx()))
						governers.add(gover.getIdx()+":"+type);
				}
			}
		}
		if (!governers.isEmpty())
			tempMap.put(GOVERNERS, governers);
		if (!dependents.isEmpty())
			tempMap.put(DEPENDENTS, dependents);
		return tempMap;
	}

	private TokenVo getTokenVo(Token token) {
		TokenVo tokenVo = new TokenVo();
		tokenVo.setTokenId(Integer.parseInt(token.getId()));
		tokenVo.setNer(token.getNER());
		tokenVo.setPos(token.getPOS());
		tokenVo.setTokenBegin(Integer.parseInt(token.getCharacterOffsetBegin()));
		tokenVo.setTokenEnd(Integer.parseInt(token.getCharacterOffsetEnd()));
		tokenVo.setWord(token.getWord());
		tokenVo.setLemma(token.getLemma());
		return tokenVo;
	}

	private xmlUtils.jdpa.Annotations.Annotation getAnnotationByMentionId(
			List<Object> subtags, String mentionClassId) {

		Annotation annotation = null;
		for (Object ele : subtags) {
			if (ele instanceof xmlUtils.jdpa.Annotations.Annotation) {
				annotation = (Annotation) ele;
				String mentionid = annotation.getMention().getId();		
				if (mentionid.equals(mentionClassId))
					break;
			}

		}
		return annotation;
	}
	
	public void loadWekaModel () {
		try {
			classifier = (Classifier) weka.core.SerializationHelper.read("files/OneR.model");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void predict (Tokeninfo persistVo) {
		
		try {
			WekaTrain wt = new WekaTrain();
			if (testingInstances == null) 
				testingInstances = wt.setDBTraining();
   
			Instance instance = new Instance(8);  
			String wordpos = persistVo.getWordPOS();
			if ("".equals(wordpos))
				instance.setMissing(testingInstances.attribute(0));
			else 
				instance.setValue(testingInstances.attribute(0), wordpos);
			
			if ("".equals(persistVo.getWordNER()))
				instance.setMissing(testingInstances.attribute(1));
			else 
				instance.setValue(testingInstances.attribute(1), persistVo.getWordNER());

			if ("".equals(persistVo.getGovernPOS()))
				instance.setMissing(testingInstances.attribute(2));
			else 
				instance.setValue(testingInstances.attribute(2), persistVo.getGovernPOS());	
			
			if ("".equals(persistVo.getGovernNER()))
				instance.setMissing(testingInstances.attribute(3));
			else 
				instance.setValue(testingInstances.attribute(3), persistVo.getGovernNER());	
			
			if ("".equals(persistVo.getGovernParse()))
				instance.setMissing(testingInstances.attribute(4));
			else 
				instance.setValue(testingInstances.attribute(4), persistVo.getGovernParse());			
			
			if ("".equals(persistVo.getDependPOS()))
				instance.setMissing(testingInstances.attribute(5));
			else 
				instance.setValue(testingInstances.attribute(5), persistVo.getDependPOS());			
			
			if ("".equals(persistVo.getDependNER()))
				instance.setMissing(testingInstances.attribute(6));
			else 
				instance.setValue(testingInstances.attribute(6), persistVo.getDependNER());

			if ("".equals(persistVo.getDependParse()))
				instance.setMissing(testingInstances.attribute(7));
			else 
				instance.setValue(testingInstances.attribute(7), persistVo.getDependParse());		
		
//			instance.setValue(a9, "?");
			instance.setDataset(testingInstances);
			testingInstances.add(instance);
			
			double score = classifier.classifyInstance(instance);
			String prediction = testingInstances.classAttribute().value((int) score);
			persistVo.setWordNER(prediction);
		} catch (Exception e) {
//		e.printStackTrace();
		}
		
	}
	
}