/*******************************************************************************
 * The MIT License (MIT)
 * Copyright (c) 2013 Jing Pu, Yang Zhao, You Yuan, Huijie Yu 
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to 
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
 * sell copies of the Software, and to permit persons to whom the Software is 
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
 * THE SOFTWARE.
 ******************************************************************************/
/**
 * 
 */
package quiz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.Helper;
import database.MyDB;

/**
 * @author yang
 * 
 */
public class MCMAQuestion extends QuestionBase {
	// choices format: #chioce0#choice1#choice2#...#
	protected final String choices;
	private static final String typeIntro = "Multi-Choice-Multi-Answer question: user should choose one or more correct answers from choice options"
			+ "Choosing all correct answer will get full score, and choosing partial correct answer will get partial score"
			+ "while choosing any wrong answer will fix zero at your score. So, be careful!";

	/**
	 * @param questionType
	 * @param questionId
	 */
	public MCMAQuestion(String questionType, String questionId) {
		super(questionType, questionId);
		String tmpChoices = "error";

		try {
			Connection con = MyDB.getConnection();
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(queryStmt);
			rs.next();
			tmpChoices = rs.getString("choices");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		choices = tmpChoices;
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
	public MCMAQuestion(String questionType, String creatorId, int timeLimit,
			String questionDescription, String answer, int maxScore,
			String tagString, float correctRatio, String choices) {
		super(questionType, creatorId, timeLimit, questionDescription, answer,
				maxScore, tagString, correctRatio);
		this.choices = choices;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getMaxScore()
	 */
	@Override
	public int getMaxScore() {
		List<String> answerList = Helper.parseTags(answer);
		return maxScore * answerList.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getQuerySaveString()
	 */
	@Override
	public String getQuerySaveString() {
		return "INSERT INTO " + MCMA_Table + " VALUES (\""
				+ super.getBaseQuerySaveString() + ", \"" + choices + "\")";
	}

	public static String getCreatedAnswer(HttpServletRequest request, int suffix) {
		String answerList[] = request.getParameterValues("answer" + "_"
				+ suffix);
		StringBuilder answer = new StringBuilder();
		for (String str : answerList) {
			answer.append("#");
			// use request.getParameter(choice0) to get answerBody
			String answerBody = request.getParameter(str + "_" + suffix);
			answer.append(answerBody);
			answer.append("#");
		}
		return answer.toString();
	}

	public static String getCreatedChoices(HttpServletRequest request,
			int suffix) {
		int numChoices = Integer.parseInt(request.getParameter("numChoices"
				+ "_" + suffix));
		StringBuilder choices = new StringBuilder();
		for (int i = 0; i < numChoices; i++) {
			choices.append("#");
			choices.append(request.getParameter("choice" + i + "_" + suffix));
			choices.append("#");
		}
		return choices.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getScore(java.lang.String)
	 */
	@Override
	public int getScore(String userInput) {
		Set<String> answerSet = getAnswerSet();
		List<String> inputList = Helper.parseTags(userInput);

		return getScore(answerSet, inputList);
	}

	// overload
	private int getScore(Set<String> answerSet, List<String> inputList) {
		int score = 0;
		for (String str : inputList) {
			if (answerSet.contains(str))
				score += maxScore; // if correct, score + 1
			else
				return 0; // if incorrect, score = 0
		}
		return score;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * quiz.QuestionBase#getUserAnswer(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public String getUserAnswer(HttpServletRequest request) {
		String answerList[] = request.getParameterValues("answer_"
				+ getQuestionId());
		if (answerList == null)
			return "";
		StringBuilder answer = new StringBuilder();
		for (String str : answerList) {
			answer.append("#");
			answer.append(str);
			answer.append("#");
		}
		return answer.toString();
	}

	private Set<String> getAnswerSet() {
		List<String> answerList = Helper.parseTags(answer);
		HashSet<String> answerSet = new HashSet<String>(answerList);
		return answerSet;
	}

	public Element toElement(Document doc) {
		Element questionElem = null;
		questionElem = doc.createElement("question");

		// set question type as attribute to the root
		Attr typeAttr = doc.createAttribute("type");
		typeAttr.setValue("multi-choice-multi-answer");
		questionElem.setAttributeNode(typeAttr);

		// add question descritpion(query)
		Element query = doc.createElement("query");
		query.appendChild(doc.createTextNode(questionDescription));
		questionElem.appendChild(query);

		// add choices and answers
		List<String> options = Helper.parseTags(choices);
		Set<String> answerSet = getAnswerSet();
		for (int i = 0; i < options.size(); i++) {
			Element option = doc.createElement("option");
			option.appendChild(doc.createTextNode(options.get(i)));
			if (answerSet.contains(options.get(i))) {
				Attr answerAttr = doc.createAttribute("answer");
				answerAttr.setValue("answer");
				option.setAttributeNode(answerAttr);
			}
			questionElem.appendChild(option);
		}

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

	public static String printCreateHtmlSinglePage() {
		StringBuilder html = new StringBuilder();
		html.append("<h4>This page will guide you to create a multiChoice-MultiAnswer question</h4>\n");
		html.append("Please enter proposed question description and answer, and label the right answer. Add and delete button could allow user to customize the number of choices.<br>");
		html.append("<p class='notice'> Notice: one question could have one or more answer.</p>");
		html.append("<p class=\"description\">Question Description:</p>\n");
		html.append("<p><textarea name=\"questionDescription\" rows=\"10\" cols=\"50\" required></textarea></p>\n");
		html.append("<p> Please enter proposed choices, and tick the checkbox if it is one of the answers </p>\n");

		html.append("<div class=\"MCMA_div\">");
		// Choice options and answers
		html.append("<div class=\"choices\">");
		for (int i = 0; i < 4; i++) {
			html.append("<div class=\"combo\">");
			html.append("<span class='option'>Choice "
					+ i
					+ ":</span><input type=\"text\" name=\"choice"
					+ i
					+ "\" required></input><input type=\"checkbox\" name=\"answer\" value=\"choice"
					+ i + "\" ></input>");
			html.append("</div>");
		}
		html.append("</div>"); // div for choices

		// hidden choice option template
		html.append("<div class=\"choice_template\" hidden=\"hidden\">");
		html.append("<span class='option'></span> <input type=\"text\" name=\"choice\" class=\"requiredField\"></input><input type=\"checkbox\" name=\"answer\" value=\"choice\"></input>");
		html.append("</div>");

		// add/delete choices
		html.append("<input type=\"button\" value=\"add\" onclick=\"addMCMAChoice(this);\" />");
		html.append("<input type=\"button\" value=\"delete\" onclick=\"deleteMCMAChoice(this);\" /><br>");

		html.append("<input class=\"numChoices\" type=\"hidden\" name=\"numChoices\" value =\"4\"></input>");
		html.append("</div>"); // div for MCMA_div

		// Full Score
		html.append("Score per correct answer:   <input class=\"max_score\" type=\"text\" name=\"maxScore\" ></input><br>");

		// add timeLimit field
		html.append("<div class=time_limit_div>Time Limit:   ");
		html.append("<input class=\"time_limit\" type=\"text\" name=\"timeLimit\" value=\"0\" ></input><br>");
		html.append("</div>");

		// Hidden information - question Type and tag information

		html.append("<p><input type=\"hidden\" name=\"questionType\" value=\""
				+ QuestionBase.MCMA + "\" ></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"tag\" value=\"not_implemeted\" ></input></p>\n");

		return html.toString();

	}

	@Override
	public String printEditHtml(int suffix) {
		StringBuilder html = new StringBuilder();
		html.append("<h4>This page will guide you to create a multiChoice-MultiAnswer question</h4>\n");
		html.append("Please enter proposed question description and answer, and label the right answer. Add and delete button could allow user to customize the number of choices.<br>");
		html.append("<p class='notice'> Notice: one question could have one or more answer.</p>");
		html.append("<p class=\"description\">Question Description:</p>\n");
		html.append("<textarea name=\"questionDescription_" + suffix
				+ "\"  rows=\"10\" cols=\"50\" required>"
				+ getQuestionDescription() + "</textarea><br>");
		html.append("<p> Please enter proposed choices, and tick the checkbox if it is one of the answers </p>\n");

		html.append("<div class=\"MCMA_div\">");
		// Choice options and answers
		html.append("<div class=\"choices\">");
		List<String> choiceList = Helper.parseTags(choices);
		Set<String> answerSet = getAnswerSet();
		for (int i = 0; i < choiceList.size(); i++) {
			String checked = "";
			if (answerSet.contains(choiceList.get(i)))
				checked = "checked";
			html.append("<div class=\"combo\">");
			html.append("<span class='option'>Choice"
					+ i
					+ ":</span><input type=\"text\" name=\"choice"
					+ i
					+ "_"
					+ suffix
					+ "\""
					+ "value=\""
					+ choiceList.get(i)
					+ "\" required ></input><input type=\"checkbox\" name=\"answer_"
					+ suffix + "\" value=\"choice" + i + "\"" + checked
					+ "></input>");
			html.append("</div>");
		}
		html.append("</div>"); // div for choices

		// hidden choice option template
		html.append("<div class=\"choice_template\" hidden=\"hidden\">");
		html.append("<span class='option'></span> <input type=\"text\" name=\"choice_"
				+ suffix
				+ "\" class=\"requiredField\"></input><input type=\"checkbox\" name=\"answer_"
				+ suffix + "\" value=\"choice\"></input>");
		html.append("</div>");
		// add/delete choices
		html.append("<input type=\"button\" value=\"add\" onclick=\"addMCMAChoice(this);\" />");
		html.append("<input type=\"button\" value=\"delete\" onclick=\"deleteMCMAChoice(this);\" /><br>");

		html.append("<input class=\"numChoices\" type=\"hidden\" name=\"numChoices_"
				+ suffix + "\" value =\"" + choiceList.size() + "\" ></input>");
		html.append("</div>"); // div for MCMA_div

		// Answer and Full Score
		html.append("Score per correct answer:   <input class=\"max_score\" type=\"text\" name=\"maxScore_"
				+ suffix + "\" value=\"" + maxScore + "\"><br>");

		// add timeLimit field
		html.append("<div class=time_limit_div>Time Limit:   ");
		html.append("<input class=\"time_limit\" type=\"text\" name=\"timeLimit_"
				+ suffix
				+ "\" value=\""
				+ getTimeLimit()
				+ "\" required></input><br>");
		html.append("</div>");

		// Hidden information - question Type and tag information

		html.append("<p><input type=\"hidden\" name=\"questionType_" + suffix
				+ "\" value=\"" + QuestionBase.MCMA + "\" ></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"tag_" + suffix
				+ "\" value=\"not_implemeted\" ></input></p>\n");

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

		// create choice options
		String choicesList[] = choices.split("#");
		for (int i = 0; i < choicesList.length; i++) {
			if (choicesList[i].isEmpty()) // remove empty string at head/end
				++i;
			html.append("<p><input type=\"checkbox\" name=\"answer_"
					+ getQuestionId() + "\" value= \"" + choicesList[i] + "\">"
					+ choicesList[i] + "</input></p>\n");
		}

		// Hidden information - questionType and questionId information
		html.append("<p><input id=\"time_limit\" type=\"hidden\" name=\"timeLimit\" value=\""
				+ timeLimit + "\" ></input></p>");

		html.append("<p><input type=\"hidden\" name=\"numChoices_"
				+ getQuestionId() + "\" value=\"" + choicesList.length
				+ "\"></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionType_"
				+ getQuestionId() + "\" value=\"" + getQuestionType()
				+ "\"></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionId_"
				+ getQuestionId() + "\" value=\"" + getQuestionId()
				+ "\" ></input></p>\n");
		html.append("</div>");
		html.append("<div id = \"submit_btn\">");
		html.append("<input type=\"submit\" value = \"Next\"/>");
		html.append("<div id = \"submit_btn\">");
		html.append("</form>\n");
		return html.toString();

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
		// create choice options
		String choicesList[] = choices.split("#");
		for (int i = 0; i < choicesList.length; i++) {
			if (choicesList[i].isEmpty()) // remove empty string at head/end
				++i;
			html.append("<p><input type=\"checkbox\" name=\"answer_"
					+ getQuestionId() + "\" value= \"" + choicesList[i] + "\">"
					+ choicesList[i] + "</input></p>\n");
		}

		// Hidden information - questionType and questionId information
		html.append("<p><input type=\"hidden\" name=\"numChoices_"
				+ getQuestionId() + "\" value=\"4\"></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionType_"
				+ getQuestionId() + "\" value=\"" + getQuestionType()
				+ "\" ></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionId_"
				+ getQuestionId() + "\" value=\"" + getQuestionId()
				+ "\"  ></input></p>\n");

		return html.toString();
	}

}
