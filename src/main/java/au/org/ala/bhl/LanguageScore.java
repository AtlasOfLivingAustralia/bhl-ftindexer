package au.org.ala.bhl;

public class LanguageScore {
	
	private String _language;
	private double _score;
	
	public LanguageScore(String name, double score) {
		_language = name;
		_score = score;
	}
	
	public String getName() {
		return _language;
	}
	
	public double getScore() {
		return _score;
	}
	
	@Override
	public String toString() {
		return String.format("%s (%g)", _language, _score);
	}

}
