package vo;

public class TokenVo {

	private int tokenId;
	private int tokenBegin;
	private int tokenEnd;
	private String pos = "";
	private String ner = "";
	private String word = "";
	private String lemma = "";
	
	public int getTokenId() {
		return tokenId;
	}
	public void setTokenId(int tokenId) {
		this.tokenId = tokenId;
	}
	public int getTokenBegin() {
		return tokenBegin;
	}
	public void setTokenBegin(int tokenBegin) {
		this.tokenBegin = tokenBegin;
	}
	public int getTokenEnd() {
		return tokenEnd;
	}
	public void setTokenEnd(int tokenEnd) {
		this.tokenEnd = tokenEnd;
	}
	public String getPos() {
		return pos;
	}
	public void setPos(String pos) {
		this.pos = pos;
	}
	public String getNer() {
		return ner;
	}
	public void setNer(String ner) {
		this.ner = ner;
	}
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	public String toString() {
		return "word : "+lemma+", pos : "+pos+", ner : "+ner;
		
	}
}