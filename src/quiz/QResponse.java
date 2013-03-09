/**
 * 
 */
package quiz;

import javax.servlet.http.HttpServletRequest;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author yang
 * 
 */
public class QResponse extends QuestionBase {

	// TODO: add partial score feature here -> be handed by human grader
	private static final String typeIntro = "In this type of question, given a question, "
			+ "user need to answer the question in the answer area. Correct answer will get full score, "
			+ "while the wrong answer will get zero";

	public QResponse(String questionType, String creatorId, int timeLimit,
			String questionDescription, String answer, int maxScore,
			String tagString, float correctRatio) {
		super(questionType, creatorId, timeLimit, questionDescription, answer,
				maxScore, tagString, correctRatio);
	}

	/**
	 * Constructor for connecting database
	 * 
	 * @param questionId
	 */
	public QResponse(String questionType, String questionId) {
		super(questionType, questionId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.QuestionBase#getQuerySaveString()
	 */
	@Override
	public String getQuerySaveString() {
		return "INSERT INTO " + QR_Table + " VALUES (\""
				+ super.getBaseQuerySaveString() + ")";
	}

	public static String printCreateHtml() {
		// TODO Auto-generated method stub
		StringBuilder html = new StringBuilder();
		html.append("<h1>This page will guide you to create a question-response question</h1>\n");

		html.append("<form action=\"QuizCreationServlet\" method=\"post\" OnSubmit=\"return checkScore()\">");
		html.append("<p> Please enter proposed question description and answer </p>\n");
		html.append("<p class= 'description'>Question Description:</p>\n");
		html.append("<p><textarea name=\"questionDescription\" rows=\"10\" cols=\"50\""
				+ "\" required></textarea></p>\n");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer\""
				+ "\" required></input></p>");
		html.append("<p>Score:   <input type=\"text\" name=\"maxScore\""
				+ "\" required></input></p>");
		html.append("<p>Time Limit:   <input type=\"text\" name=\"timeLimit\" value=\"0\" ></input></p>");

		// Hidden information - questionType and tag information
		html.append("<p><input type=\"hidden\" name=\"questionType\"  value=\""
				+ QuestionBase.QR + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"tag\" value=\"not_implemeted\"></input></p>\n");
		html.append("<input type=\"submit\" value = \"Save\"/></form>");
		return html.toString();
	}

	public static String printCreateOnSamePage() {
		// TODO Auto-generated method stub
		StringBuilder html = new StringBuilder();
		html.append("<h4>Create a question-response question</h4>\n");
		html.append("<p> Please enter proposed question description and answer </p>\n");
		html.append("<p>Question Description: <textarea name=\"questionDescription\" rows=\"10\" cols=\"50\"></textarea></p>\n");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer\" ></input></p>");
		html.append("<p>Score:   <input type=\"text\" name=\"maxScore\" ></input></p>");
		html.append("<p>Time Limit:   <input type=\"text\" name=\"timeLimit\" value=\"0\" ></input></p>");

		// Hidden information - questionType and tag information
		html.append("<p><input type=\"hidden\" name=\"questionType\"  value=\""
				+ QuestionBase.QR + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"tag\" value=\"not_implemeted\"></input></p>\n");

		return html.toString();
	}

	@Override
	public String printReadHtml() {
		StringBuilder html = new StringBuilder();
		html.append(super.printReadHtml());

		html.append("<p>This is a question page, please read the question information, and make an answer</p>");
		html.append("<p>" + typeIntro + "</p>\n");
		html.append("<form action=\"QuestionProcessServlet\" method=\"post\">");
		html.append("<p>Question Description: ");
		html.append(questionDescription + "</p>");
		html.append("<p>Answer:   <input type=\"text\" name=\"answer_"
				+ getQuestionId() + "\" ></input></p>");

		// Hidden information - questionType and questionId information
		// TODO: timeLimit pass to javascript
		html.append("<p>Time Limit:   <input type=\"text\" name=\"timeLimit\" value=\""
				+ timeLimit + "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"questionType_"
				+ getQuestionId() + "\" value=\"" + getQuestionType()
				+ "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"questionId_"
				+ getQuestionId() + "\" value=\"" + getQuestionId()
				+ "\"  ></input></p>");
		html.append("<input type=\"submit\" value = \"Next\"/></form>");

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

		html.append("<p>This is a question page, please read the question information, and make an answer</p>");
		html.append("<p>" + typeIntro + "</p>\n");

		// form action should be here
		html.append("<p>Question Description: ");
		html.append(questionDescription + "</p>");

		// every form field will be renamed as xx_questionId
		html.append("<p>Answer:   <input type=\"text\" name=\"answer_"
				+ getQuestionId() + "\" ></input></p>");

		// Hidden information - questionType and questionId information
		html.append("<p><input type=\"hidden\" name=\"questionType_"
				+ getQuestionId() + "\" value=\"" + getQuestionType()
				+ "\" ></input></p>");
		html.append("<p><input type=\"hidden\" name=\"questionId_"
				+ getQuestionId() + "\" value=\"" + getQuestionId()
				+ "\"  ></input></p>");

		return html.toString();
	}

	public static String getCreatedAnswer(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return request.getParameter("answer");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * quiz.QuestionBase#getUserAnswer(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public String getUserAnswer(HttpServletRequest request) {
		return request.getParameter("answer_" + getQuestionId());
	}

	public Element toElement(Document doc) {
		Element questionElem = null;
		questionElem = doc.createElement("question");

		// set question type as attribute to the root
		Attr typeAttr = doc.createAttribute("type");
		typeAttr.setValue("question-response");
		questionElem.setAttributeNode(typeAttr);

		// set question id as attribute to the root
		// Attr idAttr = doc.createAttribute("id");
		// idAttr.setValue(questionId);
		// questionElem.setAttributeNode(idAttr);

		// add question description(query)
		Element query = doc.createElement("query");
		query.appendChild(doc.createTextNode(questionDescription));
		questionElem.appendChild(query);

		// add answer
		Element answerElem = doc.createElement("answer");
		answerElem.appendChild(doc.createTextNode(answer));
		questionElem.appendChild(answerElem);

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

	public static String printReference() {
		return QuestionBase.printReference();
	}
}
