<!--
  The MIT License (MIT)
  Copyright (c) 2013 Jing Pu, Yang Zhao, You Yuan, Huijie Yu 
  
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to 
  deal in the Software without restriction, including without limitation the
  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or 
  sell copies of the Software, and to permit persons to whom the Software is 
  furnished to do so, subject to the following conditions:
  
  The above copyright notice and this permission notice shall be included in 
  all copies or substantial portions of the Software.
  
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, 
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN 
  THE SOFTWARE.
-->
<%@ page language="java" contentType="text/html; charset=US-ASCII"
	pageEncoding="US-ASCII"%>
<%@ page import="java.util.*"%>
<%@ page import="user.Account"%>
<%@ page import="user.UserManager"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=US-ASCII">
<link href="CSS/page_style.css" rel="stylesheet" type="text/css" />
<link href="friendpage_style.css" rel="stylesheet" type="text/css" />
<title>Friends</title>
</head>
<%
	String userId = request.getParameter("id");
	String guest = (String) session.getAttribute("guest");
	if (guest == null || guest.equals("guest") || !UserManager.alreadyExist(guest)) {
		response.sendRedirect("index.html");
		return;
	} else if(userId == null || !guest.equals(userId)){
		response.sendRedirect("friendpage.jsp?id=" + guest);
		return;
	}
	Account user = new Account(userId);
	List<String> friends = user.getFriendsList();
	List<String> unconfirmed = user.getFriendRequests();
%>
<script language="javascript" type="text/javascript">
	function friendQuery(text) {
		var r = confirm(text);
		if (r) {
			return true;
		}
		return false;
	}
</script>
<body>
	<div id="wrapper">
		<div id="inner">
			<div id="header">
				<h1><%=userId%></h1>
				<h3><%=new Date()%></h3>
				<div id="nav">
					<h2>
						<a href="home.jsp">Home</a> | <a href="Logout">Log out</a>
					</h2>
				</div>
			</div>
			<div id="body">
				<div class="inner">
					<div class="leftbox">
						<h3>My Friends</h3>
						<%
		if (friends.isEmpty()) {
	%>
						<p>I don't have any friends yet.</p>
						<%
		} else {
			for (String friend : friends) {
				String text = "Are you sure to unfriend " + friend + "?";
	%>
						<p>
							<a href="userpage.jsp?id=<%=friend%>"><%=friend%></a> | <a
								href="RemoveFriend?to=<%=friend%>"
								onclick="friendQuery('<%=text%>')">Unfriend</a>
						</p>
						<%
		}
		}
	%>
					</div>
					<div class="rightbox">
						<h3>Friend Requests</h3>
						<%
		if (unconfirmed.isEmpty()) {
	%>
						<p>I don't have any friend requests now.</p>
						<%
		} else {
			for (String friend : unconfirmed) {
				String text = "Do you want to add " + friend
						+ " as friend?";
	%>
						<p>
							<a href="userpage.jsp?id=<%=friend%>"><%=friend%></a> | <a
								href="RespondFriend?to=<%=friend%>"
								onclick="friendQuery('<%=text%>')">Respond</a>
						</p>
						<%
		}
		}
	%>
					</div>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
