package ml;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.OptionHandler;
import weka.core.Utils;
import weka.experiment.InstanceQuery;
import weka.filters.Filter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

/**
 * A little demo java program for using WEKA.<br/>
 * Check out the Evaluation class for more details.
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 * @see Evaluation
 */
public class WekaTrain {
  /** the classifier used internally */
  protected Classifier m_Classifier = null;
  
  /** the filter to use */
  protected Filter m_Filter = null;

  /** the training file */
  protected String m_TrainingFile = null;

  /** the training instances */
  protected Instances m_Training = null;

  /** for evaluating the classifier */
  protected Evaluation m_Evaluation = null;

  /**
   * initializes the demo
   */
  public WekaTrain() {
    super();
  }

  /**
   * sets the classifier to use
   * @param name        the classname of the classifier
   * @param options     the options for the classifier
   */
  public void setClassifier(String name, String[] options) throws Exception {
    m_Classifier = Classifier.forName(name, options);
  }

  /**
   * sets the filter to use
   * @param name        the classname of the filter
   * @param options     the options for the filter
   */
  public void setFilter(String name, String[] options) throws Exception {
    m_Filter = (Filter) Class.forName(name).newInstance();
    if (m_Filter instanceof OptionHandler)
      ((OptionHandler) m_Filter).setOptions(options);
  }

  /**
   * sets the file to use for training
   */
  public void setTraining() throws Exception {
/*    m_TrainingFile = name;
      m_Training     = new Instances(
                        new BufferedReader(new FileReader(m_TrainingFile)));
      m_Training.setClassIndex(m_Training.numAttributes() - 1);*/
	  InstanceQuery query = new InstanceQuery();
	  query.setUsername("root");
	  query.setPassword("password");
	  query.setQuery("SELECT wordPOS,wordNER,governPOS,governNER,governParse,dependPOS,dependNER,dependParse,class FROM tokeninfo");
	  // You can declare that your data set is sparse
	  // query.setSparseData(true);
	  m_Training = query.retrieveInstances();
	  m_Training.setClassIndex(m_Training.numAttributes() - 1); 
  }
  
  public Instances setDBTraining() throws Exception {

	  InstanceQuery query = new InstanceQuery();
	  query.setUsername("root");
	  query.setPassword("password");
	  query.setQuery("SELECT wordPOS,wordNER,governPOS,governNER,governParse,dependPOS,dependNER,dependParse,class FROM tokeninfo");
	  m_Training = query.retrieveInstances();
	  m_Training.setClassIndex(m_Training.numAttributes() - 1); 
	  return m_Training;
  }

  /**
   * runs 10fold CV over the training file
   */
  public void execute() throws Exception {
    // run filter
//    m_Filter.setInputFormat(m_Training);
//    Instances filtered = Filter.useFilter(m_Training, m_Filter);
    Instances filtered = m_Training;
    
    // train classifier on complete file for tree
    m_Classifier.buildClassifier(m_Training);
    
    // 10fold CV with seed=1
    m_Evaluation = new Evaluation(filtered);
    m_Evaluation.crossValidateModel(
        m_Classifier, filtered, 10, m_Training.getRandomNumberGenerator(1));
  }

  /**
   * outputs some data about the classifier
   */
  public String toString() {
    StringBuffer        result;

    result = new StringBuffer();
    result.append("Weka - Demo\n===========\n\n");

    result.append("Classifier...: " 
        + m_Classifier.getClass().getName() + " " 
        + Utils.joinOptions(m_Classifier.getOptions()) + "\n");

    result.append("Training file: " 
        + m_TrainingFile + "\n");
    result.append("\n");

    result.append(m_Classifier.toString() + "\n");
    result.append(m_Evaluation.toSummaryString() + "\n");
    try {
      result.append(m_Evaluation.toMatrixString() + "\n");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    try {
      result.append(m_Evaluation.toClassDetailsString() + "\n");
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return result.toString();
  }

  /**
   * returns the usage of the class
   */
  public static String usage() {
    return
        "\nusage:\n  " + WekaTrain.class.getName() 
        + "  CLASSIFIER <classname> [options] \n"
        + "  FILTER <classname> [options]\n"
        + "  DATASET <trainingfile>\n\n"
        + "e.g., \n"
        + "  java -classpath \".:weka.jar\" WekaDemo \n"
        + "    CLASSIFIER weka.classifiers.trees.J48 -U \n"
        + "    FILTER weka.filters.unsupervised.instance.Randomize \n"
        + "    DATASET iris.arff\n";
  }
  

  public static void main(String[] args) throws Exception {
	  
    WekaTrain         demo;
    String classifier = "weka.classifiers.rules.OneR";
    String saveModel = "files/OneR.model";
    String filter = "weka.filters.unsupervised.instance.Randomize";
    Vector classifierOptions = new Vector();

    // run
    demo = new WekaTrain();
    demo.setClassifier(
        classifier, 
        (String[]) classifierOptions.toArray(new String[classifierOptions.size()]));
    demo.setTraining();
    demo.execute();
    weka.core.SerializationHelper.write(saveModel, demo.m_Classifier);
    System.out.println(demo.toString());
  }
}
