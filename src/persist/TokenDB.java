package persist;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the tokeninfo database table.
 * 
 */
@Entity
@Table(name="tokeninfo")
public class TokenDB implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private int wordid;

	@Column(name="class")
	private String class_;

	private String dependNER;

	private String dependParse;

	private String dependPOS;

	private String governNER;

	private String governParse;

	private String governPOS;

	private String word;

	private String wordNER;

	private String wordPOS;

	public TokenDB() {
	}

	public int getWordid() {
		return this.wordid;
	}

	public void setWordid(int wordid) {
		this.wordid = wordid;
	}

	public String getClass_() {
		return this.class_;
	}

	public void setClass_(String class_) {
		this.class_ = class_;
	}

	public String getDependNER() {
		return this.dependNER;
	}

	public void setDependNER(String dependNER) {
		this.dependNER = dependNER;
	}

	public String getDependParse() {
		return this.dependParse;
	}

	public void setDependParse(String dependParse) {
		this.dependParse = dependParse;
	}

	public String getDependPOS() {
		return this.dependPOS;
	}

	public void setDependPOS(String dependPOS) {
		this.dependPOS = dependPOS;
	}

	public String getGovernNER() {
		return this.governNER;
	}

	public void setGovernNER(String governNER) {
		this.governNER = governNER;
	}

	public String getGovernParse() {
		return this.governParse;
	}

	public void setGovernParse(String governParse) {
		this.governParse = governParse;
	}

	public String getGovernPOS() {
		return this.governPOS;
	}

	public void setGovernPOS(String governPOS) {
		this.governPOS = governPOS;
	}

	public String getWord() {
		return this.word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getWordNER() {
		return this.wordNER;
	}

	public void setWordNER(String wordNER) {
		this.wordNER = wordNER;
	}

	public String getWordPOS() {
		return this.wordPOS;
	}

	public void setWordPOS(String wordPOS) {
		this.wordPOS = wordPOS;
	}

}