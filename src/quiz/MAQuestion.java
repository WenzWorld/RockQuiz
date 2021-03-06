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
			ResultSet rs = stmt.executeQuery(queryStmt);
			rs.next();
			tmpIsOrder = rs.getString("is_order");
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
	public MAQuestion(String questionType, String creatorId, int timeLimit,
			String questionDescription, String answer, int maxScore,
			String tagString, float correctRatio, String isOrder) {
		super(questionType, creatorId, timeLimit, questionDescription, answer,
				maxScore, tagString, correctRatio);
		this.isOrder = isOrder;
		// TODO Auto-generated constructor stub
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
		return "INSERT INTO " + MA_Table + " VALUES (\""
				+ super.getBaseQuerySaveString() + ", \"" + isOrder + "\")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getScore(java.lang.String)
	 */
	@Override
	public int getScore(String userInput) {
		List<String> answerList = Helper.parseTags(answer);
		List<String> inputList = Helper.parseTags(userInput);

		if (isOrder.equals("true"))
			return getOrderedScore(answerList, inputList);
		else
			return getUnorderedScore(answerList, inputList);
	}

	private int getOrderedScore(List<String> answerList, List<String> inputList) {
		int score = 0;
		for (int i = 0; i < answerList.size(); i++) {
			if (inputList.get(i).equals(answerList.get(i)))
				score += maxScore;
		}
		return score;
	}

	private int getUnorderedScore(List<String> answerList,
			List<String> inputList) {
		int score = 0;
		HashSet<String> answerSet = new HashSet<String>();
		for (String str : answerList) {
			answerSet.add(str);
		}
		for (String str : inputList) {
			if (answerSet.contains(str))
				score += maxScore;
		}
		return score;
	}

	public static String getCreatedAnswer(HttpServletRequest request, int suffix) {
		int numAnswers = Integer.parseInt(request.getParameter("numAnswers"
				+ "_" + suffix));
		StringBuilder answer = new StringBuilder();
		for (int i = 0; i < numAnswers; i++) {
			answer.append("#");
			// if there is no input in answer field, it should be null
			answer.append(request.getParameter("answer" + i + "_" + suffix));
			answer.append("#");
		}
		return answer.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * quiz.QuestionBase#getUserAnswer(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public String getUserAnswer(HttpServletRequest request) {
		int numAnswers = Integer.parseInt(request.getParameter("numAnswers_"
				+ getQuestionId()));
		StringBuilder answer = new StringBuilder();
		for (int i = 0; i < numAnswers; i++) {
			answer.append("#");
			// if there is no input in answer field, it should be null
			String userAnswer = request.getParameter("answer" + i + "_"
					+ getQuestionId());
			if (userAnswer == null || userAnswer == "")
				userAnswer = " ";

			answer.append(userAnswer);
			answer.append("#");
		}
		return answer.toString();
	}

	public Element toElement(Document doc) {
		Element questionElem = null;
		questionElem = doc.createElement("question");

		// set question type as attribute to the root
		Attr typeAttr = doc.createAttribute("type");
		typeAttr.setValue("multi-answer");
		questionElem.setAttributeNode(typeAttr);

		// add question descritpion(query)
		Element query = doc.createElement("query");
		query.appendChild(doc.createTextNode(questionDescription));
		questionElem.appendChild(query);

		// add answers
		List<String> answers = Helper.parseTags(answer);
		for (int i = 0; i < answers.size(); i++) {
			Element option = doc.createElement("answer");
			option.appendChild(doc.createTextNode(answers.get(i)));
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
		html.append("<h4>This page will guide you to create a multi-answer question</h4>");
		html.append("Please enter proposed question description and answer. Every Multi-Answer question could contain more than one answer.<br>");
		html.append("Creator could also specify whether the answer should be answered in order or not by ticking the corresponding checkbox");
		html.append("<p class='notice'> Notice: Creator should also specify the score per correct answer.</p>");
		html.append("<span class= 'description'>Question Description:</span><br>");
		html.append("<textarea name=\"questionDescription\" rows=\"10\" cols=\"50\""
				+ "\" required></textarea>");

		html.append("<div class=\"MA_div\">");
		// answers
		html.append("<div class=\"answers\">");
		html.append("<div class=\"combo\">");
		html.append("<span class='option'>Answer0: </span><input type=\"text\" name=\"answer0\""
				+ "\" required></input><br>");
		html.append("</div>"); // div for combo
		html.append("</div>"); // div for answers

		// hidden answer option template
		html.append("<div class=\"answer_template\" hidden=\"hidden\">");
		html.append("<span class='option'></span><input type=\"text\" name=\"answer\""
				+ " class=\"requiredField\"></input><br>");
		html.append("</div>");

		// add, delete button
		html.append("<input type=\"button\" value=\"add\" onclick=\"addAnswer(this);\" />");
		html.append("<input type=\"button\" value=\"delete\" onclick=\"deleteAnswer(this);\" /><br>");

		html.append("<input class=\"numAnswers\" type=\"hidden\" name=\"numAnswers\" value =\"1\"></input>");
		html.append("</div>"); // div for MA_div

		html.append("Score per correct answer: <input class=\"max_score\" type=\"text\" name=\"maxScore\""
				+ "\" required></input><br>\n");

		// add timeLimit field
		html.append("<div class=time_limit_div>Time Limit:   ");
		html.append("<input class=\"time_limit\" type=\"text\" name=\"timeLimit\" value=\"0\" ></input><br>");
		html.append("</div>");

		// checkbox: tick means true, otherwise null means false
		html.append("<input type=\"checkbox\" name=\"isOrder\" value=\"true\">isOrder</input><br>");

		// Hidden information - questionType,tag and number of answers
		html.append("<p><input type=\"hidden\" name=\"questionType\"  value=\""
				+ QuestionBase.MA + "\" ></input></p>\n");

		html.append("<p><input type=\"hidden\" name=\"tag\" value=\"not_implemeted\"></input></p>\n");

		return html.toString();
	}

	public String printEditHtml(int suffix) {
		StringBuilder html = new StringBuilder();
		html.append("<h4>Multi-answer Question</h4>");
		html.append("Please enter proposed question description and answer. Every Multi-Answer question could contain more than one answer.<br>");
		html.append("Creator could also specify whether the answer should be answered in order or not by ticking the corresponding checkbox");
		html.append("<p class='notice'> Notice: Creator should also specify the score per correct answer.</p>");
		html.append("<span class= 'description'>Question Description:</span><br>");
		html.append("<textarea name=\"questionDescription_" + suffix
				+ "\"  rows=\"10\" cols=\"50\" required>"
				+ getQuestionDescription() + "</textarea><br>");

		html.append("<div class=\"MA_div\">");
		// answers
		html.append("<div class=\"answers\">");
		html.append("<div class=\"combo\">");
		List<String> answerList = Helper.parseTags(answer);
		for (int i = 0; i < answerList.size(); i++) {
			html.append("<span class='option'>Answer" + i
					+ "<input type=\"text\" name=\"answer" + i + "_" + suffix
					+ "\" value=\"" + answerList.get(i) + "\" required><br>\n");
		}
		html.append("</div>"); // div for combo
		html.append("</div>"); // div for answers

		// hidden answer option template
		html.append("<div class=\"answer_template\" hidden=\"hidden\">");
		html.append("<span class='option'></span><input type=\"text\" name=\"answer_"
				+ suffix + "\" class=\"requiredField\"></input><br>");
		html.append("</div>");

		// add, delete button
		html.append("<input type=\"button\" value=\"add\" onclick=\"addAnswer(this);\" />");
		html.append("<input type=\"button\" value=\"delete\" onclick=\"deleteAnswer(this);\" /><br>");

		html.append("<input class=\"numAnswers\" type=\"hidden\" name=\"numAnswers_"
				+ suffix + "\" value =\"" + answerList.size() + "\" ></input>");
		html.append("</div>"); // div for MA_div

		html.append("Score per correct answer:   <input class=\"max_score\" type=\"text\" name=\"maxScore_"
				+ suffix + "\" value=\"" + maxScore + "\" required><br>");

		// add timeLimit field
		html.append("<div class=time_limit_div>Time Limit:   ");
		html.append("<input class=\"time_limit\" type=\"text\" name=\"timeLimit_"
				+ suffix
				+ "\" value=\""
				+ getTimeLimit()
				+ "\" required></input><br>");
		html.append("</div>");

		// checkbox: tick means true, otherwise null means false
		String checked = "";
		if (isOrder.equals("true"))
			checked = "checked";
		html.append("<input type=\"checkbox\" name=\"isOrder_" + suffix
				+ "\" value=\"true\"" + checked + ">isOrder</input><br>");

		// Hidden information - questionType,tag and number of answers
		html.append("<p><input type=\"hidden\" name=\"questionType_" + suffix
				+ "\" value=\"" + QuestionBase.MA + "\" ></input></p>\n");

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

		List<String> answerList = Helper.parseTags(answer);
		for (int i = 0; i < answerList.size(); i++) {
			html.append("<p><input type=\"text\" name=\"answer" + i + "_"
					+ getQuestionId() + "\"></input></p>\n");
		}

		// Hidden information - questionType and questionId information
		html.append("<p><input id=\"time_limit\" type=\"hidden\" name=\"timeLimit\" value=\""
				+ timeLimit + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"numAnswers_"
				+ getQuestionId() + "\" value=\"" + answerList.size()
				+ "\"></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionType_"
				+ getQuestionId() + "\" value=\"" + getQuestionType()
				+ "\" ></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionId_"
				+ getQuestionId() + "\" value=\"" + getQuestionId()
				+ "\"  ></input></p>\n");
		html.append("</div>");
		html.append("<input type=\"submit\" value = \"Next\"/></form>\n");

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

		// TODO: use javascript to dynamically generate multi-answer field
		// TODO: use javascript to dinamically check repeated same answers
		html.append("<p>Answer:   <input type=\"text\" name=\"answer0_"
				+ getQuestionId() + "\" ></input></p>\n");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer1_"
				+ getQuestionId() + "\" ></input></p>\n");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer2_"
				+ getQuestionId() + "\" ></input></p>\n");

		// Hidden information - questionType and questionId information
		html.append("<p><input type=\"hidden\" name=\"numAnswers_"
				+ getQuestionId() + "\" value=\"3\"></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionType_"
				+ getQuestionId() + "\" value=\"" + getQuestionType()
				+ "\" ></input></p>\n");
		html.append("<p><input type=\"hidden\" name=\"questionId_"
				+ getQuestionId() + "\" value=\"" + getQuestionId()
				+ "\"  ></input></p>\n");

		return html.toString();
	}

}
