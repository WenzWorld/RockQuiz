/**
 * 
 */
package quiz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import database.MyDB;

/**
 * @author yang
 * 
 */
public class PResponse extends QuestionBase {
	private String url;
	private static final String typeIntro = "In this type of question, given a picture,"
			+ "user need to answer the related question in the answer area. Correct answer will get full score, "
			+ "while the wrong answer will get zero";

	public PResponse(String questionType, String creatorId, int timeLimit,
			String questionDescription, String answer, int maxScore,
			String tagString, float correctRation, String url) {
		super(questionType, creatorId, timeLimit, questionDescription, answer,
				maxScore, tagString, correctRation);
		// TODO Auto-generated constructor stub
		this.url = url;
	}

	public PResponse(String questionType, String questionId) {
		super(questionType, questionId);
		try {
			Connection con = MyDB.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(queryStmt);
			rs.next();
			url = rs.getString(9);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getQuerySaveString()
	 */
	@Override
	public String getQuerySaveString() {
		return "INSERT INTO " + PR_Table + " VALUES (\""
				+ super.getBaseQuerySaveString() + ", \"" + url + "\")";
	}

	public static String printCreateHtmlSinglePage() {
		StringBuilder html = new StringBuilder();
		html.append("<h4>This section will guide you to create a picture-response question</h4>");
		html.append("Please enter proposed question description, answer and absolute picture url.");
		html.append("<p class=\"description\">Question Description:</p>");
		html.append("<textarea name=\"questionDescription\" rows=\"10\" cols=\"50\""
				+ " required></textarea><br>");

		// url information
		html.append("Picture URL: <input type=\"text\" name=\"url\""
				+ "\" required></input><br>");
		html.append("Answer:   <input type=\"text\" name=\"answer\""
				+ "\" required></input><br>");
		html.append("Score:   <input class=\"max_score\" type=\"text\" name=\"maxScore\""
				+ "\" required></input>");

		// add timeLimit field
		html.append("<div class=time_limit_div>Time Limit:   ");
		html.append("<input class=\"time_limit\" type=\"text\" name=\"timeLimit\" value=\"0\" ></input><br>");
		html.append("</div>");

		// Hidden information - question Type and tag information
		html.append("<p><input type=\"hidden\" name=\"questionType\" value=\""
				+ QuestionBase.PR + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"tag\" value=\"not_implemeted\"></input></p>");
		return html.toString();
	}

	@Override
	public String printEditHtml(int suffix) {
		StringBuilder html = new StringBuilder();
		html.append("<h4>Picture-Response Question</h4>");
		html.append("Please enter proposed question description, answer and absolute picture url.");
		html.append("<p class=\"description\">Question Description:</p>");
		html.append("<textarea name=\"questionDescription_" + suffix
				+ "\" rows=\"10\" cols=\"50\"" + " required>"
				+ getQuestionDescription() + "</textarea><br>");

		// url information
		html.append("Picture URL: <input type=\"text\" name=\"url_" + suffix
				+ "\" required value=\"" + url + "\"></input><br>");
		// add answer field
		html.append("Answer:   <input type=\"text\" name=\"answer_" + suffix
				+ "\" required value=\"" + getAnswer() + "\" ></input><br>\n");
		html.append("Score:<input class=\"max_score\" type=\"text\" name=\"maxScore_"
				+ suffix
				+ "\" required value=\""
				+ getMaxScore()
				+ "\" ></input>\n");

		// add timeLimit field
		html.append("<div class=time_limit_div>Time Limit:   \n");
		html.append("<input class=\"time_limit\" type=\"text\" name=\"timeLimit_"
				+ suffix
				+ "\" value=\""
				+ getTimeLimit()
				+ "\" ></input><br>\n");
		html.append("</div>\n");

		// Hidden information - question Type and tag information
		html.append("<p><input type=\"hidden\" name=\"questionType_" + suffix
				+ "\" value=\"" + QuestionBase.PR + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"tag_" + suffix
				+ "\" value=\"not_implemeted\"></input></p>");
		return html.toString();
	}

	@Override
	public String printReadHtml() {
		// TODO Auto-generated method stub
		StringBuilder html = new StringBuilder();
		html.append(super.printReadHtml());

		html.append("<p>" + typeIntro + "</p>\n");
		html.append("<form action=\"QuestionProcessServlet\" method=\"post\" id=\"questionRead\">\n");

		// question description
		html.append("<div class=\"question\">");
		html.append("<div class='description'>Question Description:</div>");
		html.append(questionDescription);

		html.append("<img border=\"0\" src=\"" + url
				+ "\" width=\"304\" height=\"228\">\n");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer_"
				+ getQuestionId() + "\" ></input></p>\n");

		// Hidden information - questionType and questionId information
		html.append("<input id=\"time_limit\" type=\"hidden\" name=\"timeLimit\" value=\""
				+ timeLimit + "\" ></input>");

		html.append("<p><input type=\"hidden\" name=\"questionType_"
				+ getQuestionId() + "\" value=\"" + getQuestionType()
				+ "\"></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionId_"
				+ getQuestionId() + "\" value=\"" + getQuestionId()
				+ "\" ></input></p>\n");
		html.append("</div>");

		html.append("<input type=\"submit\" value = \"Next\"/></form>\n");

		return html.toString();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getScore(java.lang.String)
	 */
	@Override
	public int getScore(String userInput) {
		// TODO Auto-generated method stub
		if (userInput.equals(answer))
			return maxScore;
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#printReadHtmlForSingle()
	 */
	@Override
	public String printReadHtmlForSingle() {
		StringBuilder html = new StringBuilder();
		html.append(super.printReadHtml());

		html.append("<p>This is a question page, please read the question information, and make an answer</p>\n");
		html.append("<p>" + typeIntro + "</p>\n");

		// form action should be here
		html.append("<p>Question Description: ");
		html.append(questionDescription + "</p>\n");

		// every form field will be renamed as xx_questionId
		html.append("<img border=\"0\" src=\"" + url
				+ "\" width=\"304\" height=\"228\">\n");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer_"
				+ getQuestionId() + "\" ></input></p>\n");

		// Hidden information - questionType and questionId information
		html.append("<p><input type=\"hidden\" name=\"questionType_"
				+ getQuestionId() + "\" value=\"" + getQuestionType()
				+ "\" ></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionId_"
				+ getQuestionId() + "\" value=\"" + getQuestionId()
				+ "\"  ></input></p>\n");

		return html.toString();
	}

	public static String getCreatedAnswer(HttpServletRequest request) {
		return request.getParameter("answer");
	}

	public static String getCreatedAnswer(HttpServletRequest request, int suffix) {
		return request.getParameter("answer_" + suffix);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * quiz.QuestionBase#getUserAnswer(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public String getUserAnswer(HttpServletRequest request) {
		String userAnswer = request.getParameter("answer_" + getQuestionId());
		if (userAnswer == null)
			userAnswer = "";
		return userAnswer;
	}

	public Element toElement(Document doc) {
		Element questionElem = null;

		questionElem = doc.createElement("question");

		// set question type as attribute to the root
		Attr typeAttr = doc.createAttribute("type");
		typeAttr.setValue("picture-response");
		questionElem.setAttributeNode(typeAttr);

		// set question id as attribute to the root
		// Attr idAttr = doc.createAttribute("id");
		// idAttr.setValue(questionId);
		// questionElem.setAttributeNode(idAttr);

		// add question descritpion(query)
		Element query = doc.createElement("query");
		query.appendChild(doc.createTextNode(questionDescription));
		questionElem.appendChild(query);

		// add url
		Element urlElem = doc.createElement("image-location");
		urlElem.appendChild(doc.createTextNode(this.url));
		questionElem.appendChild(urlElem);

		// add answer
		Element answer = doc.createElement("answer");
		answer.appendChild(doc.createTextNode(this.answer));
		questionElem.appendChild(answer);

		// add time-limit
		Element timeLimit = doc.createElement("time-limit");
		timeLimit.appendChild(doc.createTextNode(Integer
				.toString(this.timeLimit)));
		questionElem.appendChild(timeLimit);

		// add score
		Element maxScore = doc.createElement("score");
		maxScore.appendChild(doc.createTextNode(Integer.toString(this.maxScore)));
		questionElem.appendChild(maxScore);

		// add tag
		Element tag = doc.createElement("tag");
		tag.appendChild(doc.createTextNode(this.tagString));
		questionElem.appendChild(tag);

		return questionElem;
	}

}
