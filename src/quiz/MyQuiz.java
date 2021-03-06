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
package quiz;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import util.Helper;
import database.MyDB;

public class MyQuiz implements Quiz {

	public final class QuizEvent {
		private final String quizId;
		private final String userName;
		private final Timestamp submitTime;
		private final long timeElapsed;
		private final int score;

		private QuizEvent(String quizId, String userName, Timestamp submitTime,
				long timeElapsed, int score) {
			super();
			this.quizId = quizId;
			this.userName = userName;
			this.submitTime = submitTime;
			this.timeElapsed = timeElapsed;
			this.score = score;
		}

		private QuizEvent(String quizId) {
			// below declare a set of non-final variable, which is a hack to get
			// around the exception issue
			String userName = "error";
			Timestamp submitTime = new Timestamp(0);
			long timeElapsed = 0;
			int score = 0;

			Connection con = MyDB.getConnection();
			try {
				Statement stmt = con.createStatement();
				// query quizName_Event_Table
				ResultSet rs = stmt.executeQuery("SELECT * FROM " + quizName
						+ "_Event_Table WHERE quizId = \"" + quizId + "\"");
				rs.next();
				userName = rs.getString("userName");
				submitTime = rs.getTimestamp("submitTime");
				timeElapsed = rs.getLong("timeElapsed");
				score = rs.getInt("score");

			} catch (SQLException e) {
				e.printStackTrace();
			}
			// do the real initialization now
			this.quizId = quizId;
			this.userName = userName;
			this.submitTime = submitTime;
			this.timeElapsed = timeElapsed;
			this.score = score;
		}

