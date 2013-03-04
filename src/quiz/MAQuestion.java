/**
 * 
 */
package quiz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import database.MyDB;

/**
 * @author yang
 * 
 */
public class MAQuestion extends QuestionBase {
	// Use string rather than boolean, in order to indicate the possible "error"
	private final String isOrder;

	private static final String typeIntro = "In this type of question, given a question, "
			+ "users need to answer all the answer fields. Every answer field is just part of the answer."
			+ "Only correctly answering all the question fields could a user get full score"
			+ "Correctly answering an answer field will get a positive 3 points"
			+ "Otherwise, user would get a negative 1 point."
			+ "The final grade is the sum of postive and negative points. Lowest score is 0";

	/**
	 * @param questionType
	 * @param questionId
	 */
	public MAQuestion(String questionType, String questionId) {
		super(questionType, questionId);
		String tmpIsOrder = "error";

		try {
			Connection con = MyDB.getConnection();
			Statement stmt = con.createStatement();
			stmt.executeQuery("USE c_cs108_yzhao3");
			ResultSet rs = stmt.executeQuery(queryStmt);
			rs.next();
			tmpIsOrder = rs.getString(9);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		isOrder = tmpIsOrder;
	}

	/**
	 * @param questionType
	 * @param creatorId
	 * @param questionDescription
	 * @param answer
	 * @param maxScore
	 * @param tagString
	 * @param correctRatio
	 */
	public MAQuestion(String questionType, String creatorId,
			String questionDescription, String answer, String maxScore,
			String tagString, float correctRatio, String isOrder) {
		super(questionType, creatorId, questionDescription, answer, maxScore,
				tagString, correctRatio);
		this.isOrder = isOrder;
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#saveToDatabase()
	 */
	@Override
	public void saveToDatabase() {
		queryStmt = "INSERT INTO " + MA_Table + " VALUES (\"" + questionId
				+ "\", \"" + creatorId + "\", \"" + typeIntro + "\", \""
				+ questionDescription + "\", \"" + answer + "\", \"" + maxScore
				+ "\", \"" + tagString + "\", \"" + correctRatio + "\", \""
				+ isOrder + "\")";
		try {
			Connection con = MyDB.getConnection();
			Statement stmt = con.createStatement();
			stmt.executeUpdate(queryStmt);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String printCreateHtml() {
		// TODO Auto-generated method stub
		StringBuilder html = new StringBuilder();
		html.append("<h1>This page will guide you to create a question-response question</h1>\n");
		html.append("<form action=\"QuizCreationServlet\" method=\"post\">");
		html.append("<p> Please enter proposed question description and answer </p>\n");
		html.append("<p>Question Description: <textarea name=\"questionDescription\" rows=\"10\" cols=\"50\"></textarea></p>\n");

		// TODO: javascript to dynamically expand the number of answers
		html.append("<p>Answer:   <input type=\"text\" name=\"answer0\" ></input></p>");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer1\" ></input></p>");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer2\" ></input></p>");
		html.append("<p>Score:   <input type=\"text\" name=\"maxScore\" ></input></p>");

		// Hidden information - questionType,tag and number of answers
		// TODO: numAnswer will be automatically generated in javascript??
		html.append("<p><input type=\"hidden\" name=\"questionType\"  value=\""
				+ QuestionBase.QR + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"numAnswer\" value=\"3\"></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"tag\" value=\"not_implemeted\"></input></p>\n");
		html.append("<input type=\"submit\" value = \"Save\"/></form>");
		return html.toString();
	}

	@Override
	public String printReadHtml() {
		// TODO Auto-generated method stub
		StringBuilder html = new StringBuilder();
		html.append(super.printReadHtml());

		html.append("<p>This is a question page, please read the question information, and make an answer</p>");
		html.append("<p>" + typeIntro + "</p>\n");
		html.append("<form action=\"QuestionProcessServlet\" method=\"post\">");
		html.append("<p>Question Description: ");
		html.append(questionDescription + "</p>");
		// TODO: use javascript to dynamically generate multi-answer field
		html.append("<p>Answer:   <input type=\"text\" name=\"answer0\" ></input></p>");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer1\" ></input></p>");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer2\" ></input></p>");

		// Hidden information - questionType and questionId information
		html.append("<p><input type=\"hidden\" name=\"numAnswer\" value=\"3\"></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionType\" value=\""
				+ getQuestionType() + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"questionId\" value=\""
				+ getQuestionId() + "\"  ></input></p>");
		html.append("<input type=\"submit\" value = \"Next\"/></form>");

		return html.toString();
	}

	/**
	 * Answer format: #answer0#answer1#answer2#...#
	 * 
	 * @return
	 */
	public static String getAnswerString(HttpServletRequest request) {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		int numAnswer = Integer.parseInt((String) session
				.getAttribute("numAnswer"));
		StringBuilder answer = new StringBuilder();
		for (int i = 0; i < numAnswer; i++) {
			answer.append("#");
			answer.append((String) session.getAttribute("answer" + i));
			answer.append("#");
		}
		return answer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getScore(java.lang.String)
	 */
	@Override
	public String getScore(String userInput) {
		String[] answerList = parseAnswer(answer);
		String[] inputList = parseAnswer(userInput);

		if (isOrder.equals("true"))
			return Integer.toString(getOrderedScore(answerList, inputList));
		else
			return Integer.toString(getUnorderedScore(answerList, inputList));
	}

	private int getOrderedScore(String[] answerList, String[] inputList) {
		int score = 0;
		for (int i = 0; i < answerList.length; i++) {
			if (inputList[i].equals(answerList[i]))
				score += 3;
			else
				score -= 1;
		}
		return score >= 0 ? score : 0;
	}

	private int getUnorderedScore(String[] answerList, String[] inputList) {
		int score = 0;
		HashSet<String> answerSet = new HashSet<String>();
		for (String str : answerList) {
			answerSet.add(str);
		}
		for (String str : inputList) {
			if (answerSet.contains(str))
				score += 3;
			else
				score -= 1;
		}
		return score;
	}

	private String[] parseAnswer(String answerString) {
		String[] answerList = answerString.split("#");
		return answerList;
	}

}