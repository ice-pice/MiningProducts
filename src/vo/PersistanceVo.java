package vo;

public class PersistanceVo {
	
	private String word = "-";
	private String wordPOS = "-";
	private String wordNER = "-";
	private String governPOS = "-";
	private String governNER = "-";
	private String governParse = "-";
	private String dependPOS = "-";
	private String dependNER = "-";
	private String dependParse = "-";
	private String mentionClass = "-";

	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getWordPOS() {
		return wordPOS;
	}
	public void setWordPOS(String wordPOS) {
		this.wordPOS = wordPOS;
	}
	public String getWordNER() {
		return wordNER;
	}
	public void setWordNER(String wordNER) {
		this.wordNER = wordNER;
	}
	public String getGovernPOS() {
		return governPOS;
	}
	public void setGovernPOS(String governPOS) {
		this.governPOS = governPOS;
	}
	public String getGovernNER() {
		return governNER;
	}
	public void setGovernNER(String governNER) {
		this.governNER = governNER;
	}
	public String getGovernParse() {
		return governParse;
	}
	public void setGovernParse(String governParse) {
		this.governParse = governParse;
	}
	public String getDependPOS() {
		return dependPOS;
	}
	public void setDependPOS(String dependPOS) {
		this.dependPOS = dependPOS;
	}
	public String getDependNER() {
		return dependNER;
	}
	public void setDependNER(String dependNER) {
		this.dependNER = dependNER;
	}
	public String getDependParse() {
		return dependParse;
	}
	public void setDependParse(String dependParse) {
		this.dependParse = dependParse;
	}
	public String getMentionClass() {
		return mentionClass;
	}
	public void setMentionClass(String mentionClass) {
		this.mentionClass = mentionClass;
	}
	public String toString() {
		return word+" "+wordPOS+" "+wordNER+" "+governPOS+" "+governNER+" "+governParse+" "+dependPOS+" "+dependNER+" "+dependParse+" "+mentionClass;
	}

}