		private void saveToDatabase() {
			Connection con = MyDB.getConnection();
			try {
				Statement stmt = con.createStatement();
				// DEBUG: check if QuizId is already in database
				ResultSet rs = stmt.executeQuery("SELECT * FROM " + quizName
						+ "_Event_Table WHERE quizId = \"" + quizId + "\"");
				assert rs.isBeforeFirst() : "ERROR: quizId = " + quizId
						+ " is already in " + quizName + "_Event_Table";

				// update quizName_Event_Table -- insert a row
				String contentRow = "\"" + getQuizId() + "\",\""
						+ getUserName() + "\",\"" + getSubmitTime() + "\", "
						+ getTimeElapsed() + ", " + getScore();
				stmt.executeUpdate("INSERT INTO " + quizName
						+ "_Event_Table VALUES (" + contentRow + ")");
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		public final String getQuizId() {
			return quizId;
		}

		public final String getUserName() {
			return userName;
		}

		public final Timestamp getSubmitTime() {
			return submitTime;
		}

		public final long getTimeElapsed() {
			return timeElapsed;
		}

		public final int getScore() {
			return score;
		}

	}

	private final String quizName;
	private final String creatorId;
	private final Timestamp createTime;
	private final int totalScore;
	private String quizDescription;
	private List<String> tags;
	private boolean canPractice;
	private boolean isRandom;
	private boolean isOnePage;
	private boolean isImmCorrection; // jvm optimization
	private List<QuestionBase> questionList;
	private String category;

	private static final String CREATECONTENTTABLEPARAMS = "questionNum CHAR(32), "
			+ "questionType CHAR(32), " + "questionId CHAR(32)";

	private static final String CREATEEVENTTABLEPARAMS = "quizId CHAR(32), "
			+ "userName CHAR(32), " + "submitTime TIMESTAMP, "
			+ "timeElapsed BIGINT, " + "score INT ";

	public MyQuiz(String quizName, String creatorId, String quizDescription,
			List<String> tags, boolean canPractice, boolean isRandom,
			boolean isOnePage, boolean isImmCorrection,
			List<QuestionBase> questionList, Timestamp createTime,
			String category) {
		super();
		this.quizName = quizName;
		this.creatorId = creatorId;
		this.quizDescription = quizDescription;
		this.tags = tags;
		this.canPractice = canPractice;
		this.isRandom = isRandom;
		this.isOnePage = isOnePage;
		this.isImmCorrection = isImmCorrection;
		this.questionList = questionList;
		this.createTime = createTime;
		int totalScore = 0;
		for (QuestionBase q : questionList) {
			totalScore += q.getMaxScore();
		}
		this.totalScore = totalScore;
		this.category = category;
	}

	public MyQuiz(String quizName) {
		// below declare a set of non-final variables, which is a hack to get
		// around the exception issue
		String creatorId = "error";
		int totalScore = 0;
		String quizDescription = "error";
		List<String> tags = new ArrayList<String>();
		boolean canPractice = false;
		boolean isRandom = false;
		boolean isOnePage = false;
		boolean isImmCorrection = false;
		List<QuestionBase> questionList = new ArrayList<QuestionBase>();
		Timestamp createTime = new Timestamp(0);
		String category = "Not_Implemented_Category";

		Connection con = MyDB.getConnection();
		try {
			Statement stmt = con.createStatement();
			// query Global_Quiz_Info_Table
			ResultSet rs = stmt
					.executeQuery("SELECT * FROM Global_Quiz_Info_Table WHERE quizName = \""
							+ quizName + "\"");
			rs.next();
			creatorId = rs.getString("creatorId");
			quizDescription = rs.getString("quizDescription");
			canPractice = rs.getBoolean("canPractice");
			isRandom = rs.getBoolean("isRandom");
			isOnePage = rs.getBoolean("isOnePage");
			isImmCorrection = rs.getBoolean("isImmediateCorrection");
			String tagString = rs.getString("tagString");
			tags = Helper.parseTags(tagString);
			createTime = rs.getTimestamp("createTime");
			category = rs.getString("category");

			// query quizName_Content_Table
			int score = 0;
			rs = stmt.executeQuery("SELECT * FROM " + quizName
					+ "_Content_Table");
			while (rs.next()) {
				String questionType = rs.getString("questionType");
				String questionId = rs.getString("questionId");
				QuestionBase question = QuestionFactory.getQuestion(
						questionType, questionId);
				if (question != null)
					score += question.getMaxScore();
				questionList.add(question);
			}
			totalScore = score;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// do the real initialization now
		this.quizName = quizName;
		this.creatorId = creatorId;
		this.totalScore = totalScore;
		this.quizDescription = quizDescription;
		this.tags = tags;
		this.canPractice = canPractice;
		this.isRandom = isRandom;
		this.isOnePage = isOnePage;
		this.isImmCorrection = isImmCorrection;
		this.questionList = questionList;
		this.createTime = createTime;
		this.category = category;
	}

	public void edit(String quizDescription, List<String> tags,
			boolean canPractice, boolean isRandom, boolean isOnePage,
			boolean isImmCorrection, List<QuestionBase> questionList,
			String category) {
		this.quizDescription = quizDescription;
		this.tags = tags;
		this.canPractice = canPractice;
		this.isRandom = isRandom;
		this.isOnePage = isOnePage;
		this.isImmCorrection = isImmCorrection;
		this.questionList = questionList;
		this.category = category;
	}

	public void saveToDatabase() {
		Connection con = MyDB.getConnection();
		try {
			Statement stmt = con.createStatement();
			// update Global_Quiz_Info_Table -- insert a row
			String quizRow = "\"" + quizName + "\",\"" + creatorId + "\",\""
					+ quizDescription + "\",\"" + Helper.generateTags(tags)
					+ "\"," + canPractice + ", " + isRandom + ", " + isOnePage
					+ ", " + isImmCorrection + ", \"" + createTime + "\",\""
					+ category + "\"";
			stmt.executeUpdate("INSERT INTO Global_Quiz_Info_Table VALUES("
					+ quizRow + ")");

			// create quizName_Content_Table
			stmt.executeUpdate("DROP TABLE IF EXISTS " + quizName
					+ "_Content_Table");
			stmt.executeUpdate("CREATE TABLE " + quizName + "_Content_Table ( "
					+ CREATECONTENTTABLEPARAMS + ")");
			// populate quizName_Content_Table
			for (int i = 0; i < questionList.size(); i++) {
				QuestionBase q = questionList.get(i);
				String contentRow = "\"" + i + "\",\"" + q.getQuestionType()
						+ "\",\"" + q.getQuestionId() + "\"";
				stmt.executeUpdate("INSERT INTO " + quizName
						+ "_Content_Table VALUES(" + contentRow + ")");
			}

			// create quizName_Event_Table
			stmt.executeUpdate("DROP TABLE IF EXISTS " + quizName
					+ "_Event_Table");
			stmt.executeUpdate("CREATE TABLE " + quizName + "_Event_Table ( "
					+ CREATEEVENTTABLEPARAMS + ")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void updateDatabase() {
		Connection con = MyDB.getConnection();
		try {
			Statement stmt = con.createStatement();
			// update Global_Quiz_Info_Table -- update a row
			// hack here: delete and add again
			String quizRow = "\"" + quizName + "\",\"" + creatorId + "\",\""
					+ quizDescription + "\",\"" + Helper.generateTags(tags)
					+ "\"," + canPractice + ", " + isRandom + ", " + isOnePage
					+ ", " + isImmCorrection + ", \"" + createTime + "\",\""
					+ category + "\"";
			stmt.executeUpdate("DELETE FROM Global_Quiz_Info_Table WHERE quizName = \""
					+ quizName + "\"");
			stmt.executeUpdate("INSERT INTO Global_Quiz_Info_Table VALUES("
					+ quizRow + ")");

			// create quizName_Content_Table
			stmt.executeUpdate("DROP TABLE IF EXISTS " + quizName
					+ "_Content_Table");
			stmt.executeUpdate("CREATE TABLE " + quizName + "_Content_Table ( "
					+ CREATECONTENTTABLEPARAMS + ")");
			// populate quizName_Content_Table
			for (int i = 0; i < questionList.size(); i++) {
				QuestionBase q = questionList.get(i);
				String contentRow = "\"" + i + "\",\"" + q.getQuestionType()
						+ "\",\"" + q.getQuestionId() + "\"";
				stmt.executeUpdate("INSERT INTO " + quizName
						+ "_Content_Table VALUES(" + contentRow + ")");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void shuffleQuestionListAndSave() {
		Connection con = MyDB.getConnection();
		try {
			Statement stmt = con.createStatement();
			// delete all rows from quizName_Content_Table
			stmt.executeUpdate("DELETE FROM " + quizName + "_Content_Table");
			// shuffle current question list
			Collections.shuffle(questionList);
			// populate quizName_Content_Table
			// Yang: why need save to database every time? ? can't it be saved
			// to session?
			// Jing: it is a hack to save work
			for (int i = 0; i < questionList.size(); i++) {
				QuestionBase q = questionList.get(i);
				String contentRow = "\"" + i + "\",\"" + q.getQuestionType()
						+ "\",\"" + q.getQuestionId() + "\"";
				stmt.executeUpdate("INSERT INTO " + quizName
						+ "_Content_Table VALUES(" + contentRow + ")");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getSummaryPage() {
		return "quiz_summary.jsp?quizName=" + quizName;
	}

	/**
	 * 
	 * @return String of the hyper link to start a quiz
	 */
	public String getQuizStartPage() {
		return "QuestionProcessServlet?quizName=" + quizName;
	}

	/**
	 * 
	 * @return String of the hyper link to edit a quiz
	 */
	public String getQuizEditPage() {
		return "quiz_edit.jsp?quizName=" + quizName;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public int getTotalScore() {
		return totalScore;
	}

	public String getQuizDescription() {
		return quizDescription;
	}

	public List<String> getTags() {
		return tags;
	}

	public boolean isCanPractice() {
		return canPractice;
	}

	public boolean isRandom() {
		return isRandom;
	}

	public boolean isOnePage() {
		return isOnePage;
	}

	public boolean isImmCorrection() {
		return isImmCorrection;
	}

	public boolean containsQuizEvent(String quizId) {
		// below declare a set of non-final variable, which is a hack to get
		// around the exception issue

		Connection con = MyDB.getConnection();
		try {
			Statement stmt = con.createStatement();
			// query quizName_Event_Table
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + quizName
					+ "_Event_Table WHERE quizId = \"" + quizId + "\"");
			if (rs.isBeforeFirst())
				return true;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String getScore(String quizId) {
		if (containsQuizEvent(quizId)) {
		QuizEvent event = new QuizEvent(quizId);
		return "" + event.getScore();
		} else {
			return null;
		}
	}

	@Override
	public String getCreatorId() {
		return creatorId;
	}

	@Override
	public String getMaxScore() {
		return "" + totalScore;
	}

	@Override
	public String getQuizName() {
		return Helper.replaceUnderscore(quizName);
	}

	public List<QuestionBase> getQuestionList() {
		return questionList;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	@Override
	public String getTimeElapsed(String quizId) {
		if (containsQuizEvent(quizId)) {
			QuizEvent event = new QuizEvent(quizId);
			long timeElapsed = event.getTimeElapsed();
			return String.format(
					"%d:%d",
					TimeUnit.MILLISECONDS.toMinutes(timeElapsed),
					TimeUnit.MILLISECONDS.toSeconds(timeElapsed)
							- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS
									.toMinutes(timeElapsed)));
		} else
			return null;
	}

	/**
	 * Returns List of at most num of QuizEvents ordered by the scores
	 * 
	 * @param num
	 *            The number of events returned
	 * @return List of QuizEvent
	 */
	public List<QuizEvent> highScoreEvents(int num) {
		Connection con = MyDB.getConnection();
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
			// query quizExample0_Event_Table
			rs = stmt.executeQuery("SELECT quizId FROM " + quizName
					+ "_Event_Table " + "ORDER BY score DESC, timeElapsed ASC "
					+ "LIMIT 0," + num);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return getQuizEventList(rs);
	}

	/**
	 * Returns List of at most num of QuizEvents submitted within a day and
	 * ordered by the scores
	 * 
	 * @param num
	 *            The number of events returned
	 * @return List of QuizEvent
	 */
	public List<QuizEvent> highScoreLastDayEvents(int num) {
		Connection con = MyDB.getConnection();
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
			// query quizExample0_Event_Table
			rs = stmt
					.executeQuery("SELECT quizId FROM "
							+ quizName
							+ "_Event_Table "
							+ "WHERE submitTime >= cast((now() - interval 1 day) as date) "
							+ "ORDER BY score DESC, timeElapsed ASC "
							+ "LIMIT 0," + num);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return getQuizEventList(rs);
	}

	/**
	 * Returns List of at most num of recent taken QuizEvents
	 * 
	 * @param num
	 *            The number of events returned
	 * @return List of QuizEvent
	 */
	public List<QuizEvent> recentTakenEvents(int num) {

		Connection con = MyDB.getConnection();
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
			// query quizExample0_Event_Table
			rs = stmt.executeQuery("SELECT quizId FROM " + quizName
					+ "_Event_Table " + "ORDER BY submitTime DESC "
					+ "LIMIT 0," + num);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return getQuizEventList(rs);
	}

	private List<QuizEvent> allEvents() {
		Connection con = MyDB.getConnection();
		ResultSet rs = null;
		try {
			Statement stmt = con.createStatement();
			// query quizExample0_Event_Table
			rs = stmt.executeQuery("SELECT quizId FROM " + quizName
					+ "_Event_Table");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return getQuizEventList(rs);
	}

	/**
	 * Parse from SQL resultsSet to a List of QuizEvent
	 * 
	 * @param rs
	 *            SQL ResultSet from a query to quizName_Event_Table. It must
	 *            have the column of 'quizId'
	 * @return List of QuizEvent
	 */
	private List<QuizEvent> getQuizEventList(ResultSet rs) {
		List<QuizEvent> events = new ArrayList<MyQuiz.QuizEvent>();
		try {
			while (rs.next()) {
				String quizId = rs.getString("quizId");
				QuizEvent event = new QuizEvent(quizId);
				events.add(event);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return events;
	}

	public int getTakenTimes() {
		int num = 0;
		Connection con = MyDB.getConnection();
		try {
			Statement stmt = con.createStatement();
			// query quizName_Event_Table
			ResultSet rs = stmt.executeQuery("SELECT quizId FROM " + quizName
					+ "_Event_Table");
			// get the number of rows
			rs.last();
			num = rs.getRow();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return num;
	}

	private String generateId() {
		// get the first hash
		String quizId = Helper.getMD5ForTime();

		Connection con = MyDB.getConnection();
		try {
			Statement stmt = con.createStatement();
			// query quizName_Event_Table
			ResultSet rs = stmt.executeQuery("SELECT * FROM " + quizName
					+ "_Event_Table WHERE quizId = \"" + quizId + "\"");
			// try another hash until it is not used already
			while (rs.isBeforeFirst()) {
				quizId = Helper.getMD5ForTime();
				rs = stmt.executeQuery("SELECT * FROM " + quizName
						+ "_Event_Table WHERE quizId = \"" + quizId + "\"");
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return quizId;
	}

	/**
	 * Constructs a QuizEvent from the input and save it to database, and
	 * returns QuizId String
	 * 
	 * @param userName
	 *            name string of the user who takes the quiz
	 * @param timeElapsed
	 *            the time elapsed for the user to take the quiz
	 * @param score
	 *            the score get it by the user
	 * 
	 * @return QuizId String
	 * 
	 */
	public String saveQuizEvent(String userName, long timeElapsed, int score) {
		Timestamp submitTime = new Timestamp(new java.util.Date().getTime());
		String quizId = generateId();
		QuizEvent event = new QuizEvent(quizId, userName, submitTime,
				timeElapsed, score);
		event.saveToDatabase();
		return quizId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see quiz.Quiz#clearQuizEvent()
	 */
	@Override
	public void clearQuizEvents() {
		Connection con = MyDB.getConnection();
		try {
			Statement stmt = con.createStatement();
			// delete all rows from quizName_Event_Table
			stmt.executeUpdate("DELETE FROM " + quizName + "_Event_Table");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public int getBestScore() {
		List<QuizEvent> list = allEvents();
		int bestScore = 0;
		for (int i = 0; i < list.size(); i++) {
			int curScore = list.get(i).getScore();
			if (bestScore < curScore)
				bestScore = curScore;
		}
		assert bestScore <= getTotalScore();
		return bestScore;
	}

	public int getLowestScore() {
		if (getTakenTimes() > 0) {
		List<QuizEvent> list = allEvents();
			int LowScore = Integer.parseInt(getMaxScore());
		for (int i = 0; i < list.size(); i++) {
			int curScore = list.get(i).getScore();
			if (LowScore > curScore)
				LowScore = curScore;
		}
		assert LowScore <= getTotalScore();
		return LowScore;
		} else {
			return 0;
		}
	}

	public double getAvgScore() {
		List<QuizEvent> list = allEvents();
		double total = 0;
		for (int i = 0; i < list.size(); i++) {
			total += list.get(i).getScore();
		}
		double avg = total / getTakenTimes();
		assert avg <= getTotalScore();
		return avg;
	}

}
