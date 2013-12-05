public class Player
{
	private String pseudo;
	private int score;
	
	public Player(String p)
	{
		this.pseudo = p;
		this.score = 0;
	}

	public void updateScore(int to_add)
	{
		this.score = this.score + to_add;
	}
	
	public String getPseudo() { return this.pseudo; }
	public int getScore() { return this.score; }

}
