<%@ page language="java" contentType="text/html; charset=US-ASCII"
	pageEncoding="US-ASCII"%>
<%@ page import="java.util.*"%>
<%@ page import="user.Account"%>
<%@ page import="user.Administrator"%>
<%@ page import="user.UserManager"%>
<%@ page import="user.Message"%>
<%@ page import="user.Activity"%>
<%@ page import="user.TimeTrsf"%>
<%@ page import="java.text.SimpleDateFormat"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<title>Reading Message</title>
</head>
<%
	String userId = request.getParameter("id");
	session = request.getSession();
	String guest = (String) session.getAttribute("guest");
	if (userId == null || guest.equals("guest")) {
		response.sendRedirect("index.html");
		return;
	} else if (!guest.equals(userId)) {
		response.sendRedirect("home.jsp?id=" + guest);
		return;
	}
	String box = request.getParameter("box");
	String msgCode = request.getParameter("msg");
	Account user = new Account(userId);
	Message msg = user.readMessage(box, msgCode);
	SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss.S");
	Date time = sdf.parse(msg.getTime());
	String timeDscr = TimeTrsf.dscr(time, new Date());
%>
<body>
	<p>Message</p>
	<table border="1" width="300" rules="rows">
		<tr><th><%=msg.title%>	<%=timeDscr %></th></tr>
		<tr><td><%=msg.from%></td></tr>
		<tr><td><%=msg.to%></td></tr>
		<tr><td><%=msg.content%></td></tr>
	</table>
</body>
</html>
