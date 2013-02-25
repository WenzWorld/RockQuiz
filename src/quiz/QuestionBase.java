
package quiz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import database.MyDB;


public abstract class QuestionBase {
	protected String questionType;  //how to use final here?
	protected String questionId;
	protected String creatorId;
	protected String typeIntro;
	protected String questionDescription;
	protected String answer;
	protected String maxScore;
	protected String tagString;
	protected String correctRatio;

	
	
	protected Connection con;
	protected Statement stmt;
	protected ResultSet rs;
	protected String queryStmt;
	
	protected static final String QR = "Question_Response";
	protected static final String FIB = "Fill_In_Blank";
	protected static final String MC = "Multi_Choice";
	protected static final String PR = "Picture_Response";
	
	protected static final String QR_Table = "Question_Response_Pool";
	protected static final String FIB_Table = "Fill_In_Blank_Pool";
	protected static final String MC_Table = "Multi_Choice_Pool";
	protected static final String PR_Table = "Picture_Response_Pool";
	
	
	
	//have to have this, otherwise subclass (i.e. QResponse(String questionId)) will cause error
	public QuestionBase() {};
	
	//Create a question from webpage
	public QuestionBase(String questionType, String questionId,
			String creatorId, String typeIntro, String questionDescription,
			String answer, String maxScore, String tagString,
			String correctRation) {
		super();
		this.questionType = questionType;
		this.questionId = questionId;
		this.creatorId = creatorId;
		this.typeIntro = typeIntro;
		this.questionDescription = questionDescription;
		this.answer = answer;
		this.maxScore = maxScore;
		this.tagString = tagString;
		this.correctRatio = correctRation;
	}

	//	Create a question from database

	
	
	//MyQuiz get a question using questionType and questionId
	public static QuestionBase getQuestion(String questionType, String questionId){
		//switch questionType -- factory?
		
		if (questionType.equals(QR)) 
			return new QResponse(questionId);
		if (questionType.equals(FIB)) 
			return new FillInBlank(questionId);
		if (questionType.equals(MC))
			return new MultiChoice(questionId);
		if (questionType.equals(PR))
			return new PResponse(questionId);
		return null;
	}	
	
	
	
	//when clicking submit
	public abstract void saveToDatabase();
	
	//called by quiz to print html for every question
	//essentially, it is a html-string
	public abstract String printHTML(); 
    
	
	public abstract String getScore(String userInput);
	
	
	public String getMaxScore() {
		return maxScore;
	}
	
	public String getQuestionType() {
		return questionType;
	}
	public String getQuestionId() {
		return questionId;
	}
	public String getTypeIntro() {
		return typeIntro;
	}
	public String getQuestionDescription() {
		return questionDescription;
	}
	public String getAnswer() {
		return answer;
	}
	
}

