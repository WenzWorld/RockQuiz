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

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import util.Helper;

/**
 * @author yang
 * 
 */
public class Matching extends MCMAQuestion {
	private static final String typeIntro = "Matching question: user should try to match every option and answer"
			+ "Correct answer will get positive points, while the wrong answer will get negative points";

	/**
	 * @param questionType
	 * @param creatorId
	 * @param timeLimit
	 * @param questionDescription
	 * @param answer
	 * @param maxScore
	 * @param tagString
	 * @param correctRation
	 * @param choices
	 */
	public Matching(String questionType, String creatorId, int timeLimit,
			String questionDescription, String answer, int maxScore,
			String tagString, float correctRation, String choices) {
		super(questionType, creatorId, timeLimit, questionDescription, answer,
				maxScore, tagString, correctRation, choices);
		// this.choices = choices;
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param questionType
	 * @param questionId
	 */
	public Matching(String questionType, String questionId) {
		super(questionType, questionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getQuerySaveString()
	 */
	@Override
	public String getQuerySaveString() {
		return "INSERT INTO " + MATCH_Table + " VALUES (\""
				+ super.getBaseQuerySaveString() + ", \"" + choices + "\")";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getMaxScore()
	 */
	@Override
	public int getMaxScore() {
		// TODO if allow expanding matching option, should change this hard code
		// "4"
		return maxScore * 4;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getScore(java.lang.String)
	 */
	@Override
	public int getScore(String userInput) {
		List<String> inputList = Helper.parseTags(userInput);
		List<String> solutionList = Helper.parseTags(answer);
		int score = 0;
		for (int i = 0; i < solutionList.size(); i++) {
			if (inputList.get(i).equals(solutionList.get(i)))
				score += maxScore;
		}
		return score;
	}

	@Override
	public String getUserAnswer(HttpServletRequest request) {
		int numAnswers = Integer.parseInt(request.getParameter("numChoices_"
				+ getQuestionId()));
		StringBuilder answer = new StringBuilder();
		for (int i = 0; i < numAnswers; i++) {
			answer.append("#");
			String userAnswer = request.getParameter("choice" + i + "_"
					+ getQuestionId());
			if (userAnswer == null || userAnswer == "")
				userAnswer = " ";
			answer.append(userAnswer);
			answer.append("#");
		}
		return answer.toString();
	}

	public static String getCreatedAnswer(HttpServletRequest request, int suffix) {
		int numAnswers = Integer.parseInt(request.getParameter("numChoices"
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

	public static String printCreateHtmlSinglePage() {
		StringBuilder html = new StringBuilder();
		html.append("<h4>This page will guide you to create a Matching question</h4>");
		html.append("<p> Please enter proposed question description and answer </p>");
		html.append("<p class='notice'> Notice: For RockQuiz -alpha0.0, only one matching problem is allowed in \"one-page\" quiz creation.<br>");
		html.append("Also, the matching question currently only supports 4 choices and answers.</p>");
		html.append("<p class=\"description\">Question Description:</p>\n");
		html.append("<textarea name=\"questionDescription\" rows=\"10\" cols=\"50\" required></textarea>");

		html.append("<div class=\"Match_div\">");

		// Choice options
		html.append("<div class=\"choices\">");
		for (int i = 0; i < 4; i++) {
			html.append("<div class=\"combo\">");
			html.append("<span class='option'>Choice" + i + " & Answer" + i
					+ ": </span><input type=\"text\" name=\"choice" + i
					+ "\" required></input><input type=\"text\" name=\"answer"
					+ i + "\" required></input>");
			html.append("</div>");
		}
		html.append("</div>"); // for choices div

		html.append("<input class=\"numChoices\" type=\"hidden\" name=\"numChoices\" value =\"4\" ></input>");
		html.append("</div>"); // for Match_div

		// Full Score
		html.append("Score per answer:   <input class=\"max_score\"  type=\"text\" name=\"maxScore\" required></input>");

		// add timeLimit field
		html.append("<div class=time_limit_div>Time Limit:   ");
		html.append("<input class=\"time_limit\" type=\"text\" name=\"timeLimit\" value=\"0\" ></input><br>");
		html.append("</div>");

		// Hidden information - question Type and tag information
		html.append("<p><input type=\"hidden\" name=\"questionType\" value=\""
				+ QuestionBase.MATCH + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"tag\" value=\"not_implemeted\" ></input></p>");

		return html.toString();

	}

	public String printEditHtml(int suffix) {
		StringBuilder html = new StringBuilder();
		html.append("<h4>This page will guide you to create a Matching question</h4>");
		html.append("<p> Please enter proposed question description and answer </p>");
		html.append("<p class='notice'> Notice: For RockQuiz -alpha0.0, only one matching problem is allowed in \"one-page\" quiz creation.<br>");
		html.append("Also, the matching question currently only supports 4 choices and answers.</p>");
		html.append("<p class=\"description\">Question Description:</p>\n");
		html.append("<textarea name=\"questionDescription_" + suffix
				+ "\"  rows=\"10\" cols=\"50\" required>"
				+ getQuestionDescription() + "</textarea><br>");

		html.append("<div class=\"Match_div\">");

		// Choice options
		html.append("<div class=\"choices\">");
		List<String> choiceList = Helper.parseTags(choices);
		List<String> answerList = Helper.parseTags(answer);
		for (int i = 0; i < choiceList.size(); i++) {
			html.append("<div class=\"combo\">");
			html.append("<span class='option'>Choice" + i + " & Answer" + i
					+ ": </span><input type=\"text\" name=\"choice" + i + "_"
					+ suffix + "\" value=\"" + choiceList.get(i)
					+ "\" required></input><input type=\"text\" name=\"answer"
					+ i + "_" + suffix + "\" value=\"" + answerList.get(i)
					+ "\" required></input>");
			html.append("</div>");
		}
		html.append("</div>"); // for choices div

		html.append("<input class=\"numChoices\" type=\"hidden\" name=\"numChoices_"
				+ suffix + "\" value =\"" + choiceList.size() + "\" ></input>");
		html.append("</div>"); //
		html.append("</div>"); // for Match_div

		// Full Score
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

		// Hidden information - question Type and tag information
		html.append("<p><input type=\"hidden\" name=\"questionType_" + suffix
				+ "\" value=\"" + QuestionBase.MATCH + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"tag_" + suffix
				+ "\" value=\"not_implemeted\" ></input></p>\n");

		return html.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.MultiChoice#printReadHtml()
	 */
	@Override
	public String printReadHtml() {
		StringBuilder html = new StringBuilder();

		html.append("<p class='creator'>Question Creator: " + creatorId
				+ "</p>\n");
		html.append("<h2>Question Type Introduction</h2>\n");
		html.append("<p>" + typeIntro + "</p>\n");
		html.append("<form action=\"QuestionProcessServlet\" method=\"post\" id=\"questionRead\">");

		// question description
		html.append("<div class=\"question\">");
		html.append("<div class='description'>Question Description:</div>");
		html.append("<p>");
		html.append(questionDescription);
		html.append("</p>");

		html.append("<div id=\"match\"></div>");
		html.append("<div id=\"results\"></div>");
		// create choice options
		List<String> choiceList = Helper.parseTags(choices);
		for (int i = 0; i < choiceList.size(); i++) {
			String choiceSpanId = "cs" + i;
			String choiceId = "choice" + i;
			String choiceName = "choice" + i + "_" + getQuestionId();
			html.append("<span id=" + "'" + choiceSpanId
					+ "' hidden=\"hidden\">" + "<input id=" + "'" + choiceId
					+ "' type=\"hidden\" name=\"" + choiceName + "\"></input>"
					+ choiceList.get(i) + "</span>");
		}

		// create answer options
		List<String> answerList = Helper.parseTags(answer);
		Collections.shuffle(answerList);
		for (int i = 0; i < answerList.size(); i++) {
			String answerSpanId = "as" + i;
			String answerId = "answer" + i;
			String answerName = "answer" + i + "_" + getQuestionId();
			html.append("<span id=" + "'" + answerSpanId
					+ "' hidden=\"hidden\">" + "<input id=" + "'" + answerId
					+ "' type=\"hidden\" name=\"" + answerName + "\"></input>"
					+ answerList.get(i) + "</span>");
		}

		// Hidden information - questionType and questionId information
		html.append("<p><input id=\"time_limit\" type=\"hidden\" name=\"timeLimit\" value=\""
				+ timeLimit + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"numChoices_"
				+ getQuestionId() + "\"  value=\"" + answerList.size()
				+ "\"></input></p>\n");
		html.append("<input type=\"hidden\" name=\"questionType_"
				+ getQuestionId() + "\" value=\"" + getQuestionType()
				+ "\"></input>");
		html.append("<input type=\"hidden\" name=\"questionId_"
				+ getQuestionId() + "\" value=\"" + getQuestionId()
				+ "\" ></input>");
		html.append("</div>");

		html.append("<input class=\"submit_btn\" id=\"result\" type=\"submit\" value = \"Next\"/>");
		html.append("</form>");
		html.append("<div class=\"clear_btn\">");
		html.append("<button id=\"clear\">Clear</button>");
		html.append("</div>");
		return html.toString();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.MultiChoice#printReadHtmlForSingle()
	 */
	@Override
	public String printReadHtmlForSingle() {
		// TODO Auto-generated method stub
		return super.printReadHtmlForSingle();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.MultiChoice#toElement(org.w3c.dom.Document)
	 */
	@Override
	public Element toElement(Document doc) {
		// TODO Auto-generated method stub
		return super.toElement(doc);
	}

}
